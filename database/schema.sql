-- =====================================================================
-- SUNRISE DENTAL CLINIC MANAGEMENT SYSTEM
-- Database Schema, Triggers, Stored Procedures, Functions, and Seed Data
-- Target: MySQL (XAMPP / phpMyAdmin compatible)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `sunrisedb` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `sunrisedb`;

-- ---------------------------------------------------------------------
-- 1. DROP EXISTING OBJECTS (FOR CLEAN RE-INSTALLATION)
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS `trg_before_insert_appointment`;
DROP PROCEDURE IF EXISTS `CalculateInvoiceTotal`;
DROP FUNCTION IF EXISTS `CheckDentistAvailability`;

DROP TABLE IF EXISTS `invoice_items`;
DROP TABLE IF EXISTS `invoices`;
DROP TABLE IF EXISTS `appointments`;
DROP TABLE IF EXISTS `treatments`;
DROP TABLE IF EXISTS `dentists`;
DROP TABLE IF EXISTS `patients`;
DROP TABLE IF EXISTS `users`;

-- ---------------------------------------------------------------------
-- 2. TABLE: users
-- ---------------------------------------------------------------------
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `role` ENUM('Administrator', 'Receptionist', 'Dentist') NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 3. TABLE: dentists
-- ---------------------------------------------------------------------
CREATE TABLE `dentists` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `specialization` VARCHAR(100) NOT NULL,
    `contact_number` VARCHAR(20) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_dentist_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 4. TABLE: patients (No login, managed by clinic staff)
-- ---------------------------------------------------------------------
CREATE TABLE `patients` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `contact_number` VARCHAR(20) NOT NULL,
    `email` VARCHAR(100) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 5. TABLE: treatments
-- ---------------------------------------------------------------------
CREATE TABLE `treatments` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `type` VARCHAR(100) NOT NULL UNIQUE,
    `cost` DECIMAL(10,2) NOT NULL,
    `description` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 6. TABLE: appointments
-- ---------------------------------------------------------------------
CREATE TABLE `appointments` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `appointment_number` VARCHAR(30) UNIQUE,
    `patient_id` INT NOT NULL,
    `dentist_id` INT NOT NULL,
    `treatment_id` INT NOT NULL,
    `appointment_date` DATE NOT NULL,
    `appointment_time` TIME NOT NULL,
    `status` ENUM('SCHEDULED', 'IN PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    `notes` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_appt_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_appt_dentist` FOREIGN KEY (`dentist_id`) REFERENCES `dentists` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_appt_treatment` FOREIGN KEY (`treatment_id`) REFERENCES `treatments` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 7. TABLE: invoices
-- ---------------------------------------------------------------------
CREATE TABLE `invoices` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `appointment_id` INT NOT NULL UNIQUE,
    `consultation_fee` DECIMAL(10,2) NOT NULL DEFAULT 1500.00,
    `treatment_cost` DECIMAL(10,2) NOT NULL,
    `total_amount` DECIMAL(10,2) NOT NULL,
    `generated_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `status` ENUM('PAID', 'PENDING', 'CANCELLED') DEFAULT 'PAID',
    `payment_method` VARCHAR(50) DEFAULT 'Cash',
    CONSTRAINT `fk_invoice_appt` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 8. TABLE: invoice_items (Composition: Invoice owns items)
-- ---------------------------------------------------------------------
CREATE TABLE `invoice_items` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `invoice_id` INT NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    CONSTRAINT `fk_item_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- 9. STORED FUNCTION: CheckDentistAvailability
-- Returns 1 if dentist has NO conflicting scheduled appointment at date/time.
-- Returns 0 if slot is already occupied (prevents double bookings).
-- =====================================================================
DELIMITER $$
CREATE FUNCTION `CheckDentistAvailability`(
    `p_dentist_id` INT,
    `p_date` DATE,
    `p_time` TIME
) RETURNS TINYINT(1)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE `conflict_count` INT;
    
    SELECT COUNT(*) INTO `conflict_count`
    FROM `appointments`
    WHERE `dentist_id` = `p_dentist_id`
      AND `appointment_date` = `p_date`
      AND `appointment_time` = `p_time`
      AND `status` IN ('SCHEDULED', 'IN PROGRESS');
      
    IF `conflict_count` = 0 THEN
        RETURN 1; -- Available
    ELSE
        RETURN 0; -- Double booking detected / unavailable
    END IF;
END$$
DELIMITER ;

-- =====================================================================
-- 10. TRIGGER: trg_before_insert_appointment
-- Auto-generates formatted appointment number (e.g. APT-2026-0001)
-- Never typed by staff, guaranteeing uniqueness.
-- =====================================================================
DELIMITER $$
CREATE TRIGGER `trg_before_insert_appointment`
BEFORE INSERT ON `appointments`
FOR EACH ROW
BEGIN
    DECLARE `next_seq` INT DEFAULT 1;
    DECLARE `appt_year` VARCHAR(10);
    DECLARE `pattern` VARCHAR(30);
    
    IF NEW.`appointment_date` IS NOT NULL THEN
        SET `appt_year` = CAST(YEAR(NEW.`appointment_date`) AS CHAR);
    ELSE
        SET `appt_year` = CAST(YEAR(CURDATE()) AS CHAR);
    END IF;
    
    SET `pattern` = CONCAT('APT-', `appt_year`, '-%');
    
    SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(`appointment_number`, '-', -1) AS UNSIGNED)), 0) + 1
    INTO `next_seq`
    FROM `appointments`
    WHERE `appointment_number` LIKE `pattern`;
    
    SET NEW.`appointment_number` = CONCAT('APT-', `appt_year`, '-', LPAD(`next_seq`, 4, '0'));
