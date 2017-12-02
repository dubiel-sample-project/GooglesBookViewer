package com.dubiel.sample.googlesbookviewer.search.searchitem;



public class VolumeInfo {
    public String title;
    public ImageLinks imageLinks;
    public String[] authors;
    public String description;

    public VolumeInfo(String title, ImageLinks imageLinks) {
        this.title = title;
        this.imageLinks = imageLinks;
    }

    public VolumeInfo(String title, ImageLinks imageLinks, String[] authors) {
        this.title = title;
        this.imageLinks = imageLinks;
        this.authors = authors;
    }

    public VolumeInfo(String title, ImageLinks imageLinks, String[] authors, String description) {
        this.title = title;
        this.imageLinks = imageLinks;
        this.authors = authors;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public ImageLinks getImageLinks() {
        return imageLinks;
    }

    public String[] getAuthors() {
        return authors;
    }

    public String getDescription() {
        return description;
    }
}
