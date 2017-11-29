package com.dubiel.sample.googlesbookviewer.search.searchitem;



public class VolumeInfo {
    public String title;
    public ImageLinks imageLinks;
    public String[] authors;

    public VolumeInfo(String title, ImageLinks imageLinks) {
        this.title = title;
        this.imageLinks = imageLinks;
    }

    public VolumeInfo(String title, ImageLinks imageLinks, String[] authors) {
        this.title = title;
        this.imageLinks = imageLinks;
        this.authors = authors;
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
}
