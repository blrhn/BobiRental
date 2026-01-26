TRUNCATE TABLE tool, employee, client, rental_agreement, tool_event, fee RESTART IDENTITY CASCADE;

-- Sprzet budowlany
INSERT INTO tool (tool_name, tool_availability_status, tool_description, tool_category, tool_price) VALUES
-- Wiertarki
    ('Wiertarka udarowa Makita', 'AVAILABLE', 'Wiertarka udarowa 850W, idealna do betonu', 'DRILL', 350.00),
    ('Wiertarka akumulatorowa DeWalt', 'AVAILABLE', 'Bezprzewodowa wiertarka 18V z dwoma akumulatorami', 'DRILL', 450.00),
    ('Wiertarka stołowa Bosch', 'RENTED', 'Wiertarka kolumnowa do precyzyjnych prac', 'DRILL', 1200.00),
    ('Wiertarka magnetyczna', 'AVAILABLE', 'Do wiercenia w stalowych konstrukcjach', 'DRILL', 800.00),

-- Piły
    ('Piła tarczowa Metabo', 'AVAILABLE', 'Piła tarczowa 1600W, głębokość cięcia 70mm', 'SAW', 550.00),
    ('Piła łańcuchowa Stihl', 'RENTED', 'Profesjonalna piła spalinowa do drewna', 'SAW', 900.00),
    ('Piła szablasta Bosch', 'AVAILABLE', 'Uniwersalna piła do różnych materiałów', 'SAW', 380.00),
    ('Piła taśmowa stacjonarna', 'UNAVAILABLE', 'Wymaga naprawy - uszkodzony silnik', 'SAW', 1500.00),
    ('Piła ukosowa Makita', 'AVAILABLE', 'Z laserem, precyzyjne cięcia pod kątem', 'SAW', 780.00),

-- Szlifierki/Przecinarki
    ('Szlifierka kątowa 125mm', 'AVAILABLE', 'Mała szlifierka 850W do prac wykończeniowych', 'CUTTER', 280.00),
    ('Szlifierka kątowa 230mm', 'RENTED', 'Duża szlifierka 2200W do ciężkich prac', 'CUTTER', 520.00),
    ('Przecinarka do betonu', 'AVAILABLE', 'Profesjonalna przecinarka spalinowa', 'CUTTER', 1800.00),
    ('Szlifierka do parkietu', 'AVAILABLE', 'Specjalistyczna maszyna do drewna', 'CUTTER', 650.00),
    ('Przecinarka plazmowa', 'UNAVAILABLE', 'W serwisie - wymiana dysz', 'CUTTER', 2500.00),
    ('Flex 230mm Hilti', 'AVAILABLE', 'Wysokiej jakości szlifierka kątowa', 'CUTTER', 680.00);


-- Pracownicy
INSERT INTO employee (employee_name, employee_surname, employee_role, employee_login, employee_password) VALUES
    ('Jan', 'Kowalski', 'WAREHOUSE_MANAGER', 'j.kowalski', 'haslo123'),
    ('Anna', 'Nowak', 'REGULAR_EMPLOYEE', 'a.nowak', 'anna2024'),
    ('Piotr', 'Wiśniewski', 'REGULAR_EMPLOYEE', 'p.wisniewski', 'piotr456'),
    ('Maria', 'Dąbrowska', 'WAREHOUSE_MANAGER', 'm.dabrowska', 'maria789'),
    ('Tomasz', 'Lewandowski', 'REGULAR_EMPLOYEE', 't.lewandowski', 'tomek321');


