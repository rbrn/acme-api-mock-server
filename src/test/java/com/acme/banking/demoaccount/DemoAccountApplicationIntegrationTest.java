package com.acme.banking.demoaccount;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for the Demo Account Mock Server.
 * <p>
 * These tests verify that the WireMock server correctly serves
 * the mock responses for the personal-data endpoint.
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DemoAccountApplicationIntegrationTest {

    private static final int WIREMOCK_PORT = 8089;
    private static final String BASE_URL = "http://localhost:" + WIREMOCK_PORT;
    private static final String PERSONAL_DATA_ENDPOINT = "/customers/{partnerId}/personal-data";
    private static final String PARTNER_ID = "6585363429";
    private static final String HEADER_DEUBA_CLIENT_ID = "deuba-client-id";

    private WireMockServer wireMockServer;

    @BeforeAll
    void setUp() {
        WireMockConfiguration config = WireMockConfiguration.options()
                .port(WIREMOCK_PORT)
                .usingFilesUnderClasspath("wiremock")
                .globalTemplating(true);

        wireMockServer = new WireMockServer(config);
        wireMockServer.start();

        RestAssured.baseURI = BASE_URL;
    }

    @AfterAll
    void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    @DisplayName("Should return personal data with valid deuba-client-id header containing 'pb-banking'")
    void shouldReturnPersonalDataWithPbBankingHeader() {
        given()
                .header(HEADER_DEUBA_CLIENT_ID, "pb-banking")
                .accept(ContentType.JSON)
        .when()
                .get(PERSONAL_DATA_ENDPOINT, PARTNER_ID)
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("firstname", equalTo("Max"))
                .body("lastname", equalTo("Mustermann"))
                .body("nationality", equalTo("DEU"))
                .body("id", equalTo(6585363429L))
                .body("fullName", equalTo("Max Mustermann"));
    }

    @Test
    @DisplayName("Should return personal data with valid deuba-client-id header containing 'mobile-banking'")
    void shouldReturnPersonalDataWithMobileBankingHeader() {
        given()
                .header(HEADER_DEUBA_CLIENT_ID, "mobile-banking")
                .accept(ContentType.JSON)
        .when()
                .get(PERSONAL_DATA_ENDPOINT, PARTNER_ID)
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("firstname", equalTo("Max"))
                .body("nationality", equalTo("DEU"));
    }

    @Test
    @DisplayName("Should return personal data with valid deuba-client-id header containing 'app-banking'")
    void shouldReturnPersonalDataWithAppBankingHeader() {
        given()
                .header(HEADER_DEUBA_CLIENT_ID, "app-banking")
                .accept(ContentType.JSON)
        .when()
                .get(PERSONAL_DATA_ENDPOINT, PARTNER_ID)
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("firstname", equalTo("Max"))
                .body("nationality", equalTo("DEU"));
    }

    @Test
    @DisplayName("Should return personal data with correct address information")
    void shouldReturnCorrectAddressInformation() {
        given()
                .header(HEADER_DEUBA_CLIENT_ID, "pb-banking")
                .accept(ContentType.JSON)
        .when()
                .get(PERSONAL_DATA_ENDPOINT, PARTNER_ID)
        .then()
                .statusCode(200)
                .body("registrationAddress.street", equalTo("Taunusanlage"))
                .body("registrationAddress.streetNumber", equalTo("12"))
                .body("registrationAddress.postalCode", equalTo("60325"))
                .body("registrationAddress.city", equalTo("Frankfurt am Main"))
                .body("postalAddress.street", equalTo("Taunusanlage"));
    }

    @Test
    @DisplayName("Should return personal data with correct contact information")
    void shouldReturnCorrectContactInformation() {
        given()
                .header(HEADER_DEUBA_CLIENT_ID, "pb-banking")
                .accept(ContentType.JSON)
        .when()
                .get(PERSONAL_DATA_ENDPOINT, PARTNER_ID)
        .then()
                .statusCode(200)
                .body("emailAddress.address", equalTo("max.mustermann@mail.com"))
                .body("emailAddress.type", equalTo("PRIVATE"))
                .body("phoneNumbers.private.countryCode", equalTo("+49"))
                .body("phoneNumbers.private.number", equalTo("123456789"))
                .body("phoneNumbers.mobile.number", equalTo("900000700"));
    }

    @Test
    @DisplayName("Should return personal data with correct personal details")
    void shouldReturnCorrectPersonalDetails() {
        given()
                .header(HEADER_DEUBA_CLIENT_ID, "pb-banking")
                .accept(ContentType.JSON)
        .when()
                .get(PERSONAL_DATA_ENDPOINT, PARTNER_ID)
        .then()
                .statusCode(200)
                .body("dateOfBirth", equalTo("1996-01-01"))
                .body("maritalStatus", equalTo("married"))
                .body("gender", equalTo("MALE"));
    }

    @Test
    @DisplayName("Should work with different partner IDs due to regex pattern matching")
    void shouldWorkWithDifferentPartnerIds() {
        given()
                .header(HEADER_DEUBA_CLIENT_ID, "pb-banking")
                .accept(ContentType.JSON)
        .when()
                .get(PERSONAL_DATA_ENDPOINT, "9999999999")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("firstname", equalTo("Max"));
    }

    @Test
    @DisplayName("Should return 404 when deuba-client-id header is missing")
    void shouldReturn404WhenHeaderIsMissing() {
        given()
                .accept(ContentType.JSON)
        .when()
                .get(PERSONAL_DATA_ENDPOINT, PARTNER_ID)
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Should return 404 when deuba-client-id header does not contain '-banking'")
    void shouldReturn404WhenHeaderValueIsInvalid() {
        given()
                .header(HEADER_DEUBA_CLIENT_ID, "invalid-header")
                .accept(ContentType.JSON)
        .when()
                .get(PERSONAL_DATA_ENDPOINT, PARTNER_ID)
        .then()
                .statusCode(404);
    }
}