END$$
DELIMITER ;

-- =====================================================================
-- 11. STORED PROCEDURE: CalculateInvoiceTotal
-- Calculates treatment cost, standard clinic consultation fee, and total amount.
-- =====================================================================
DELIMITER $$
CREATE PROCEDURE `CalculateInvoiceTotal`(
    IN `p_appointment_id` INT,
    OUT `p_treatment_cost` DECIMAL(10,2),
    OUT `p_consultation_fee` DECIMAL(10,2),
    OUT `p_total` DECIMAL(10,2)
)
BEGIN
    DECLARE `t_cost` DECIMAL(10,2) DEFAULT 0.00;
    DECLARE `c_fee` DECIMAL(10,2) DEFAULT 1500.00; -- Standard clinic consultation fee
    
    SELECT t.`cost` INTO `t_cost`
    FROM `appointments` a
    JOIN `treatments` t ON a.`treatment_id` = t.`id`
    WHERE a.`id` = `p_appointment_id`;
    
    SET `p_treatment_cost` = COALESCE(`t_cost`, 0.00);
    SET `p_consultation_fee` = `c_fee`;
    SET `p_total` = `p_treatment_cost` + `p_consultation_fee`;
    
    -- Also return a result set for direct querying
    SELECT 
        `p_appointment_id` AS `appointment_id`,
        `p_consultation_fee` AS `consultation_fee`,
        `p_treatment_cost` AS `treatment_cost`,
        `p_total` AS `total_amount`;
END$$
DELIMITER ;

-- =====================================================================
-- 12. SEED DATA
-- Default passwords are SHA-256 hashes of 'admin123'
-- Hash: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- =====================================================================

-- Users
INSERT INTO `users` (`id`, `username`, `password_hash`, `role`, `full_name`, `email`) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator', 'Dr. Aruna Bandara (Admin)', 'admin@sunrisedental.lk'),
(2, 'receptionist1', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Receptionist', 'Kavindi Senanayake', 'kavindi@sunrisedental.lk'),
(3, 'dr.silva', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Dentist', 'Dr. Samantha Silva', 'samantha.silva@sunrisedental.lk'),
(4, 'dr.perera', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Dentist', 'Dr. Rohan Perera', 'rohan.perera@sunrisedental.lk'),
(5, 'dr.fernando', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Dentist', 'Dr. Kasun Fernando', 'kasun.fernando@sunrisedental.lk');

-- Dentists
INSERT INTO `dentists` (`id`, `user_id`, `full_name`, `specialization`, `contact_number`, `email`) VALUES
(1, 3, 'Dr. Samantha Silva', 'Orthodontics & Cosmetic Dentistry', '0771122334', 'samantha.silva@sunrisedental.lk'),
(2, 4, 'Dr. Rohan Perera', 'Periodontics & General Dentistry', '0772233445', 'rohan.perera@sunrisedental.lk'),
(3, 5, 'Dr. Kasun Fernando', 'Endodontics & Oral Surgery', '0773344556', 'kasun.fernando@sunrisedental.lk');

-- Treatments
INSERT INTO `treatments` (`id`, `type`, `cost`, `description`) VALUES
(1, 'General Consultation & Examination', 1500.00, 'Comprehensive oral examination, diagnostic review, and treatment planning.'),
(2, 'Dental Scaling & Polishing', 3500.00, 'Full mouth ultrasonic tartar removal and tooth surface polishing.'),
(3, 'Composite Tooth Filling', 4500.00, 'Tooth-coloured light-cured composite resin restoration.'),
(4, 'Root Canal Treatment (RCT)', 15000.00, 'Single/multi-canal endodontic therapy with disinfection and obturation.'),
(5, 'Simple Tooth Extraction', 3000.00, 'Local anaesthesia extraction of non-restorable tooth.'),
(6, 'Teeth Whitening (Bleaching)', 12000.00, 'Professional in-office laser/LED dental whitening.'),
(7, 'Porcelain Crown Restoration', 25000.00, 'High-grade porcelain-fused-to-metal / zirconia dental crown.'),
(8, 'Orthodontic Braces Adjustment', 5000.00, 'Monthly orthodontic archwire replacement and bracket adjustments.');

-- Patients
INSERT INTO `patients` (`id`, `name`, `address`, `contact_number`, `email`) VALUES
(1, 'Kamal Jayawardena', 'No. 45, Galle Road, Colombo 03', '0771234567', 'kamal.j@gmail.com'),
(2, 'Nimali Perera', 'No. 12, Kandy Road, Kiribathgoda', '0719876543', 'nimali.p@yahoo.com'),
(3, 'Sunil Wickramasinghe', 'No. 88, High Level Road, Nugegoda', '0765554321', 'sunil.w@gmail.com'),
(4, 'Fatima Razik', 'No. 23, Main Street, Dehiwala', '0721112233', 'fatima.r@gmail.com');

-- Sample Appointments (Triggers will auto-generate APT-2026-0001, APT-2026-0002, etc.)
INSERT INTO `appointments` (`patient_id`, `dentist_id`, `treatment_id`, `appointment_date`, `appointment_time`, `status`, `notes`) VALUES
(1, 1, 2, '2026-09-02', '09:00:00', 'SCHEDULED', 'Routine scaling requested'),
(2, 2, 3, '2026-09-02', '10:00:00', 'SCHEDULED', 'Upper molar sensitivity'),
(3, 3, 4, '2026-09-02', '11:00:00', 'SCHEDULED', 'First stage root canal');
