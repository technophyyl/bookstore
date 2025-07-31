package com.bookstore.api.tests;

import com.bookstore.api.model.Book;
import com.bookstore.api.utils.ApiUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Feature("Bookstore API Tests")
public class BookstoreApiTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookstoreApiTests.class);
    private Book book;
    private String bookId;
    private WireMockServer wireMockServer;

    @BeforeClass
    public void setup() {
        // Initialize WireMock server with dynamic port and response templating
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort().globalTemplating(true));
        wireMockServer.start();
        LOGGER.info("WireMock server started on port: " + wireMockServer.port());

        // Configure WireMock client to use the dynamic port
        WireMock.configureFor("localhost", wireMockServer.port());

        // Update RestAssured base URI to WireMock's dynamic port
        ApiUtils.setBaseURI(wireMockServer.baseUrl());

        // Initialize test data
        book = new Book("Test Book", "Test Author", 29.99);

        // Stub for POST /books (create book)
        stubFor(post(urlEqualTo("/books"))
                .withRequestBody(equalToJson("{\"title\":\"" + book.getTitle() + "\",\"author\":\"" + book.getAuthor() + "\",\"price\":" + book.getPrice() + "}", true, true))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"12345\",\"title\":\"" + book.getTitle() + "\",\"author\":\"" + book.getAuthor() + "\",\"price\":" + book.getPrice() + "}")));

        // Stub for GET /books/{id} (retrieve book)
        stubFor(get(urlPathMatching("/books/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"{{request.path.[1]}}\",\"title\":\"" + book.getTitle() + "\",\"author\":\"" + book.getAuthor() + "\",\"price\":" + book.getPrice() + "}")));

        // Stub for PUT /books/{id} (update book)
        stubFor(put(urlPathMatching("/books/.*"))
                .withRequestBody(equalToJson("{\"title\":\"Updated Book\",\"author\":\"Updated Author\",\"price\":39.99}", true, true))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"{{request.path.[1]}}\",\"title\":\"Updated Book\",\"author\":\"Updated Author\",\"price\":39.99}")));

        // Stub for DELETE /books/{id} (delete book)
        stubFor(delete(urlPathMatching("/books/.*"))
                .willReturn(aResponse()
                        .withStatus(204)));

        // Stub for GET /books/999999 (non-existent book)
        stubFor(get(urlEqualTo("/books/999999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"detail\":\"Book not found\"}")));

        // Stub for POST /books with missing title
        stubFor(post(urlEqualTo("/books"))
                .withRequestBody(equalToJson("{\"title\":null,\"author\":\"Test Author\",\"price\":29.99}", true, true))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"detail\":\"Title is required\"}")));

        // Stub for POST /books with missing author
        stubFor(post(urlEqualTo("/books"))
                .withRequestBody(equalToJson("{\"title\":\"Test Book\",\"author\":null,\"price\":29.99}", true, true))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"detail\":\"Author is required\"}")));

        // Stub for POST /books with zero price
        stubFor(post(urlEqualTo("/books"))
                .withRequestBody(equalToJson("{\"title\":\"Test Book\",\"author\":\"Test Author\",\"price\":0}", true, true))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"detail\":\"Price must be positive\"}")));

        // Stub for POST /books with malformed JSON
        stubFor(post(urlEqualTo("/books"))
                .withRequestBody(matching(".*[^}]+"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"detail\":\"Invalid JSON format\"}")));

        // Stub for PUT /books/999999 (non-existent book)
        stubFor(put(urlEqualTo("/books/999999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"detail\":\"Book not found\"}")));

        // Stub for DELETE /books/999999 (non-existent book)
        stubFor(delete(urlEqualTo("/books/999999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"detail\":\"Book not found\"}")));

        // Stub for GET /books/invalid (invalid endpoint)
        stubFor(get(urlEqualTo("/books/invalid"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"detail\":\"Endpoint not found\"}")));

        // Stub for PATCH /books/{id} (invalid method)
        stubFor(patch(urlPathMatching("/books/.*"))
                .willReturn(aResponse()
                        .withStatus(405)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"detail\":\"Method not allowed\"}")));
    }

    @AfterClass
    public void tearDown() {
        // Stop WireMock server
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            LOGGER.info("WireMock server stopped");
        }
    }

    @Test(priority = 1)
    @Description("Test creating a new book")
    public void testCreateBook() {
        Response response = ApiUtils.postRequest("/books", book);
        response.then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("title", equalTo(book.getTitle()))
                .body("author", equalTo(book.getAuthor()))
                .body("price", equalTo((float) book.getPrice()));

        bookId = response.jsonPath().getString("id");
    }

    @Test(priority = 2, dependsOnMethods = "testCreateBook")
    @Description("Test retrieving a book by ID")
    public void testGetBook() {
        ApiUtils.getRequest("/books/" + bookId)
                .then()
                .statusCode(200)
                .body("id", equalTo(bookId))
                .body("title", equalTo(book.getTitle()));
    }

    @Test(priority = 3, dependsOnMethods = "testCreateBook")
    @Description("Test updating a book")
    public void testUpdateBook() {
        Book updatedBook = new Book("Updated Book", "Updated Author", 39.99);
        ApiUtils.putRequest("/books/" + bookId, updatedBook)
                .then()
                .statusCode(200)
                .body("title", equalTo(updatedBook.getTitle()))
                .body("author", equalTo(updatedBook.getAuthor()))
                .body("price", equalTo((float) updatedBook.getPrice()));
    }

    @Test(priority = 4, dependsOnMethods = "testCreateBook")
    @Description("Test deleting a book")
    public void testDeleteBook() {
        ApiUtils.deleteRequest("/books/" + bookId)
                .then()
                .statusCode(204);
    }

    @Test
    @Description("Test negative scenario: Get non-existent book")
    public void testGetNonExistentBook() {
        ApiUtils.getRequest("/books/999999")
                .then()
                .statusCode(404)
                .body("detail", containsString("not found"));
    }

    @Test
    @Description("Test negative scenario: Create book with invalid data")
    public void testCreateBookInvalidData() {
        Book invalidBook = new Book("", "", -10.0);
        ApiUtils.postRequest("/books", invalidBook)
                .then()
                .statusCode(422)
                .body("detail", notNullValue());
    }

    @Test
    @Description("Test negative scenario: Create book with missing title")
    public void testCreateBookMissingTitle() {
        Book invalidBook = new Book(null, "Test Author", 29.99);
        ApiUtils.postRequest("/books", invalidBook)
                .then()
                .statusCode(422)
                .body("detail", containsString("Title is required"));
    }

    @Test
    @Description("Test negative scenario: Create book with missing author")
    public void testCreateBookMissingAuthor() {
        Book invalidBook = new Book("Test Book", null, 29.99);
        ApiUtils.postRequest("/books", invalidBook)
                .then()
                .statusCode(422)
                .body("detail", containsString("Author is required"));
    }

    @Test
    @Description("Test negative scenario: Create book with zero price")
    public void testCreateBookZeroPrice() {
        Book invalidBook = new Book("Test Book", "Test Author", 0.0);
        ApiUtils.postRequest("/books", invalidBook)
                .then()
                .statusCode(422)
                .body("detail", containsString("Price must be positive"));
    }

    @Test
    @Description("Test negative scenario: Create book with malformed JSON")
    public void testCreateBookMalformedJson() {
        String malformedJson = "{\"title\":\"Test Book\",\"author\":\"Test Author\",\"price\":29.99"; // Missing closing brace
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(malformedJson)
                .post("/books");
        response.then()
                .statusCode(400)
                .body("detail", containsString("Invalid JSON format"));
    }

    @Test
    @Description("Test negative scenario: Update non-existent book")
    public void testUpdateNonExistentBook() {
        Book updatedBook = new Book("Updated Book", "Updated Author", 39.99);
        ApiUtils.putRequest("/books/999999", updatedBook)
                .then()
                .statusCode(404)
                .body("detail", containsString("not found"));
    }

    @Test
    @Description("Test negative scenario: Delete non-existent book")
    public void testDeleteNonExistentBook() {
        ApiUtils.deleteRequest("/books/999999")
                .then()
                .statusCode(404)
                .body("detail", containsString("not found"));
    }

    @Test
    @Description("Test negative scenario: Access invalid endpoint")
    public void testInvalidEndpoint() {
        ApiUtils.getRequest("/books/invalid")
                .then()
                .statusCode(404)
                .body("detail", containsString("Endpoint not found"));
    }

    @Test
    @Description("Test negative scenario: Use invalid HTTP method")
    public void testInvalidMethod() {
        Book updatedBook = new Book("Updated Book", "Updated Author", 39.99);
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(updatedBook)
                .patch("/books/12345");
        response.then()
                .statusCode(405)
                .body("detail", containsString("Method not allowed"));
    }
}