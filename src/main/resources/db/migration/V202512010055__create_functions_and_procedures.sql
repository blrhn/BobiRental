-- Funkcja zarządzająca uprawnieniami pracowników (INSERT i UPDATE)
CREATE OR REPLACE FUNCTION manage_employee_permissions()
    RETURNS TRIGGER AS $$
BEGIN
    -- Dla operacji INSERT
    IF TG_OP = 'INSERT' THEN
        -- Tworzenie nowego użytkownika jeśli nie istnieje
        IF NOT EXISTS (
            SELECT 1 FROM pg_roles WHERE rolname = NEW.employee_login
        ) THEN
            EXECUTE format('CREATE USER %I WITH PASSWORD %L',
                           NEW.employee_login,
                           NEW.employee_password);
            RAISE NOTICE 'Created new database user: %', NEW.employee_login;
        END IF;

        -- Dla operacji UPDATE
    ELSIF TG_OP = 'UPDATE' THEN
        -- Jeśli zmienił się login, zmień nazwę użytkownika
        IF OLD.employee_login != NEW.employee_login THEN
            EXECUTE format('ALTER USER %I RENAME TO %I',
                           OLD.employee_login,
                           NEW.employee_login);
            RAISE NOTICE 'Renamed database user from % to %', OLD.employee_login, NEW.employee_login;
        END IF;

        -- Jeśli zmieniło się hasło, zaktualizuj je
        IF OLD.employee_password != NEW.employee_password THEN
            EXECUTE format('ALTER USER %I WITH PASSWORD %L',
                           NEW.employee_login,
                           NEW.employee_password);
            RAISE NOTICE 'Updated password for user: %', NEW.employee_login;
        END IF;
    END IF;

    -- Usuń wszystkie istniejące role (czyścimy przed ponownym przypisaniem)
    BEGIN
        EXECUTE format('REVOKE employee FROM %I', NEW.employee_login);
        EXECUTE format('REVOKE warehouse_manager FROM %I', NEW.employee_login);
    EXCEPTION WHEN OTHERS THEN NULL;
    END;

    -- Przypisz odpowiednią rolę na podstawie employee_role
    IF NEW.employee_role = 'REGULAR_EMPLOYEE' THEN
        EXECUTE format('GRANT employee TO %I', NEW.employee_login);
        RAISE NOTICE 'Assigned EMPLOYEE role to user: %', NEW.employee_login;

    ELSIF NEW.employee_role = 'WAREHOUSE_MANAGER' THEN
        EXECUTE format('GRANT warehouse_manager TO %I', NEW.employee_login);
        RAISE NOTICE 'Assigned WAREHOUSE_MANAGER role to user: %', NEW.employee_login;

    ELSE
        RAISE EXCEPTION 'Unknown employee role: %', NEW.employee_role;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger dla INSERT i UPDATE
CREATE TRIGGER trg_manage_employee_permissions
    AFTER INSERT OR UPDATE OF employee_role, employee_login, employee_password ON employee
    FOR EACH ROW
EXECUTE FUNCTION manage_employee_permissions();

-- Komentarz:
-- Automatycznie aktualizuje uprawnienia gdy zmienia się rola pracownika.
-- Obsługuje zmianę loginu i hasła.
-- Zapewnia automatyczne zarządzanie uprawnieniami.


-- Funkcja obliczająca łączna sumę opłat klienta
CREATE OR REPLACE FUNCTION total_client_fees(p_client_id INT)
    RETURNS NUMERIC(10,2) AS $$
SELECT COALESCE(SUM(actual_fee), 0)
FROM fee
WHERE client_id = p_client_id;
$$ LANGUAGE sql;

-- Komentarz:
-- Funkcja sumuje wszystkie opłaty przypisane do danego klienta i zwraca wartosc.

-- Funkcja obliczająca sumę zaległych opłat klienta
CREATE OR REPLACE FUNCTION calculate_client_debt(p_client_id INT)
    RETURNS NUMERIC(10,2) AS $$
SELECT COALESCE(SUM(actual_fee), 0)
FROM fee
WHERE client_id = p_client_id
  AND is_fee_paid = FALSE
  AND fee_duty_date < CURRENT_DATE;
$$ LANGUAGE sql;

-- Komentarz:
-- Funkcja oblicza aktualną kwotę zaległych opłat klienta.
-- Zwraca tylko niezapłacone opłaty, których termin minął.

-- Funkcja aktualizująca flagę client_has_duty w zależności od aktualnego długu
CREATE OR REPLACE FUNCTION update_client_has_duty()
    RETURNS TRIGGER AS $$
DECLARE
    c_id INT; -- client_id
    has_debt BOOLEAN;
    debt_amount NUMERIC;
