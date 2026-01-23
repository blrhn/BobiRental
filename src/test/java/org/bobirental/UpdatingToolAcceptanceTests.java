package org.bobirental;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.bobirental.tool.AvailabilityStatus;
import org.bobirental.tool.EventCategory;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Sql(scripts = "/example_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UpdatingToolAcceptanceTests {
    @LocalServerPort
    private int port;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    private Integer wareHouseManagerId;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        wareHouseManagerId = 1;
    }

    @Test
    public void testUpdatingTool() {
        Integer toolId = 1;

        // wyswietlenie aktualnych danych
        String toolResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                        pathParams("id", toolId).
                when().
                        get("/tools/get/{id}").
                then().
                        statusCode(200).
                        extract().asString();

        // dane do zaktualizowania
        Map<String, Object> toolEventRequest = createToolRequest();

        String updateToolResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                        pathParams("id", toolId).
                        body(toolEventRequest).
                when().
                        put("/tools/{id}").
                then().
                        statusCode(200).
                        body("toolPrice", equalTo(550.5F)).
                        extract().asString();
    }

    private Map<String, Object> createToolRequest() {
        Map<String, Object> toolRequest = new HashMap<>();

        toolRequest.put("toolName", "Wiertarka udarowa Makita");
        toolRequest.put("availabilityStatus", AvailabilityStatus.AVAILABLE);
        toolRequest.put("toolDescription", "Wiertarka udarowa 850W, idealna do betonu");
        toolRequest.put("toolCategory", ToolCategory.DRILL);
        toolRequest.put("toolPrice", new BigDecimal("550.50"));

        return toolRequest;
    }

    @Test
    public void testDeletingTool() {
        Integer toolId = 1;

        // wyswietlenie aktualnych danych
        String toolResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                        pathParams("id", toolId).
                when().
                        get("/tools/get/{id}").
                then().
                        statusCode(200).
                        extract().asString();

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

        // weryfikacja

        String updateToolResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                        pathParams("id", toolId).
                        body(toolEventRequest).
                when().
                        get("/tools/get/{id}").
                then().
                        statusCode(200).
                        body("toolAvailabilityStatus", equalTo(AvailabilityStatus.UNAVAILABLE.name())).
                        extract().asString();

        String toolEventsResponse =
                given().
                        auth().preemptive().basic("j.kowalski", "haslo123").
                        contentType(ContentType.JSON).
                        pathParams("id", toolId).
                when().
                        get("/tool_events/tool/{id}").
                then().
                        statusCode(200).
                        body("[1].eventCategory", equalTo(EventCategory.DELETION.name())).
                        extract().asString();
    }

    private Map<String, Object> createToolEventRequest(Integer toolId) {
        Map<String, Object> toolEventRequest = new HashMap<>();

        toolEventRequest.put("eventCategory", EventCategory.DELETION);
        toolEventRequest.put("eventComment", "Popsute");
        toolEventRequest.put("toolId", toolId);
        toolEventRequest.put("employeeId", wareHouseManagerId);

        return toolEventRequest;
    }

}
