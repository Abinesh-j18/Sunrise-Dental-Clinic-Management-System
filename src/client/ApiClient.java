package client;

import com.google.gson.reflect.TypeToken;
import model.*;
import server.JsonUtil;
import server.RestResponse;
import server.dto.BookAppointmentRequest;
import server.dto.GenerateInvoiceRequest;
import server.dto.LoginRequest;
import server.dto.LoginResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tier 1 HTTP REST Client using java.net.http.HttpClient.
 * Communicates strictly over HTTP/JSON with the REST Service layer.
 *
 * @author Student
 */
public class ApiClient {
    private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    private final String baseUrl;
    private final HttpClient httpClient;

    public ApiClient() {
        this(DEFAULT_BASE_URL);
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public LoginResponse login(String username, String password) throws ApiException, IOException, InterruptedException {
        LoginRequest req = new LoginRequest(username, password);
        String reqJson = JsonUtil.toJson(req);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/login"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(reqJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<LoginResponse>>() {}.getType();
        RestResponse<LoginResponse> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public void logout() {
        try {
            HttpRequest request = authorizedRequestBuilder(baseUrl + "/logout")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during logout API call", e);
        } finally {
            SessionContext.getInstance().clearSession();
        }
    }

    public List<DentistProfile> getDentists() throws ApiException, IOException, InterruptedException {
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/dentists")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<List<DentistProfile>>>() {}.getType();
        RestResponse<List<DentistProfile>> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public List<Treatment> getTreatments() throws ApiException, IOException, InterruptedException {
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/treatments")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<List<Treatment>>>() {}.getType();
        RestResponse<List<Treatment>> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public List<server.dto.StaffMemberDTO> getStaffMembers() throws ApiException, IOException, InterruptedException {
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/staff")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<List<server.dto.StaffMemberDTO>>>() {}.getType();
        RestResponse<List<server.dto.StaffMemberDTO>> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public server.dto.StaffMemberDTO registerStaffMember(server.dto.RegisterStaffRequest req) throws ApiException, IOException, InterruptedException {
        String reqJson = JsonUtil.toJson(req);
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/staff")
                .POST(HttpRequest.BodyPublishers.ofString(reqJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<server.dto.StaffMemberDTO>>() {}.getType();
        RestResponse<server.dto.StaffMemberDTO> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public boolean deleteStaffMember(int userId) throws ApiException, IOException, InterruptedException {
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/staff/" + userId)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<Boolean>>() {}.getType();
        RestResponse<Boolean> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return Boolean.TRUE.equals(restResp.getData());
    }

    public Treatment createTreatment(Treatment treatment) throws ApiException, IOException, InterruptedException {
        String reqJson = JsonUtil.toJson(treatment);
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/treatments")
                .POST(HttpRequest.BodyPublishers.ofString(reqJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<Treatment>>() {}.getType();
        RestResponse<Treatment> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public boolean deleteTreatment(int treatmentId) throws ApiException, IOException, InterruptedException {
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/treatments/" + treatmentId)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<Boolean>>() {}.getType();
        RestResponse<Boolean> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return Boolean.TRUE.equals(restResp.getData());
    }

    public List<Patient> getPatients(String query) throws ApiException, IOException, InterruptedException {
        String url = baseUrl + "/patients";
        if (query != null && !query.trim().isEmpty()) {
            url += "?search=" + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        }

        HttpRequest request = authorizedRequestBuilder(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<List<Patient>>>() {}.getType();
        RestResponse<List<Patient>> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public Patient registerPatient(Patient patient) throws ApiException, IOException, InterruptedException {
        String reqJson = JsonUtil.toJson(patient);
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/patients")
                .POST(HttpRequest.BodyPublishers.ofString(reqJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<Patient>>() {}.getType();
        RestResponse<Patient> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public Appointment bookAppointment(BookAppointmentRequest bookReq) throws ApiException, IOException, InterruptedException {
        String reqJson = JsonUtil.toJson(bookReq);
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/appointments")
                .POST(HttpRequest.BodyPublishers.ofString(reqJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<Appointment>>() {}.getType();
        RestResponse<Appointment> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public Appointment getAppointmentByNumber(String appointmentNumber) throws ApiException, IOException, InterruptedException {
        String encoded = URLEncoder.encode(appointmentNumber.trim(), StandardCharsets.UTF_8);
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/appointments/" + encoded)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<Appointment>>() {}.getType();
        RestResponse<Appointment> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public Appointment updateAppointmentTreatment(int appointmentId, int treatmentId, String clinicalNotes, String status) throws ApiException, IOException, InterruptedException {
        server.dto.UpdateTreatmentRequest req = new server.dto.UpdateTreatmentRequest(treatmentId, clinicalNotes, status);
        String reqJson = JsonUtil.toJson(req);
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/appointments/" + appointmentId + "/treatment")
                .POST(HttpRequest.BodyPublishers.ofString(reqJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<Appointment>>() {}.getType();
        RestResponse<Appointment> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public List<Appointment> getAllAppointments() throws ApiException, IOException, InterruptedException {
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/appointments")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<List<Appointment>>>() {}.getType();
        RestResponse<List<Appointment>> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public List<Appointment> getAppointmentsByDentistAndDate(int dentistId, LocalDate date) throws ApiException, IOException, InterruptedException {
        String url = baseUrl + "/appointments?dentistId=" + dentistId;
        if (date != null) {
            url += "&date=" + date.toString();
        }

        HttpRequest request = authorizedRequestBuilder(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<List<Appointment>>>() {}.getType();
        RestResponse<List<Appointment>> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public Invoice calculateAndGenerateInvoice(int appointmentId, String paymentMethod) throws ApiException, IOException, InterruptedException {
        GenerateInvoiceRequest req = new GenerateInvoiceRequest(paymentMethod);
        String reqJson = JsonUtil.toJson(req);

        HttpRequest request = authorizedRequestBuilder(baseUrl + "/invoices/" + appointmentId)
                .POST(HttpRequest.BodyPublishers.ofString(reqJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<Invoice>>() {}.getType();
        RestResponse<Invoice> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public Invoice getInvoiceByAppointmentId(int appointmentId) throws ApiException, IOException, InterruptedException {
        HttpRequest request = authorizedRequestBuilder(baseUrl + "/invoices/" + appointmentId)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<Invoice>>() {}.getType();
        RestResponse<Invoice> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public List<DailyAppointmentReportItem> getDailyAppointmentsReport(Integer dentistId, LocalDate date) throws ApiException, IOException, InterruptedException {
        StringBuilder url = new StringBuilder(baseUrl + "/reports/daily-appointments?");
        if (dentistId != null && dentistId > 0) {
            url.append("dentistId=").append(dentistId).append("&");
        }
        if (date != null) {
            url.append("date=").append(date.toString()).append("&");
        }

        HttpRequest request = authorizedRequestBuilder(url.toString())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<List<DailyAppointmentReportItem>>>() {}.getType();
        RestResponse<List<DailyAppointmentReportItem>> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public List<RevenueByTreatmentReportItem> getRevenueByTreatmentReport(Integer month, Integer year) throws ApiException, IOException, InterruptedException {
        StringBuilder url = new StringBuilder(baseUrl + "/reports/revenue-by-treatment?");
        if (month != null && month > 0) {
            url.append("month=").append(month).append("&");
        }
        if (year != null && year > 0) {
            url.append("year=").append(year).append("&");
        }

        HttpRequest request = authorizedRequestBuilder(url.toString())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<List<RevenueByTreatmentReportItem>>>() {}.getType();
        RestResponse<List<RevenueByTreatmentReportItem>> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    public List<TopTreatmentReportItem> getTopTreatmentsReport(int limit) throws ApiException, IOException, InterruptedException {
        String url = baseUrl + "/reports/top-treatments?limit=" + limit;
        HttpRequest request = authorizedRequestBuilder(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Type type = new TypeToken<RestResponse<List<TopTreatmentReportItem>>>() {}.getType();
        RestResponse<List<TopTreatmentReportItem>> restResp = JsonUtil.fromJson(response.body(), type);

        handleErrorIfNeeded(response.statusCode(), restResp);
        return restResp.getData();
    }

    private HttpRequest.Builder authorizedRequestBuilder(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15));

        String token = SessionContext.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
            builder.header("X-Session-Token", token);
        }
        return builder;
    }

    private void handleErrorIfNeeded(int statusCode, RestResponse<?> restResp) throws ApiException {
        if (statusCode >= 400 || (restResp != null && !restResp.isSuccess())) {
            String message = restResp != null && restResp.getMessage() != null ? restResp.getMessage() : "HTTP Error " + statusCode;
            String errorType = restResp != null && restResp.getError() != null ? restResp.getError() : "API Error";
            throw new ApiException(statusCode, errorType, message);
        }
    }
}
