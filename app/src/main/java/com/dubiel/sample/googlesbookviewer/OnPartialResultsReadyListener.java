package com.dubiel.sample.googlesbookviewer;


interface OnPartialResultsReadyListener {
    void onPartialResultsReady(int current, int max, int totalResults);
}
