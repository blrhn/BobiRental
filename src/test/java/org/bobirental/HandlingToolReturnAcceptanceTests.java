package org.bobirental;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.bobirental.rental.fee.FeeCategory;
import org.bobirental.tool.EventCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Sql(scripts = "/example_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class HandlingToolReturnAcceptanceTests {
    @LocalServerPort
    private int port;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    private Integer warehouseManagerId;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        warehouseManagerId = 1;
    }

    @Test
    public void testHandlingToolReturn_WhenToolIsNotDamaged() {
        Integer clientId = 5;
        Integer agreementId = 4;

        given().
                auth().preemptive().basic("a.nowak", "anna2024").
                contentType(ContentType.JSON).
                pathParam("id", agreementId).
                queryParam("clientId", clientId).
        when().
                post("/rental_agreements/initiate-return/{id}").
        then().
                statusCode(200);


        // szef magazynu sprawdza liste sprzetow do sprawdzenia

        String toReviewResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                when().
                        get("/rental_agreements/to-review")
                .then().
                        statusCode(200).
                        body("[0].id", equalTo(agreementId)).
                        extract().asString();

        // ocena stanu sprzetu - brak uszkodzen -> zamkniecie umowy

        String closeAgreementResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                        pathParam("id", agreementId).
                        queryParam("employeeId", warehouseManagerId).
                when().
                        post("/rental_agreements/close/{id}/").
                then().
                        statusCode(200).
                        extract().asString();

        System.out.println(closeAgreementResponse);

        // weryfikacja zamkniecia umowy
        String closedAgreement =
                given().
                        auth().preemptive().basic("a.nowak", "anna2024").
                        contentType(ContentType.JSON).
                        pathParam("id", agreementId)
                .when().
                        get("/rental_agreements/{id}")
                .then().
                        statusCode(200).
                        body("agreementTerminated", equalTo(true)).
                        body("agreementActualTerminationDate", notNullValue()).
                        extract().asString();

        Integer toolId = from(closedAgreement).getInt("tool.id");

        // weryfikacja dostepności sprzetu
        String toolAvailableAgain =
                given().
                        auth().preemptive().basic("a.nowak", "anna2024").
                        contentType(ContentType.JSON).
                        pathParam("id", toolId).
                when().
                        get("/tools/available/{id}").
                then().
                        statusCode(200).
                        body("toolAvailabilityStatus", equalTo("AVAILABLE")).
                        extract().asString();
    }

    @Test
    public void testHandlingToolReturn_WhenToolIsDamaged() {
        Integer clientId = 5;
        Integer agreementId = 4;

        given().
                auth().preemptive().basic("a.nowak", "anna2024").
                contentType(ContentType.JSON).
                pathParam("id", agreementId).
                queryParam("clientId", clientId).
        when().
                post("/rental_agreements/initiate-return/{id}").
        then().
                statusCode(200);


        // szef magazynu sprawdza liste sprzetow do sprawdzenia

        String toReviewResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                when().
                        get("/rental_agreements/to-review")
                .then().
                        statusCode(200).
                        body("[0].id", equalTo(agreementId)).
                        extract().asString();

        // szef magazynu ocenia stan sprzetu - uszkodzenia
        Integer toolId = from(toReviewResponse).getInt("[0].tool.id");

        // nalozenie kary
        Map<String, Object> feeRequest = createFeeRequest(clientId, agreementId);

        String feeCreationResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                        body(feeRequest).
                when().
                        post("/fees/create").
                then().
                        statusCode(200).
                        extract().asString();

        Integer feeId = Integer.parseInt(feeCreationResponse);

        // Usuwanie sprzetu
        Map<String, Object> toolEventRequest = createToolEventRequest(toolId);

        String markAsUnavailableResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                        body(toolEventRequest).
                        pathParam("id", toolId).
                when().
                        delete("/tools/{id}").
                then().
                        statusCode(200).
                        extract().asString();

        // weryfikacja nalozenia kary na klienta
        String totalClientFeesResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                        pathParam("id", clientId).
                when().
                        get("/clients/fees/{id}").
                then().
                        statusCode(200).
                        extract().asString();

        BigDecimal fee = new BigDecimal(totalClientFeesResponse);
        assertTrue(fee.subtract(new BigDecimal("200.0")).compareTo(BigDecimal.ZERO) >= 0);

        // weryfikacja niedostepnosci sprzetu
        String unaviableToolResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                        pathParam("id", toolId).
                when().
                        get("/tools/available/{id}").
                then().
                        statusCode(200).
                        body(equalTo("")).
                        extract().asString();
    }

    private Map<String, Object> createFeeRequest(Integer clientId, Integer agreementId) {
        Map<String, Object> feeRequest = new HashMap<>();

        feeRequest.put("feeCategory", FeeCategory.PENALTY);
        feeRequest.put("rentalAgreementId",  agreementId);
        feeRequest.put("clientId", clientId);
        feeRequest.put("employeeId", warehouseManagerId);
        feeRequest.put("actualFee", new BigDecimal("100.50"));
        feeRequest.put("feeDutyDate", LocalDate.now().plusDays(14));

        return feeRequest;
    }

    private Map<String, Object> createToolEventRequest(Integer toolId) {
        Map<String, Object> toolEventRequest = new HashMap<>();

        toolEventRequest.put("eventCategory", EventCategory.DELETION);
        toolEventRequest.put("toolId", toolId);
        toolEventRequest.put("employeeId", warehouseManagerId);

        return toolEventRequest;
    }

}
