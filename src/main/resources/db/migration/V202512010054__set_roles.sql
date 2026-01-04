-- Role
CREATE ROLE employee;
CREATE ROLE warehouse_manager;

-- UPRAWNIENIA DLA TABEL
-- CLIENT
GRANT INSERT, SELECT, UPDATE ON client TO employee;
GRANT INSERT, SELECT, UPDATE, DELETE ON client TO warehouse_manager;

-- RENTAL AGREEMENT
GRANT INSERT, SELECT, UPDATE ON rental_agreement TO employee;
GRANT INSERT, SELECT, UPDATE, DELETE ON rental_agreement TO warehouse_manager;

-- TOOL
GRANT SELECT ON tool TO employee;
GRANT INSERT, SELECT, UPDATE, DELETE ON tool TO warehouse_manager;

-- TOOL EVENT
GRANT INSERT, SELECT ON tool_event TO employee;
GRANT INSERT, SELECT, UPDATE, DELETE ON tool_event TO warehouse_manager;

-- EMPLOYEE
GRANT SELECT ON employee TO employee;
GRANT INSERT, SELECT, UPDATE, DELETE ON employee TO warehouse_manager;

-- FEE
GRANT SELECT, INSERT, UPDATE ON fee TO employee;
GRANT INSERT, SELECT, UPDATE, DELETE ON fee TO warehouse_manager;