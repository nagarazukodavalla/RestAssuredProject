package apiTestCases;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertFalse;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.Scenario;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.net.URI;
import java.util.regex.Pattern;

public class ApiSteps {

    private static final String DEFAULT_BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final Pattern JSON_SECRET_FIELD = Pattern.compile(
            "(?i)(\"(?:authorization|cookie|set-cookie|password|passwd|token|access_token|"
                    + "refresh_token|api_key|apikey|client_secret|secret)\"\\s*:\\s*)"
                    + "(\"(?:\\\\.|[^\"\\\\])*\"|[^,}\\]]+)");
    private static final Pattern TEXT_SECRET_FIELD = Pattern.compile(
            "(?i)\\b(authorization|cookie|set-cookie|password|passwd|token|access_token|"
                    + "refresh_token|api_key|apikey|client_secret|secret)(\\s*[:=]\\s*)[^\\s,;&]+");
    private static final Pattern URL_SECRET_PARAMETER = Pattern.compile(
            "(?i)([?&](?:authorization|cookie|password|passwd|token|access_token|"
                    + "refresh_token|api_key|apikey|client_secret|secret)=)[^&#\\s]*");
    private static final Pattern URL_USER_INFO = Pattern.compile("(?i)(https?://)[^/@\\s]+@");

    private RequestSpecification requestSpecification;
    private Response response;
    private String baseUrl;
    private String requestMethod;
    private String requestUrl;

    @Before
    public void configureScenarioRequest() {
        baseUrl = System.getProperty("api.base.url", DEFAULT_BASE_URL).trim();
        URI uri = URI.create(baseUrl);
        if (baseUrl.isEmpty()
                || uri.getHost() == null
                || (!"http".equalsIgnoreCase(uri.getScheme())
                        && !"https".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalArgumentException(
                    "System property api.base.url must be an absolute HTTP or HTTPS URL");
        }

        requestSpecification = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setAccept(ContentType.JSON)
                .build();
        response = null;
        requestMethod = null;
        requestUrl = null;
    }

    @Given("the API request is configured")
    public void apiRequestIsConfigured() {
        assertFalse(baseUrl.isBlank(), "The API base URL must be configured");
    }

    @When("I send a GET request to {string}")
    public void sendGetRequest(String path) {
        requestMethod = "GET";
        requestUrl = joinUrl(baseUrl, path);
        response = given(requestSpecification).when().get(path);
    }

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int expectedStatus) {
        response.then().statusCode(expectedStatus);
    }

    @Then("the response should contain todo id {int}")
    public void responseShouldContainTodoId(int expectedId) {
        response.then().body("id", equalTo(expectedId));
    }

    @Then("the todo should have a title")
    public void todoShouldHaveATitle() {
        response.then().body("title", not(equalTo("")));
    }

    @After
    public void attachApiDetails(Scenario scenario) {
        StringBuilder details = new StringBuilder()
                .append("Request method: ")
                .append(requestMethod == null ? "not sent" : requestMethod)
                .append("\nRequest URL: ")
                .append(requestUrl == null ? "not sent" : sanitize(requestUrl))
                .append("\nRequest body: none")
                .append("\nResponse status: ")
                .append(response == null ? "not received" : response.getStatusCode())
                .append("\nResponse body:\n")
                .append(response == null
                        ? "not received"
                        : sanitize(response.getBody().asPrettyString()));
        scenario.attach(details.toString(), "text/plain", "Sanitized API details");
    }

    private static String joinUrl(String base, String path) {
        String normalizedBase = base.replaceAll("/+$", "");
        return normalizedBase + (path.startsWith("/") ? path : "/" + path);
    }

    private static String sanitize(String value) {
        String sanitized = URL_USER_INFO.matcher(value).replaceAll("$1[REDACTED]@");
        sanitized = URL_SECRET_PARAMETER.matcher(sanitized).replaceAll("$1[REDACTED]");
        sanitized = JSON_SECRET_FIELD.matcher(sanitized).replaceAll("$1\"[REDACTED]\"");
        return TEXT_SECRET_FIELD.matcher(sanitized).replaceAll("$1$2[REDACTED]");
    }
}
