package com.dubiel.sample.googlebookviewer.search;


public interface OnPartialResultsReadyListener {
    void onPartialResultsReady(int current, int max, int totalResults, int startIndex);
}
