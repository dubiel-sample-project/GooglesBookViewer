package com.dubiel.sample.googlebookviewer.search.searchitem;


public class ImageLinks {
    public String smallThumbnail;
    public String small;

    public ImageLinks(String smallThumbnail) {
        this.smallThumbnail = smallThumbnail;
    }

    public ImageLinks(String smallThumbnail, String small) {
        this.smallThumbnail = smallThumbnail;
        this.small = small;
    }

    public String getSmallThumbnail() {
        return smallThumbnail;
    }

    public String getSmall() {
        return small;
    }
}
