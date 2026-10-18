package dev.airscan.bootstrap;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class BootstrapResourceTest {
    @Test
    void statusIsServedOverHttp() {
        get("/bootstrap/status")
                .then()
                .statusCode(200)
                .contentType(startsWith("text/plain"))
                .body(equalTo("ready"));
    }

    @Test
    void openApiExposesBootstrapStatus() {
        get("/q/openapi?format=json")
                .then()
                .statusCode(200)
                .contentType(containsString("json"))
                .body("paths.'/bootstrap/status'.get.responses.'200'.description", org.hamcrest.Matchers.notNullValue());
    }
}