BEGIN
    -- Przypisanie odpowiedniego client_id w zależności od wykonywanej operacji
    IF TG_OP = 'DELETE' THEN
        c_id := OLD.client_id;
    ELSE
        c_id := NEW.client_id;
    END IF;

    SELECT calculate_client_debt(c_id)
    INTO debt_amount;
    has_debt := (debt_amount > 0);

    UPDATE client AS c
    SET client_has_duty = has_debt
    WHERE c.client_id = c_id
    AND client_has_duty != has_debt;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;


--Trigger do aktualizacji flagi client_has_duty
CREATE TRIGGER trg_update_client_has_duty
    AFTER INSERT OR UPDATE OF is_fee_paid, fee_duty_date, client_id OR DELETE
    ON fee
    FOR EACH ROW
EXECUTE FUNCTION update_client_has_duty();


-- Funkcja przedłużająca datę usunięcia klienta po wstawieniu umowy
CREATE OR REPLACE FUNCTION extend_client_removal()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE client
    SET client_removal_date = (CURRENT_DATE + INTERVAL '1 year')
    WHERE client_id = NEW.client_id;

    RAISE NOTICE 'Extended client % until %', NEW.client_id, (CURRENT_DATE + INTERVAL '1 year');

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Trigger wykonujący funkcję przedłużającą datę usunięcia klienta
CREATE TRIGGER trg_extend_client_removal
    AFTER INSERT ON rental_agreement
    FOR EACH ROW
EXECUTE FUNCTION extend_client_removal();

-- Komentarz:
-- Wraz z rozpoczęciem nowej umowy, data usunięcia klienta zostaje odpowiednio
-- przedłużona


-- Procedura zamykająca umowę wynajmu z automatycznym naliczaniem kary za opóźnienie
CREATE OR REPLACE PROCEDURE close_agreement(
    p_agreement_id INT,
    p_employee_id INT
)
    LANGUAGE plpgsql
AS $$
DECLARE
    end_date DATE;
    due_date DATE;
    exec_date DATE;
    c_id INT;
    tool_price NUMERIC(10,2);
    days_late INT;
    rental_days INT;
    penalty_amount NUMERIC(10,2);
    rental_fee NUMERIC(10,2);
BEGIN
    -- Pobranie danych umowy wraz z ceną narzędzia
    SELECT ra.agreement_actual_termination_date,
           ra.agreement_estimated_termination_date,
           ra.agreement_execution_date,
           ra.client_id,
           t.tool_price
    INTO end_date, due_date, exec_date, c_id, tool_price
    FROM rental_agreement ra
             JOIN tool t ON ra.tool_id = t.tool_id
    WHERE ra.agreement_id = p_agreement_id;

    SELECT actual_fee
    INTO rental_fee
    FROM fee
    WHERE agreement_id = p_agreement_id AND fee_category = 'RENTAL_FEE';

    -- Sprawdzenie czy umowa istnieje
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Agreement with ID % does not exist', p_agreement_id;
    END IF;

    -- Sprawdzenie czy umowa nie jest już zamknieta
    IF EXISTS (SELECT 1 FROM rental_agreement WHERE agreement_id = p_agreement_id AND is_agreement_terminated = TRUE) THEN
        RAISE EXCEPTION 'Agreement with ID % is already terminated', p_agreement_id;
    END IF;

    -- Sprawdzenie czy pracownik istnieje
    IF NOT EXISTS (SELECT 1 FROM employee WHERE employee_id = p_employee_id) THEN
        RAISE EXCEPTION 'Employee with ID % does not exist', p_employee_id;
    END IF;

    -- Ustawienie aktualnej daty jako daty zakończenia jeśli nie została podana
    IF end_date IS NULL THEN
        end_date := CURRENT_DATE;
        UPDATE rental_agreement
        SET agreement_actual_termination_date = CURRENT_DATE
        WHERE agreement_id = p_agreement_id;
    END IF;

    rental_days := GREATEST(1, end_date - exec_date);

    -- Oznaczenie zakończenia umowy
    UPDATE rental_agreement
    SET is_agreement_terminated = TRUE
    WHERE agreement_id = p_agreement_id;

    -- Jeśli zwrot po terminie to naliczana jest kara
    IF end_date > due_date THEN
        -- Obliczenie liczby dni opóźnienia
        days_late := (end_date - due_date);

        -- Obliczenie kary (1.5x dzienna cena narzędzia za kaźdy dzień opóźnienia)
        penalty_amount := (rental_fee / rental_days) * 1.5 * days_late;

        -- Minimalna kara 50 PLN
        penalty_amount := GREATEST(50, penalty_amount);

        -- Wstawienie kary z właściwym employee_id
        INSERT INTO fee(
            fee_category,
            agreement_id,
            client_id,
            employee_id,
            actual_fee,
            fee_duty_date,
            fee_finalized_date
        ) VALUES(
                    'PENALTY',
                    p_agreement_id,
                    c_id,
                    p_employee_id,  -- Użycie przekazanego ID pracownika
                    penalty_amount,
                    end_date,
                    NULL
                );

        RAISE NOTICE 'Penalty fee of % PLN applied for agreement % (% days late)', penalty_amount, p_agreement_id, days_late;
    ELSE
        RAISE NOTICE 'Agreement % closed successfully without penalty', p_agreement_id;
    END IF;

    UPDATE tool
    SET tool_availability_status = 'AVAILABLE'
    WHERE tool_id = (SELECT tool_id FROM rental_agreement WHERE agreement_id = p_agreement_id);
    -- Dodanie wpisu do historii
    INSERT INTO tool_event(
        event_category,
        tool_id,
        employee_id,
        event_comment
    )
    SELECT
        'UPDATE',
        tool_id,
        p_employee_id,
        'Umowa zakończona' ||
        CASE WHEN end_date > due_date THEN ' z karą' ELSE '' END
    FROM rental_agreement
    WHERE agreement_id = p_agreement_id;