-- Klienci
INSERT INTO client (client_name, client_surname, client_address, client_mail, client_has_duty, client_removal_date) VALUES
    ('Marek', 'Kowalczyk', 'ul. Długa 15, 00-001 Warszawa', 'marek.kowal@email.pl', FALSE, CURRENT_DATE + INTERVAL '1 year'),
    ('Zofia', 'Szymańska', 'ul. Krótka 8/12, 30-002 Kraków', 'zofia.szy@gmail.com', FALSE, CURRENT_DATE + INTERVAL '1 year'),
    ('Andrzej', 'Woźniak', 'ul. Polna 44, 80-003 Gdańsk', 'a.wozniak@firma.pl', FALSE, CURRENT_DATE + INTERVAL '6 months'),
    ('Katarzyna', 'Kamińska', 'os. Słoneczne 7, 50-004 Wrocław', 'kasia.kam@interia.pl', FALSE, CURRENT_DATE + INTERVAL '1 year'),
    ('Michał', 'Zieliński', 'ul. Jasna 23, 60-005 Poznań', 'michal.z@outlook.com', FALSE, CURRENT_DATE + INTERVAL '1 year'),
    ('Ewa', 'Mazur', 'ul. Ciemna 9/3, 90-006 Łódź', 'ewa.mazur@wp.pl', FALSE, CURRENT_DATE + INTERVAL '3 months'),
    ('Paweł', 'Król', 'ul. Stara 56, 20-007 Lublin', 'pawel.krol@yahoo.com', FALSE, CURRENT_DATE + INTERVAL '1 year'),
    ('Agnieszka', 'Wojciechowska', 'ul. Nowa 31, 40-008 Katowice', 'agnes.w@gmail.com', FALSE, CURRENT_DATE + INTERVAL '1 year');


-- Umowy
-- Zakończone umowy
INSERT INTO rental_agreement (agreement_execution_date, agreement_estimated_termination_date, agreement_actual_termination_date, is_agreement_terminated, client_id, tool_id, employee_id, agreement_comment) VALUES
    (CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE - INTERVAL '38 days', CURRENT_DATE - INTERVAL '38 days', TRUE, 1, 1, 2, 'Wynajem na remont mieszkania'),
    (CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '23 days', CURRENT_DATE - INTERVAL '20 days', TRUE, 2, 5, 3, 'Budowa tarasu'),
    (CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '53 days', CURRENT_DATE - INTERVAL '50 days', TRUE, 4, 7, 2, 'Prace wykończeniowe');

-- Aktywne umowy (w trakcie)
INSERT INTO rental_agreement (agreement_execution_date, agreement_estimated_termination_date, agreement_actual_termination_date, is_agreement_terminated, client_id, tool_id, employee_id, agreement_comment) VALUES
    (CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '9 days', NULL, FALSE, 5, 3, 3, 'Wiercenie w betonie - budowa garażu'),
    (CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE + INTERVAL '11 days', NULL, FALSE, 7, 6, 5, 'Cięcie drewna na budowie'),
    (CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE + INTERVAL '6 days', NULL, FALSE, 1, 11, 2, 'Szlifowanie powierzchni metalowych'),
    (CURRENT_DATE, CURRENT_DATE + INTERVAL '7 days', NULL, FALSE, 8, 9, 3, 'Cięcia precyzyjne - stolarka'),
    (CURRENT_DATE, CURRENT_DATE + INTERVAL '14 days', NULL, FALSE, 2, 12, 5, 'Cięcie betonu na budowie');


-- Historia sprzętu
INSERT INTO tool_event (event_category, event_date, event_comment, tool_id, employee_id) VALUES
-- Historia wiertarki Makita
    ('CREATION', CURRENT_DATE - INTERVAL '180 days', 'Zakup nowego sprzętu', 1, 1),
    ('UPDATE', CURRENT_DATE - INTERVAL '38 days', 'Zamknięcie umowy ze sprzętem', 1, 2),

-- Historia wiertarki DeWalt
    ('CREATION', CURRENT_DATE - INTERVAL '150 days', 'Zakup nowego sprzętu', 2, 1),

-- Historia wiertarki stołowej (obecnie wypożyczona)
    ('CREATION', CURRENT_DATE - INTERVAL '200 days', 'Zakup nowego sprzętu', 3, 1),

-- Historia piły tarczowej
    ('CREATION', CURRENT_DATE - INTERVAL '160 days', 'Zakup nowego sprzętu', 5, 1),
    ('UPDATE', CURRENT_DATE - INTERVAL '20 days', 'Zamknięcie umowy ze sprzętem', 5, 3),

-- Historia piły łańcuchowej
    ('CREATION', CURRENT_DATE - INTERVAL '190 days', 'Zakup nowego sprzętu', 6, 1),

