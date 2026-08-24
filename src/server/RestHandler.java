package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.*;
import server.dto.BookAppointmentRequest;
import server.dto.GenerateInvoiceRequest;
import server.dto.LoginRequest;
import server.dto.LoginResponse;
import service.*;
import service.reports.ReportType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central REST HTTP Request Handler dispatching requests to service layer methods,
 * enforcing session authentication headers, and formatting responses as JSON.
 *
 * @author Student
 */
public class RestHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(RestHandler.class.getName());

    private final ClinicService clinicService;
    private final SessionManager sessionManager;

    public RestHandler(ClinicService clinicService, SessionManager sessionManager) {
        this.clinicService = clinicService;
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        LOGGER.info(String.format("Incoming HTTP %s %s", method, path));

        // CORS headers for broad client interoperability
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Session-Token");

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            // 1. Unauthenticated endpoints & Browser Service Explorer
            if ("/".equals(path) || "/api".equalsIgnoreCase(path) || "/api-docs".equalsIgnoreCase(path)) {
                handleBrowserLandingPage(exchange);
                return;
            } else if ("/health".equalsIgnoreCase(path) && "GET".equalsIgnoreCase(method)) {
                handleHealthCheck(exchange);
                return;
            } else if ("/login".equalsIgnoreCase(path) && "POST".equalsIgnoreCase(method)) {
                handleLogin(exchange);
                return;
            }

            // 2. Authenticate session token for all other protected endpoints
            String token = extractAuthToken(exchange);
            User authUser = sessionManager.validateSession(token);
            if (authUser == null) {
                sendErrorResponse(exchange, 401, "Unauthorized", "Missing, invalid, or expired session token. Please log in or provide ?token= in URL.");
                return;
            }

            // 3. Dispatch to authenticated routes
            dispatchRoute(exchange, method, path, uri.getRawQuery(), authUser);

        } catch (ValidationException ve) {
            LOGGER.log(Level.WARNING, "Validation error: " + ve.getMessage());
            sendErrorResponse(exchange, 400, "Bad Request", ve.getMessage());
        } catch (AuthenticationException ae) {
            LOGGER.log(Level.WARNING, "Authentication error: " + ae.getMessage());
            sendErrorResponse(exchange, 401, "Unauthorized", ae.getMessage());
        } catch (DoubleBookingException dbe) {
            LOGGER.log(Level.WARNING, "Double booking detected: " + dbe.getMessage());
            sendErrorResponse(exchange, 409, "Conflict", dbe.getMessage());
        } catch (ResourceNotFoundException rnfe) {
            LOGGER.log(Level.WARNING, "Resource not found: " + rnfe.getMessage());
            sendErrorResponse(exchange, 404, "Not Found", rnfe.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled server error processing " + path, e);
            sendErrorResponse(exchange, 500, "Internal Server Error", "An internal server error occurred. Please contact clinic systems administrator.");
        }
    }

    private void dispatchRoute(HttpExchange exchange, String method, String path, String rawQuery, User user)
            throws Exception {

        Map<String, String> queryParams = parseQueryParams(rawQuery);

        if ("/logout".equalsIgnoreCase(path) && "POST".equalsIgnoreCase(method)) {
            handleLogout(exchange);
        } else if ("/patients".equalsIgnoreCase(path)) {
            if ("POST".equalsIgnoreCase(method)) {
                handleRegisterPatient(exchange);
            } else if ("GET".equalsIgnoreCase(method)) {
                handleGetPatients(exchange, queryParams);
            } else {
                sendErrorResponse(exchange, 405, "Method Not Allowed", "Method not allowed for /patients");
            }
        } else if ("/dentists".equalsIgnoreCase(path) && "GET".equalsIgnoreCase(method)) {
            handleGetDentists(exchange);
        } else if ("/treatments".equalsIgnoreCase(path)) {
            if ("POST".equalsIgnoreCase(method)) {
                handleCreateTreatment(exchange);
            } else if ("GET".equalsIgnoreCase(method)) {
                handleGetTreatments(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method Not Allowed", "Method not allowed for /treatments");
            }
        } else if (path.startsWith("/treatments/") && "DELETE".equalsIgnoreCase(method)) {
            String idStr = path.substring("/treatments/".length());
            handleDeleteTreatment(exchange, Integer.parseInt(idStr));
        } else if ("/staff".equalsIgnoreCase(path)) {
            if ("GET".equalsIgnoreCase(method)) {
                handleGetStaff(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handleRegisterStaff(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method Not Allowed", "Method not allowed for /staff");
            }
        } else if (path.startsWith("/staff/") && "DELETE".equalsIgnoreCase(method)) {
            String idStr = path.substring("/staff/".length());
            handleDeleteStaff(exchange, Integer.parseInt(idStr));
        } else if ("/appointments".equalsIgnoreCase(path)) {
            if ("POST".equalsIgnoreCase(method)) {
                handleBookAppointment(exchange);
            } else if ("GET".equalsIgnoreCase(method)) {
                handleGetAppointments(exchange, queryParams);
            } else {
                sendErrorResponse(exchange, 405, "Method Not Allowed", "Method not allowed for /appointments");
            }
        } else if (path.startsWith("/appointments/")) {
            if (path.endsWith("/treatment") && ("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))) {
                String idStr = path.substring("/appointments/".length(), path.length() - "/treatment".length());
                int apptId = Integer.parseInt(idStr);
                handleUpdateAppointmentTreatment(exchange, apptId);
            } else if ("GET".equalsIgnoreCase(method)) {
                String apptNumber = path.substring("/appointments/".length());
                handleGetAppointmentByNumber(exchange, apptNumber);
            } else {
                sendErrorResponse(exchange, 405, "Method Not Allowed", "Method not allowed for /appointments/{id}");
            }
        } else if (path.startsWith("/invoices/")) {
            String idStr = path.substring("/invoices/".length());
            int apptId = Integer.parseInt(idStr);
            if ("POST".equalsIgnoreCase(method)) {
                handleGenerateInvoice(exchange, apptId);
            } else if ("GET".equalsIgnoreCase(method)) {
                handleGetInvoice(exchange, apptId);
            } else {
                sendErrorResponse(exchange, 405, "Method Not Allowed", "Method not allowed for /invoices/{id}");
            }
        } else if (path.startsWith("/reports/")) {
            if ("GET".equalsIgnoreCase(method)) {
                handleReports(exchange, path, queryParams);
            } else {
                sendErrorResponse(exchange, 405, "Method Not Allowed", "Method not allowed for /reports");
            }
        } else {
            sendErrorResponse(exchange, 404, "Not Found", "Endpoint not found: " + path);
        }
    }

    private void handleLogin(HttpExchange exchange) throws Exception {
        String body = readRequestBody(exchange);
        LoginRequest req = JsonUtil.fromJson(body, LoginRequest.class);
        if (req == null) {
            throw new ValidationException("Login payload cannot be empty.");
        }

        User user = clinicService.authenticate(req.getUsername(), req.getPassword());
        String token = sessionManager.createSession(user);

        LoginResponse respData = new LoginResponse(token, user);
        sendJsonResponse(exchange, 200, RestResponse.ok(respData, "Authentication successful."));
    }

    private void handleLogout(HttpExchange exchange) throws Exception {
        String token = extractAuthToken(exchange);
        sessionManager.invalidateSession(token);
        sendJsonResponse(exchange, 200, RestResponse.ok(true, "Logged out successfully."));
    }

    private void handleRegisterPatient(HttpExchange exchange) throws Exception {
        String body = readRequestBody(exchange);
        Patient patient = JsonUtil.fromJson(body, Patient.class);
        Patient created = clinicService.registerPatient(patient);
        sendJsonResponse(exchange, 201, RestResponse.created(created, "Patient registered successfully."));
    }

    private void handleGetPatients(HttpExchange exchange, Map<String, String> query) throws Exception {
        String search = query.get("search");
        sendJsonResponse(exchange, 200, RestResponse.ok(clinicService.getPatients(search), "Patients retrieved successfully."));
    }

    private void handleGetDentists(HttpExchange exchange) throws Exception {
        sendJsonResponse(exchange, 200, RestResponse.ok(clinicService.getDentists(), "Dentists retrieved successfully."));
    }

    private void handleGetTreatments(HttpExchange exchange) throws Exception {
        sendJsonResponse(exchange, 200, RestResponse.ok(clinicService.getTreatments(), "Treatments retrieved successfully."));
    }

    private void handleCreateTreatment(HttpExchange exchange) throws Exception {
        String body = readRequestBody(exchange);
        Treatment trt = JsonUtil.fromJson(body, Treatment.class);
        Treatment created = clinicService.createTreatment(trt);
        sendJsonResponse(exchange, 201, RestResponse.created(created, "Treatment added to catalog successfully."));
    }

    private void handleDeleteTreatment(HttpExchange exchange, int id) throws Exception {
        boolean ok = clinicService.deleteTreatment(id);
        sendJsonResponse(exchange, 200, RestResponse.ok(ok, "Treatment removed from catalog."));
    }

    private void handleGetStaff(HttpExchange exchange) throws Exception {
        sendJsonResponse(exchange, 200, RestResponse.ok(clinicService.getAllStaffMembers(), "Staff members retrieved."));
    }

    private void handleRegisterStaff(HttpExchange exchange) throws Exception {
        String body = readRequestBody(exchange);
        server.dto.RegisterStaffRequest req = JsonUtil.fromJson(body, server.dto.RegisterStaffRequest.class);
        server.dto.StaffMemberDTO created = clinicService.registerStaffMember(req);
        sendJsonResponse(exchange, 201, RestResponse.created(created, "Staff member registered successfully."));
    }

    private void handleDeleteStaff(HttpExchange exchange, int userId) throws Exception {
        boolean ok = clinicService.deleteStaffMember(userId);
        sendJsonResponse(exchange, 200, RestResponse.ok(ok, "Staff member removed successfully."));
    }

    private void handleBookAppointment(HttpExchange exchange) throws Exception {
        String body = readRequestBody(exchange);
        BookAppointmentRequest req = JsonUtil.fromJson(body, BookAppointmentRequest.class);
        if (req == null) {
            throw new ValidationException("Booking request payload cannot be empty.");
        }

        Appointment appt = clinicService.bookAppointment(
                req.getPatientId(),
                req.getDentistId(),
                req.getTreatmentId(),
                req.getDate(),
                req.getTime(),
                req.getNotes()
        );

        sendJsonResponse(exchange, 201, RestResponse.created(appt, "Appointment booked successfully with number: " + appt.getAppointmentNumber()));
    }

    private void handleGetAppointments(HttpExchange exchange, Map<String, String> query) throws Exception {
        String dentistIdStr = query.get("dentistId");
        String dateStr = query.get("date");

        if (dentistIdStr != null && !dentistIdStr.isEmpty()) {
            int dentistId = Integer.parseInt(dentistIdStr);
            LocalDate date = dateStr != null && !dateStr.isEmpty() ? LocalDate.parse(dateStr) : LocalDate.now();
            sendJsonResponse(exchange, 200, RestResponse.ok(clinicService.getAppointmentsByDentistAndDate(dentistId, date), "Appointments retrieved."));
        } else {
            sendJsonResponse(exchange, 200, RestResponse.ok(clinicService.getAllAppointments(), "All appointments retrieved."));
        }
    }

    private void handleGetAppointmentByNumber(HttpExchange exchange, String apptNumber) throws Exception {
        Appointment appt = clinicService.getAppointmentByNumber(URLDecoder.decode(apptNumber, StandardCharsets.UTF_8));
        sendJsonResponse(exchange, 200, RestResponse.ok(appt, "Appointment details retrieved."));
    }

    private void handleUpdateAppointmentTreatment(HttpExchange exchange, int appointmentId) throws Exception {
        String body = readRequestBody(exchange);
        server.dto.UpdateTreatmentRequest req = JsonUtil.fromJson(body, server.dto.UpdateTreatmentRequest.class);
        if (req == null || req.getTreatmentId() <= 0) {
            throw new ValidationException("A valid treatment selection is required.");
        }

        Appointment updated = clinicService.updateAppointmentTreatment(
                appointmentId,
                req.getTreatmentId(),
                req.getClinicalNotes(),
                req.getStatus()
        );
        sendJsonResponse(exchange, 200, RestResponse.ok(updated, "Doctor treatment and clinical notes updated successfully."));
    }

    private void handleGenerateInvoice(HttpExchange exchange, int apptId) throws Exception {
        String body = readRequestBody(exchange);
        GenerateInvoiceRequest req = JsonUtil.fromJson(body, GenerateInvoiceRequest.class);
        String paymentMethod = req != null && req.getPaymentMethod() != null ? req.getPaymentMethod() : "Cash";

        Invoice invoice = clinicService.calculateAndGenerateInvoice(apptId, paymentMethod);
        sendJsonResponse(exchange, 200, RestResponse.ok(invoice, "Invoice generated successfully."));
    }

    private void handleGetInvoice(HttpExchange exchange, int apptId) throws Exception {
        Invoice invoice = clinicService.getInvoiceByAppointmentId(apptId);
        sendJsonResponse(exchange, 200, RestResponse.ok(invoice, "Invoice retrieved successfully."));
    }

    private void handleReports(HttpExchange exchange, String path, Map<String, String> query) throws Exception {
        if ("/reports/daily-appointments".equalsIgnoreCase(path)) {
            Map<String, Object> params = new HashMap<>(query);
            Object data = clinicService.generateReport(ReportType.DAILY_APPOINTMENTS, params);
            sendJsonResponse(exchange, 200, RestResponse.ok(data, "Daily appointments report generated."));
        } else if ("/reports/revenue-by-treatment".equalsIgnoreCase(path)) {
            Map<String, Object> params = new HashMap<>(query);
            Object data = clinicService.generateReport(ReportType.REVENUE_BY_TREATMENT, params);
            sendJsonResponse(exchange, 200, RestResponse.ok(data, "Revenue by treatment report generated."));
        } else if ("/reports/top-treatments".equalsIgnoreCase(path)) {
            Map<String, Object> params = new HashMap<>(query);
            Object data = clinicService.generateReport(ReportType.TOP_TREATMENTS, params);
            sendJsonResponse(exchange, 200, RestResponse.ok(data, "Top treatments report generated."));
        } else {
            sendErrorResponse(exchange, 404, "Not Found", "Unknown report: " + path);
        }
    }

    private void handleHealthCheck(HttpExchange exchange) throws IOException {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Sunrise Dental Clinic Management REST Web Service");
        health.put("tier", "Tier 2 (Service Layer)");
        health.put("protocol", "HTTP/1.1 RESTful JSON");
        health.put("port", exchange.getLocalAddress().getPort());
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        health.put("activeSessions", sessionManager.getActiveSessionCount());

        sendJsonResponse(exchange, 200, RestResponse.ok(health, "REST Web Service is healthy and operational."));
    }

    private void handleBrowserLandingPage(HttpExchange exchange) throws IOException {
        String html = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Sunrise Dental Clinic — Tier 2 REST Web Service</title>\n" +
                "    <style>\n" +
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f8fafc; color: #1e293b; margin: 0; padding: 40px; }\n" +
                "        .container { max-width: 900px; margin: 0 auto; background: white; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); padding: 36px; border: 1px solid #e2e8f0; }\n" +
                "        h1 { color: #0284c7; margin-top: 0; font-size: 26px; }\n" +
                "        .badge { display: inline-block; background: #ecfdf5; color: #047857; padding: 4px 12px; border-radius: 9999px; font-weight: 600; font-size: 13px; margin-bottom: 20px; border: 1px solid #a7f3d0; }\n" +
                "        .card { background: #f1f5f9; border-left: 4px solid #0284c7; padding: 14px 18px; margin: 16px 0; border-radius: 0 8px 8px 0; }\n" +
                "        table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n" +
                "        th, td { text-align: left; padding: 12px 14px; border-bottom: 1px solid #e2e8f0; }\n" +
                "        th { background: #f8fafc; color: #64748b; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; }\n" +
                "        .method { font-weight: bold; padding: 3px 8px; border-radius: 4px; font-size: 12px; display: inline-block; }\n" +
                "        .get { background: #dbeafe; color: #1d4ed8; }\n" +
                "        .post { background: #dcfce7; color: #15803d; }\n" +
                "        .put { background: #fef3c7; color: #b45309; }\n" +
                "        a { color: #0284c7; text-decoration: none; font-weight: 500; }\n" +
                "        a:hover { text-decoration: underline; }\n" +
                "        code { background: #f1f5f9; padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 13px; color: #0f172a; }\n" +
                "        .footer { margin-top: 30px; font-size: 12px; color: #94a3b8; text-align: center; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <span class=\"badge\">LIVE REST SERVER ONLINE</span>\n" +
                "        <h1>Sunrise Dental Clinic REST Web Service</h1>\n" +
                "        <p>This is the <strong>Tier 2 Service Layer</strong> built using Java's built-in HTTP Server (<code>com.sun.net.httpserver.HttpServer</code>) with multi-threaded fixed thread pool concurrency.</p>\n" +
                "        \n" +
                "        <div class=\"card\">\n" +
                "            <strong>3-Tier Architecture Flow:</strong><br>\n" +
                "            Tier 1 (Swing Desktop Client) &harr; <strong>Tier 2 (REST HTTP Server :8080)</strong> &harr; Tier 3 (MySQL <code>sunrisedb</code> via JDBC)\n" +
                "        </div>\n" +
                "\n" +
                "        <h3>Public Browser Test Endpoints (Click to inspect JSON):</h3>\n" +
                "        <table>\n" +
                "            <thead>\n" +
                "                <tr><th>HTTP Verb</th><th>REST Endpoint</th><th>Description</th><th>Direct Link</th></tr>\n" +
                "            </thead>\n" +
                "            <tbody>\n" +
                "                <tr><td><span class=\"method get\">GET</span></td><td><code>/health</code></td><td>Server health & connection status</td><td><a href=\"/health\" target=\"_blank\">/health</a></td></tr>\n" +
                "                <tr><td><span class=\"method get\">GET</span></td><td><code>/dentists</code></td><td>List of active dental surgeons</td><td><a href=\"/dentists\" target=\"_blank\">/dentists</a></td></tr>\n" +
                "                <tr><td><span class=\"method get\">GET</span></td><td><code>/treatments</code></td><td>Catalog of dental procedures & tariff</td><td><a href=\"/treatments\" target=\"_blank\">/treatments</a></td></tr>\n" +
                "            </tbody>\n" +
                "        </table>\n" +
                "\n" +
                "        <h3>Authenticated REST Endpoints (Requires Bearer token or ?token=):</h3>\n" +
                "        <table>\n" +
                "            <thead>\n" +
                "                <tr><th>HTTP Verb</th><th>REST Endpoint</th><th>Description</th></tr>\n" +
                "            </thead>\n" +
                "            <tbody>\n" +
                "                <tr><td><span class=\"method post\">POST</span></td><td><code>/login</code></td><td>Authenticate staff credentials & obtain JWT/Bearer session token</td></tr>\n" +
                "                <tr><td><span class=\"method post\">POST</span></td><td><code>/logout</code></td><td>Invalidate session token</td></tr>\n" +
                "                <tr><td><span class=\"method post\">POST</span></td><td><code>/patients</code></td><td>Register new patient record</td></tr>\n" +
                "                <tr><td><span class=\"method get\">GET</span></td><td><code>/patients</code></td><td>Search and list clinic patients</td></tr>\n" +
                "                <tr><td><span class=\"method post\">POST</span></td><td><code>/appointments</code></td><td>Book appointment with double-booking prevention trigger</td></tr>\n" +
                "                <tr><td><span class=\"method get\">GET</span></td><td><code>/appointments/{number}</code></td><td>Search appointment by APT-YYYY-XXXX number</td></tr>\n" +
                "                <tr><td><span class=\"method post\">POST</span></td><td><code>/appointments/{id}/treatment</code></td><td>Doctor updates diagnosed clinical treatment & notes</td></tr>\n" +
                "                <tr><td><span class=\"method post\">POST</span></td><td><code>/invoices/{appointmentId}</code></td><td>Calculate & generate invoice via MySQL Stored Procedure</td></tr>\n" +
                "                <tr><td><span class=\"method get\">GET</span></td><td><code>/reports/daily-appointments</code></td><td>Generate daily dentist appointment schedule report</td></tr>\n" +
                "                <tr><td><span class=\"method get\">GET</span></td><td><code>/reports/revenue-by-treatment</code></td><td>Generate clinic revenue breakdown report</td></tr>\n" +
                "                <tr><td><span class=\"method get\">GET</span></td><td><code>/reports/top-treatments</code></td><td>Generate top requested treatments report</td></tr>\n" +
                "            </tbody>\n" +
                "        </table>\n" +
                "\n" +
                "        <div class=\"footer\">\n" +
                "            Sunrise Dental Clinic Management System &bull; CIS6003 Advanced Programming\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, htmlBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(htmlBytes);
        }
    }

    private String extractAuthToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length()).trim();
        }
        String customHeader = exchange.getRequestHeaders().getFirst("X-Session-Token");
        if (customHeader != null && !customHeader.trim().isEmpty()) {
            return customHeader.trim();
        }
        // Support browser URL query param testing: ?token=...
        if (exchange.getRequestURI().getRawQuery() != null) {
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getRawQuery());
            String queryToken = query.get("token");
            if (queryToken != null && !queryToken.trim().isEmpty()) {
                return queryToken.trim();
            }
        }
        return null;
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object responseObj) throws IOException {
        String json = JsonUtil.toJson(responseObj);
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String error, String message) throws IOException {
        RestResponse<Void> resp = RestResponse.error(statusCode, error, message);
        sendJsonResponse(exchange, statusCode, resp);
    }

    private Map<String, String> parseQueryParams(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return map;
        }
        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = idx < pair.length() - 1 ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : "";
                map.put(key, val);
            }
        }
        return map;
    }
}
