package org.bobirental;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.bobirental.tool.AvailabilityStatus;
import org.bobirental.tool.ToolCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Sql(scripts = "/example_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class RentingToolAcceptanceTests {
    @LocalServerPort
    private int port;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    private Integer employeeId;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        employeeId = 2;
    }

    @Test
    public void testRentingTool_WithId() {

        // podanie numeru klienta i numeru sprzetu (pracownik zna numer sprzetu)
        Integer clientId = 1;
        Integer toolId = 4;

        // Weryfikacja statusu klienta oraz sprawdzenie dostepnosci sprzetu

        String clientVerificationResponse =
                given().
                        auth().preemptive().basic("a.nowak", "anna2024").
                        contentType(ContentType.JSON).
                        pathParam("id", clientId).
                when().
                        get("/clients/can_create/{id}").
                then().
                        statusCode(200).
                        body(equalTo("true")).
                        extract().asString();

        String toolVerificationResponse =
                given()
                        .auth().preemptive().basic("a.nowak", "anna2024").
                        contentType(ContentType.JSON).
                        pathParam("id", toolId).
                when().
                        get("/tools/available/{id}").
                then().
                        statusCode(200).
                        body("toolAvailabilityStatus", equalTo(AvailabilityStatus.AVAILABLE.name())).
                        body("id", equalTo(toolId)).
                        extract().asString();

        // Wypozyczenie sprzetu
        Map<String, Object> rentalAgreementRequest = createRentalAgreementRequest(toolId, clientId);

        String createRentalAgreementResponse =
                given().
                        auth().preemptive().basic("a.nowak", "anna2024").
                        contentType(ContentType.JSON).
                        body(rentalAgreementRequest).
                when().
                        post("/rental_agreements/create").
                then().
                        statusCode(200).
                        body(notNullValue()).
                        extract().asString();

        Integer agreementId = Integer.parseInt(createRentalAgreementResponse);

        // potwierdzenie zawarcia umowy
        String rentalAgreementResponse =
                given().
                        auth().preemptive().basic("a.nowak", "anna2024").
                        contentType(ContentType.JSON).
                        pathParam("id", agreementId).
                when().
                        get("/rental_agreements/{id}").
                then().
                        statusCode(200).
                        body("id", equalTo(agreementId)).
                        body("client.id", equalTo(clientId)).
                        body("tool.id", equalTo(toolId)).
                        body("employee.id", equalTo(employeeId)).
                        body("agreementTerminated", equalTo(false)).
                        body("agreementExecutionDate", equalTo(LocalDate.now().toString())).
                        extract().asString();
    }

    private Map<String, Object> createRentalAgreementRequest(Integer toolId, Integer clientId) {
        Map<String, Object> rentalAgreementRequest = new HashMap<>();
        rentalAgreementRequest.put("agreementEstimatedTerminationDate", LocalDate.now().plusDays(7));
        rentalAgreementRequest.put("clientId", clientId);
        rentalAgreementRequest.put("toolId", toolId);
        rentalAgreementRequest.put("employeeId", employeeId);
        rentalAgreementRequest.put("agreementComment", "Na potrzeby remontowe kuchni");

        return rentalAgreementRequest;
    }

    @Test
    public void testRentingTool_WithCategory() {

        // podanie numeru klienta i kategorii sprzetu (pracownik nie zna numer sprzetu)
        Integer clientId = 1;
        ToolCategory toolCategory = ToolCategory.DRILL;

        // Weryfikacja statusu klienta oraz sprawdzenie dostepnosci sprzetu

        String clientVerificationResponse =
                given().
                        auth().preemptive().basic("a.nowak", "anna2024").
                        contentType(ContentType.JSON).
                        pathParam("id", clientId).
                when().
                        get("/clients/can_create/{id}").
                then().
                        statusCode(200).
                        body(equalTo("true")).
                        extract().asString();

        String toolVerificationResponse =
                given().
                        auth().preemptive().basic("a.nowak", "anna2024").
                        contentType(ContentType.JSON).
                        pathParam("category", toolCategory.name()).
                when().
                        get("/tools/available/category/{category}").
                then().
                        statusCode(200).
                        body("[0].toolAvailabilityStatus", equalTo(AvailabilityStatus.AVAILABLE.name())).
                        extract().asString();

        // Wybranie jednego z dostepnych sprzetow

        Integer toolId = from(toolVerificationResponse).getInt("[0].id");

        // Wypozyczenie sprzetu
        Map<String, Object> rentalAgreementRequest = createRentalAgreementRequest(toolId, clientId);

        String createRentalAgreementResponse =
                given().
                        auth().preemptive().basic("a.nowak", "anna2024").
                        contentType(ContentType.JSON).
                        body(rentalAgreementRequest).
                when().
                        post("/rental_agreements/create").
                then().
                        statusCode(200).
                        body(notNullValue()).
                        extract().asString();

        Integer agreementId = Integer.parseInt(createRentalAgreementResponse);

        // potwierdzenie zawarcia umowy
        String rentalAgreementResponse =
                given().
                        auth().preemptive().basic("a.nowak", "anna2024").
                        contentType(ContentType.JSON).
                        pathParam("id", agreementId).
                when().
                        get("/rental_agreements/{id}").
                then().
                        statusCode(200).
                        body("id", equalTo(agreementId)).
                        body("client.id", equalTo(clientId)).
                        body("tool.id", equalTo(toolId)).
                        body("employee.id", equalTo(employeeId)).
                        body("agreementTerminated", equalTo(false)).
                        body("agreementExecutionDate", equalTo(LocalDate.now().toString())).
                        extract().asString();
    }


    // TESTY IDOTO-ODPORNOSCI

    @Test
    void testRentingToolFragment_ShouldPreventRentalForClientWithDebt() {
        // klient z dlugiem
        Integer clientWithDebt = 3;

        // sprawdzenie czy klient może utworzyć umowę
        given().
                auth().preemptive().basic("a.nowak", "anna2024").
                pathParam("id", clientWithDebt)
        .when().
                get("/clients/can_create/{id}").
        then().
                statusCode(200).
                body(equalTo("false"));
    }

    @Test
    void testRentingToolFragment_ShouldPreventRentalOfUnavailableTool() {
        Integer unavailableToolId = 3;

        // Próba pobrania niedostępnego sprzętu
        given().
                auth().preemptive().basic("a.nowak", "anna2024").
                pathParam("id", unavailableToolId).
        when().
                get("/tools/available/{id}").
        then().
                statusCode(200).
                body(equalTo(""));
    }
}
