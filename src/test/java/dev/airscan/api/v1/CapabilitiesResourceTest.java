package dev.airscan.api.v1;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CapabilitiesResourceTest {
  @Test
  void versionedQueryWorksOverHttpWithoutMcp() {
    get("/api/v1/capabilities")
        .then()
        .statusCode(200)
        .contentType(containsString("application/json"))
        .body("apiVersion", equalTo("v1"))
        .body("operations", hasItem("airscan.capabilities"));
  }

  @Test
  void servedOpenApiExposesVersionedJsonContract() {
    get("/q/openapi?format=json")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body(
            "paths.'/api/v1/capabilities'.get.responses.'200'.content.'application/json'.schema",
            notNullValue());
  }
}
