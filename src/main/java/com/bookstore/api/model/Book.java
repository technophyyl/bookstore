package com.bookstore.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Book {
    @JsonProperty
    private String title;
    @JsonProperty
    private String author;
    @JsonProperty
    private double price;

    public Book() {
    }

    public Book(String title, String author, double price) {
        this.title = title;
        this.author = author;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public double getPrice() {
        return price;
    }
}