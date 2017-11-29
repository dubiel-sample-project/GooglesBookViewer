package com.dubiel.sample.googlesbookviewer.search.searchitem;



public class BookDetailItem {
    public String id;
    public String selfLink;
    public VolumeInfo volumeInfo;

    public BookDetailItem() {
    }

    public BookDetailItem(String id, String selfLink, VolumeInfo volumeInfo) {
        this.id = id;
        this.selfLink = selfLink;
        this.volumeInfo = volumeInfo;
    }
}