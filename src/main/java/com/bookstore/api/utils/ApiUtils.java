package com.bookstore.api.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class ApiUtils {
    private static final Logger LOGGER = Logger.getLogger(ApiUtils.class.getName());
    private static String BASE_URL;

    static {
        Properties props = new Properties();
        try {
            props.load(ApiUtils.class.getClassLoader().getResourceAsStream("config.properties"));
            BASE_URL = props.getProperty("api.base.url", "http://localhost:8080");
            LOGGER.info("Initialized base URL: " + BASE_URL);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
        RestAssured.baseURI = BASE_URL;
    }

    public static void setBaseURI(String baseURI) {
        RestAssured.baseURI = baseURI;
        LOGGER.info("Updated base URI to: " + baseURI);
    }

    public static Response postRequest(String endpoint, Object body) {
        return RestAssured.given()
                .contentType("application/json")
                .body(body)
                .post(endpoint);
    }

    public static Response getRequest(String endpoint) {
        return RestAssured.given()
                .get(endpoint);
    }

    public static Response putRequest(String endpoint, Object body) {
        return RestAssured.given()
                .contentType("application/json")
                .body(body)
                .put(endpoint);
    }

    public static Response deleteRequest(String endpoint) {
        return RestAssured.given()
                .delete(endpoint);
    }
}