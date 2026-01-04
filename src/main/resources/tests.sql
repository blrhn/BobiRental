-- Integralność semantyczna

-- Próba wstawienia sprzętu z niedodatnią ceną
DO $$
    BEGIN
        INSERT INTO tool (tool_name, tool_category, tool_price)
        VALUES ('Narzędzie testowe', 'DRILL', -100.00);
        VALUES ('Narzędzie testowe', 'DRILL', 0.00);
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'Poprawne odrzucenie niedodatniej ceny';
END $$;

-- Czy są jakieś sprzęty z niedodatnią ceną
SELECT tool_id, tool_name, tool_price
FROM tool
WHERE tool_price <= 0;

-- Próba wstawienia niedodatniej opłaty
DO $$
    BEGIN
        INSERT INTO fee (fee_category, agreement_id, client_id, employee_id, actual_fee, fee_duty_date)
        VALUES ('RENTAL_FEE', 1, 1, 1, -50.00, CURRENT_DATE);
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'Poprawne odrzucenie niedodatniej oplaty';
END $$;

-- Czy są jakieś opłaty niedodatnie
SELECT fee_id, actual_fee
FROM fee
WHERE actual_fee <= 0;

-- Próba utworzenia umowy z nieprawidłowymi datami
DO $$
    BEGIN
        INSERT INTO rental_agreement (agreement_execution_date, agreement_estimated_termination_date, client_id, tool_id, employee_id)
        VALUES (CURRENT_DATE, CURRENT_DATE - INTERVAL '5 days', 1, 1, 1);
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'Poprawne odrzucenie umowy z niepoprawnymi datami';
END $$;

-- Sprawdzenie poprawności daty zawarcia i oszacowanego zamknięcia umowy
SELECT agreement_id, agreement_execution_date, agreement_estimated_termination_date
FROM rental_agreement
WHERE agreement_estimated_termination_date < agreement_execution_date;

-- Próba utworzenia umowy na dłużej niż 90 dni
DO $$
    BEGIN
        INSERT INTO rental_agreement (agreement_execution_date, agreement_estimated_termination_date, client_id, tool_id, employee_id)
        VALUES (CURRENT_DATE, CURRENT_DATE + INTERVAL '100 days', 1, 1, 1);
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'Poprawne odrzucenie umowy na dłużej niż 90 dni';
END $$;

-- Integralność encji

-- Czy wszystkie primary key są unikalne i not null
SELECT tool_id, COUNT(*)
FROM tool
GROUP BY tool_id
HAVING COUNT(*) > 1;

SELECT employee_id, COUNT(*)
FROM employee
GROUP BY employee_id
HAVING COUNT(*) > 1;

SELECT client_id, COUNT(*)
FROM client
GROUP BY client_id
HAVING COUNT(*) > 1;

SELECT agreement_id, COUNT(*)
FROM rental_agreement
GROUP BY agreement_id
HAVING COUNT(*) > 1;

SELECT event_id, COUNT(*)
FROM tool_event
GROUP BY event_id
HAVING COUNT(*) > 1;

SELECT fee_id, COUNT(*)
FROM fee
GROUP BY fee_id
HAVING COUNT(*) > 1;

-- Próba wstawienia pracownika z już istniejącym loginem
DO $$
    BEGIN
        INSERT INTO employee (employee_name, employee_surname, employee_role, employee_login, employee_password)
        VALUES ('Test', 'Test', 'REGULAR_EMPLOYEE', 'j.kowalski', 'test123');
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'Poprawne odrzucenie pracownika z już istniejącym loginem';
END $$;

-- Czy jest jakiś duplikat istniejących loginów
SELECT employee_login, COUNT(*)
FROM employee
GROUP BY employee_login
HAVING COUNT(*) > 1;

-- Integralność referencji

-- Próba wstawienia umowy z nieistniejącym narzędziem
DO $$
    BEGIN
        INSERT INTO rental_agreement (agreement_estimated_termination_date, client_id, tool_id, employee_id)
        VALUES (CURRENT_DATE + INTERVAL '7 days', 1, 99999, 1);
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE 'Poprawne odrzucenie umowy z nieistniejącym narzędziem';
END $$;

-- Czy jakiś client_id w rental_agreement nie istnieje w client
SELECT ra.agreement_id, ra.client_id
FROM rental_agreement ra
         LEFT JOIN client c ON ra.client_id = c.client_id
WHERE c.client_id IS NULL;

-- Czy jakiś tool_id w rental_agreement nie istnieje w tool
SELECT ra.agreement_id, ra.tool_id
FROM rental_agreement ra
         LEFT JOIN tool t ON ra.tool_id = t.tool_id
WHERE t.tool_id IS NULL;

-- Czy jakiś employee_id w rental_agreement nie istnieje w employee
SELECT ra.agreement_id, ra.employee_id
FROM rental_agreement ra
         LEFT JOIN employee e ON ra.employee_id = e.employee_id