-- Historia piły taśmowej (uszkodzona)
    ('CREATION', CURRENT_DATE - INTERVAL '220 days', 'Zakup nowego sprzętu', 8, 1),
    ('UPDATE', CURRENT_DATE - INTERVAL '10 days', 'Awaria silnika - przekazano do naprawy', 8, 1),

-- Historia szlifierki 230mm
    ('CREATION', CURRENT_DATE - INTERVAL '140 days', 'Zakup nowego sprzętu', 11, 1),

-- Historia przecinarki plazmowej
    ('CREATION', CURRENT_DATE - INTERVAL '250 days', 'Zakup nowego sprzętu - profesjonalny model', 14, 1),
    ('UPDATE', CURRENT_DATE - INTERVAL '7 days', 'Zużycie dysz - wysłano do serwisu', 14, 4);


-- Oplaty
-- Opłaty za zakończone umowy (zapłacone)
INSERT INTO fee (fee_category, agreement_id, client_id, employee_id, actual_fee, is_fee_paid, fee_duty_date, fee_finalized_date) VALUES
    ('RENTAL_FEE', 1, 1, 2, 49.00, TRUE, CURRENT_DATE - INTERVAL '38 days', CURRENT_DATE - INTERVAL '36 days'),
    ('RENTAL_FEE', 2, 2, 3, 77.00, TRUE, CURRENT_DATE - INTERVAL '23 days', CURRENT_DATE - INTERVAL '20 days'),
    ('RENTAL_FEE', 3, 4, 2, 53.20, TRUE, CURRENT_DATE - INTERVAL '53 days', CURRENT_DATE - INTERVAL '50 days');

-- Opłaty za aktywne umowy (jeszcze nieopłacone)
INSERT INTO fee (fee_category, agreement_id, client_id, employee_id, actual_fee, is_fee_paid, fee_duty_date, fee_finalized_date) VALUES
    ('RENTAL_FEE', 4, 5, 3, 336.00, FALSE, CURRENT_DATE + INTERVAL '9 days', NULL),
    ('RENTAL_FEE', 5, 7, 5, 252.00, FALSE, CURRENT_DATE + INTERVAL '11 days', NULL),
    ('RENTAL_FEE', 6, 1, 2, 72.80, FALSE, CURRENT_DATE + INTERVAL '6 days', NULL),
    ('RENTAL_FEE', 7, 8, 3, 109.20, FALSE, CURRENT_DATE + INTERVAL '7 days', NULL),
    ('RENTAL_FEE', 8, 2, 5, 182.00, FALSE, CURRENT_DATE + INTERVAL '14 days', NULL);

-- Opłaty karne (przeterminowane - dla klientów z zaległościami)
    -- INSERT INTO fee (fee_category, agreement_id, client_id, employee_id, actual_fee, is_fee_paid, fee_duty_date, fee_finalized_date) VALUES
    -- ('PENALTY', 2, 2, 3, 50.00, FALSE, CURRENT_DATE - INTERVAL '20 days', NULL);

-- Dodatkowe zaległe opłaty dla klienta ID 3 (Andrzej Woźniak - ma client_has_duty = TRUE)
INSERT INTO fee (fee_category, agreement_id, client_id, employee_id, actual_fee, is_fee_paid, fee_duty_date, fee_finalized_date) VALUES
    -- ('RENTAL_FEE', 3, 3, 2, 150.00, FALSE, CURRENT_DATE - INTERVAL '15 days', NULL),
    ('PENALTY', 3, 3, 2, 75.00, FALSE, CURRENT_DATE + INTERVAL '15 days', NULL);

-- Dodatkowe zaległe opłaty dla klienta ID 6 (Ewa Mazur - ma client_has_duty = TRUE)
-- INSERT INTO fee (fee_category, agreement_id, client_id, employee_id, actual_fee, is_fee_paid, fee_duty_date, fee_finalized_date) VALUES
--     ('RENTAL_FEE', 3, 6, 5, 200.00, FALSE, CURRENT_DATE - INTERVAL '20 days', NULL);

UPDATE tool
SET tool_availability_status = 'AVAILABLE'
WHERE tool_id IN (1, 2, 4, 5, 7, 9, 10, 12);