END;
$$;

-- Komentarz:
-- Procedura zamyka umowę wynajmu i automatycznie nalicza karę za opóźnienie.
-- Kara jest obliczania na podstawie ceny narzędzia i liczby dni opóźnienia.
-- Minimalna kara to 50

-- Funkcja sprawdzająca poprawność dat w umowie wynajmu
CREATE OR REPLACE FUNCTION validate_agreement_dates(
    p_execution_date DATE,
    p_estimated_date DATE
)
    RETURNS BOOLEAN AS $$
BEGIN
    -- Data szacowanego zakonczenia nie może byc wcześniejsza niz data wykonania
    IF p_estimated_date < p_execution_date THEN
        RETURN FALSE;
    END IF;

    -- Umowa nie może być dłuższa niz 90 dni (można zmieniż według potrzeb)
    IF (p_estimated_date - p_execution_date) > 90 THEN
        RETURN FALSE;
    END IF;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Komentarz:
-- Funkcja weryfikuje logiczną poprawność dat w umowie wynajmu.
-- Zapobiega tworzeniu umow z nieprawidłowymi przedziałami czasowymi.
-- Może być używana w triggerach lub przez aplikację przed wstawieniem danych.


-- Trigger sprawdzający poprawność dat przed wstawieniem nowej umowy
CREATE OR REPLACE FUNCTION check_agreement_dates()
    RETURNS TRIGGER AS $$
BEGIN
    -- Dla operacji INSERT zawsze waliduj
    IF TG_OP = 'INSERT' THEN
        IF NOT validate_agreement_dates(
                NEW.agreement_execution_date,
                NEW.agreement_estimated_termination_date
               ) THEN
            RAISE EXCEPTION 'Invalid agreement dates: estimated date cannot be before execution date and maximum rental period is 90 days';
        END IF;

        -- Dla operacji UPDATE waliduj tylko jeśli daty się zmieniają
    ELSIF TG_OP = 'UPDATE' THEN
        -- Sprawdź czy daty uległy zmianie
        IF (OLD.agreement_execution_date IS DISTINCT FROM NEW.agreement_execution_date) OR
           (OLD.agreement_estimated_termination_date IS DISTINCT FROM NEW.agreement_estimated_termination_date) THEN

            IF NOT validate_agreement_dates(
                    NEW.agreement_execution_date,
                    NEW.agreement_estimated_termination_date
                   ) THEN
                RAISE EXCEPTION 'Invalid agreement dates: estimated date cannot be before execution date and maximum rental period is 90 days';
            END IF;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_agreement_dates
    BEFORE INSERT OR UPDATE ON rental_agreement
    FOR EACH ROW
EXECUTE FUNCTION check_agreement_dates();

-- Komentarz:
-- Trigger automatycznie weryfikuje poprawność dat przy każdej operacji INSERT, oraz dla UPDATE gdy zaktualizujemy datę na tabeli rental_agreement.
-- Zapewnia integralność danych na poziomie bazy, niezależnie od aplikacji.
-- Zapobiega tworzeniu nieprawidłowych umów wypożyczenia.


-- Funkcja sprawdzająca czy klient może tworzyć nowe umowy
CREATE OR REPLACE FUNCTION can_client_create_agreement(p_client_id INT)
    RETURNS BOOLEAN AS $$
BEGIN
    RETURN NOT EXISTS (
        SELECT 1 FROM client
        WHERE client_id = p_client_id
          AND (client_has_duty = TRUE OR calculate_client_debt(p_client_id) > 0)
    );
END;
$$ LANGUAGE plpgsql;