WHERE e.employee_id IS NULL;

-- Czy jakiś tool_id w tool_event nie istnieje w tool
SELECT te.event_id, te.tool_id
FROM tool_event te
         LEFT JOIN tool t ON te.tool_id = t.tool_id
WHERE t.tool_id IS NULL;

-- Czy jakiś employee_id w tool_event nie istnieje w employee
SELECT te.event_id, te.employee_id
FROM tool_event te
         LEFT JOIN employee e ON te.employee_id = e.employee_id
WHERE e.employee_id IS NULL;

-- Czy jakiś agreement_id w fee nie istnieje w rental_agreement
SELECT f.fee_id, f.agreement_id
FROM fee f
         LEFT JOIN rental_agreement ra ON f.agreement_id = ra.agreement_id
WHERE ra.agreement_id IS NULL;

-- Czy jakiś client_id w fee nie istnieje w client
SELECT f.fee_id, f.client_id
FROM fee f
         LEFT JOIN client c ON c.client_id = f.client_id
WHERE c.client_id IS NULL;

-- Czy jakiś employee_id w fee nie istnieje w employee
SELECT f.fee_id, f.employee_id
FROM fee f
         LEFT JOIN employee e ON e.employee_id = f.employee_id
WHERE e.employee_id IS NULL;

-- Funkcje

-- Suma opłat klienta o id 1 - 121.8
SELECT total_client_fees(1);

-- Suma opłat klienta o id 2 - 309
SELECT total_client_fees(2);

-- Klienci z zaległościami (id: 3 i 6)
SELECT
    c.client_id,
    c.client_name, c.client_surname AS client,
    calculate_client_debt(c.client_id) AS debt
FROM client c
WHERE c.client_has_duty = TRUE;

-- Test poprawności dat
-- Poprawna
SELECT validate_agreement_dates(CURRENT_DATE, CURRENT_DATE + 14);

-- Niepoprawna
SELECT validate_agreement_dates(CURRENT_DATE, CURRENT_DATE - 14);

-- Niepoprawna (dłużej niż 90 dni)
SELECT validate_agreement_dates(CURRENT_DATE, CURRENT_DATE + 91);

-- Którzy klienci mogą lub nie mogą zawrzeć nowej umowy
SELECT
    c.client_id,
    c.client_name, c.client_surname AS client,
    can_client_create_agreement(c.client_id),
    c.client_has_duty
FROM client c
ORDER BY c.client_id;

-- TRIGGERY

-- Zmiana statusu sprzętu po utworzeniu umowy
DO $$
    DECLARE
        t_id INT; -- tool_id
        status availability_status;
    BEGIN
        -- Znalezienie dostępnego narzędzia
        SELECT t.tool_id
        INTO t_id
        FROM tool t
        WHERE tool_availability_status = 'AVAILABLE'
        LIMIT 1;

        -- Utworzenie umowy
        INSERT INTO rental_agreement (agreement_estimated_termination_date, client_id, tool_id, employee_id)
        VALUES (CURRENT_DATE + INTERVAL '7 days', 1, t_id, 1);

        SELECT tool_availability_status
        INTO status
        FROM tool t
        WHERE t_id = t.tool_id;

        IF status = 'RENTED' THEN
            RAISE NOTICE 'Status sprzętu został poprawnie zmieniony na wypożyczony';
        END IF;

        ROLLBACK;
END $$;

-- Przedłużenie daty usunięcia klienta
DO  $$
    DECLARE
        old_date DATE;
        new_date DATE;
        c_id INT := 3; -- client_id
    BEGIN
        SELECT client_removal_date
        INTO old_date
        FROM client
        WHERE client_id = c_id;

        -- Utworzenie nowej umowy
        INSERT INTO rental_agreement (agreement_estimated_termination_date, client_id, tool_id, employee_id)
        VALUES (CURRENT_DATE + INTERVAL '7 days', c_id, 1, 1);

        -- Sprawdzenie nowej daty
        SELECT client_removal_date
        INTO new_date
        FROM client
        WHERE client_id = c_id;

        IF new_date >= old_date THEN
            RAISE NOTICE 'Data usunięcia prawidłowo przedłużona z % na %', old_date, new_date;
        END IF;

        ROLLBACK;
END $$;

-- Utworzenie umowy dla niedostępnego sprzętu
DO $$
    DECLARE
        rented_tool INT;
    BEGIN
        -- Znalezienie wypożyczonego sprzętu
        SELECT tool_id
        INTO rented_tool
        FROM tool
        WHERE tool_availability_status = 'RENTED'
        LIMIT 1;

        -- Próba utworzenia umowy funkcją
        PERFORM create_rental_agreement_func(1, rented_tool, 1, CURRENT_DATE + 7, 'Test');

        EXCEPTION WHEN raise_exception THEN
            RAISE NOTICE 'Poprawne niedopuszczenie do wypożyczenia niedostępnego sprzętu';
