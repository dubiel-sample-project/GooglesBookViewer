package com.dubiel.sample.googlesbookviewer.search.searchitem;



public class VolumeInfo {
    public String title;
    public ImageLinks imageLinks;

    public VolumeInfo(String title, ImageLinks imageLinks) {
        this.title = title;
        this.imageLinks = imageLinks;
    }

    public String getTitle() {
        return title;
    }

    public ImageLinks getImageLinks() {
        return imageLinks;
    }
}