-- Funkcja tworzy nową umowę wynajmu i zwraca ID utworzonej umowy.
CREATE OR REPLACE FUNCTION create_rental_agreement_func(
    p_client_id INT,
    p_tool_id INT,
    p_employee_id INT,
    p_estimated_date DATE,
    p_comment VARCHAR(100) DEFAULT NULL
)
    RETURNS INT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_tool_status availability_status;
    v_tool_price NUMERIC(10,2);
    v_rental_fee NUMERIC(10,2);
    v_agreement_id INT;
    v_execution_date DATE;
    v_days INT;
BEGIN
    -- Użyj domyślnej daty wykonania (takiej jak w tabeli)
    v_execution_date := CURRENT_DATE;

    -- Sprawdzenie dostępności sprzętu
    SELECT tool_availability_status, tool_price
    INTO v_tool_status, v_tool_price
    FROM tool
    WHERE tool_id = p_tool_id;

    IF v_tool_status != 'AVAILABLE' THEN
        RAISE EXCEPTION 'Tool is not available for rental. Current status: %', v_tool_status;
    END IF;

    -- Walidacja dat
    IF NOT validate_agreement_dates(v_execution_date, p_estimated_date) THEN
        RAISE EXCEPTION 'Invalid rental dates: execution date %, estimated date %', v_execution_date, p_estimated_date;
    END IF;

    -- Sprawdzenie czy klient istnieje
    IF NOT EXISTS (SELECT 1 FROM client WHERE client_id = p_client_id) THEN
        RAISE EXCEPTION 'Client with ID % does not exist', p_client_id;
    END IF;

    -- Sprawdzenie czy klient może tworzyć umowy
    IF NOT can_client_create_agreement(p_client_id) THEN
        RAISE EXCEPTION 'Client with ID % has outstanding duties and cannot create new agreements. Debt amount: %',
            p_client_id, calculate_client_debt(p_client_id);
    END IF;

    -- Sprawdzenie czy pracownik istnieje
    IF NOT EXISTS (SELECT 1 FROM employee WHERE employee_id = p_employee_id) THEN
        RAISE EXCEPTION 'Employee with ID % does not exist', p_employee_id;
    END IF;

    -- Wstawienie umowy
    INSERT INTO rental_agreement (
        agreement_estimated_termination_date,
        client_id,
        tool_id,
        employee_id,
        agreement_comment
    ) VALUES (
                 p_estimated_date,
                 p_client_id,
                 p_tool_id,
                 p_employee_id,
                 p_comment
             ) RETURNING agreement_id INTO v_agreement_id;

    v_days := GREATEST(1, p_estimated_date - v_execution_date);
    v_rental_fee := v_tool_price * 0.02 * v_days;
    v_rental_fee := GREATEST(5 * v_days, v_rental_fee);

    -- Automatyczne utworzenie opłaty za wynajem
    INSERT INTO fee (
        fee_category,
        agreement_id,
        client_id,
        employee_id,
        actual_fee,
        fee_duty_date
    ) VALUES (
                 'RENTAL_FEE',
                 v_agreement_id,
                 p_client_id,
                 p_employee_id,
                 v_rental_fee,
                 p_estimated_date
             );

    RAISE NOTICE 'Rental agreement % created successfully with fee: %', v_agreement_id, v_rental_fee;
    RETURN v_agreement_id;
END;
$$;

-- Komentarz:
-- Funkcja tworzy nową umowę wynajmu i zwraca ID utworzonej umowy.
-- Może być wywołana w SELECT lub przypisana do zmiennej.
--
-- Sposób użycia:
-- SELECT create_rental_agreement_func(1, 5, 3, '2024-02-15', 'Test agreement');
-- Lub:
-- DO $$
-- DECLARE
--     new_id INT;
-- BEGIN
--     new_id := create_rental_agreement_func(1, 5, 3, '2024-02-15', 'Test agreement');
--     RAISE NOTICE 'Created agreement with ID: %', new_id;
-- END $$;


-- Funkcja do oznaczenia sprzętu jako wynajęty
CREATE OR REPLACE FUNCTION set_tool_rented()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE tool
    SET tool_availability_status = 'RENTED'
    WHERE tool_id = NEW.tool_id;

    INSERT INTO tool_event(event_category, tool_id, employee_id, event_comment)
    VALUES('UPDATE', NEW.tool_id, NEW.employee_id, 'Wypożyczenie sprzętu');

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE TRIGGER trg_set_rented
    AFTER INSERT ON rental_agreement
    FOR EACH ROW
EXECUTE FUNCTION set_tool_rented();

-- Komentarz:
-- Po stworzeniu nowej umowy wypożyczenia sprzęt automatycznie zmienia status
-- i dopisywany jest wpis do historii.