END $$;

-- Procedury

-- Zamknięcie umowy bez kary
DO $$
    DECLARE
        ag_id INT; -- agreement_id
        fee_count INT;
    BEGIN
        -- Utworzenie testowej umowy
        SELECT create_rental_agreement_func(1, 2, 1, CURRENT_DATE + 7, 'Test')
        INTO ag_id;

        -- Zamknięcie umowy w terminie
        UPDATE rental_agreement
        SET agreement_actual_termination_date = CURRENT_DATE + INTERVAL '5 days'
        WHERE agreement_id = ag_id;

        CALL close_agreement(ag_id, 1);

        SELECT COUNT(*)
        INTO fee_count
        FROM fee
        WHERE agreement_id = ag_id AND fee_category = 'PENALTY';

        IF fee_count = 0 THEN
            RAISE NOTICE 'Poprawnie nie nałożono kary';
        END IF;

        ROLLBACK;
END $$;

-- Zamknięcie umowy z karą
DO $$
    DECLARE
        ag_id INT; -- agreement_id
        fee_count INT;
    BEGIN
        -- Utworzenie testowej umowy
        SELECT create_rental_agreement_func(1, 2, 1, CURRENT_DATE + 4, 'Test')
        INTO ag_id;

        -- Zamknięcie umowy w terminie
        UPDATE rental_agreement
        SET agreement_actual_termination_date = CURRENT_DATE + INTERVAL '5 days'
        WHERE agreement_id = ag_id;

        CALL close_agreement(ag_id, 1);

        SELECT COUNT(*)
        INTO fee_count
        FROM fee
        WHERE agreement_id = ag_id AND fee_category = 'PENALTY';

        IF fee_count > 0 THEN
            RAISE NOTICE 'Poprawnie nałożono karę';
        END IF;

        ROLLBACK;
END $$;

-- Próba zamknięcia nieistniejącej umowy
DO $$
    BEGIN
        CALL close_agreement(99999, 1);
        EXCEPTION WHEN raise_exception THEN
            RAISE NOTICE 'Poprawne nieudane zamknięcie nieistniejącej umowy';
END $$;

-- Próba podwójnego zamknięcia umowy
DO $$
    DECLARE
        closed_id INT;
    BEGIN
        SELECT agreement_id
        INTO closed_id
        FROM rental_agreement
        WHERE is_agreement_terminated = TRUE
        LIMIT 1;

        CALL close_agreement(closed_id, 1);

    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'Poprawne nieudane zamknięcie już zakończonej umowy';
END $$;

-- Role

-- Sprawdzenie utworzonych ról
SELECT rolname
FROM pg_roles
WHERE rolname IN ('employee', 'warehouse_manager')
ORDER BY rolname;

-- Rola employee; dodanie nowego narzędzia
DO $$
    BEGIN
        SET ROLE employee;
        INSERT INTO tool (tool_name, tool_category, tool_price)
        VALUES ('Tool', 'DRILL', 100.00);
        EXCEPTION WHEN insufficient_privilege THEN
            RAISE NOTICE 'Poprawne niedopuszczenie pracownika do wstawienia nowego narzędzia';

        RESET ROLE;
END $$;

-- Rola employee; usunięcie klienta
DO $$
    BEGIN
        SET ROLE employee;
        DELETE FROM client WHERE client_id = 1;
        EXCEPTION WHEN insufficient_privilege THEN
            RAISE NOTICE 'Poprawne niedopuszczenie pracownika do usunięcia klienta';

        RESET ROLE;
END $$;

-- Rola employee; usunięcie umowy
DO $$
    BEGIN
        SET ROLE employee;
        DELETE FROM rental_agreement
        WHERE agreement_id = 1;
        EXCEPTION WHEN insufficient_privilege THEN
            RAISE NOTICE 'Poprawne niedopuszczenie pracownika do usunięcia umowy';

        RESET ROLE;
END $$;

-- Rola employee; aktualizacja tool_event
DO $$
    BEGIN
        SET ROLE employee;
        UPDATE tool_event
        SET event_comment = 'TEST'
        WHERE event_id = 1;
        EXCEPTION WHEN insufficient_privilege THEN
            RAISE NOTICE 'Poprawne niedopuszczenie pracownika do aktualizacji tool_event';

        RESET ROLE;
END $$;

-- Rola employee; usunięcie opłaty
DO $$
    BEGIN
        SET ROLE employee;
        DELETE FROM fee
        WHERE fee_id = 1;
    EXCEPTION WHEN insufficient_privilege THEN
        RAISE NOTICE 'Poprawne niedopuszczenie pracownika do usunięcia opłaty';

        RESET ROLE;
END $$;