package com.dubiel.sample.googlebookviewer.UnitTests;

import com.dubiel.sample.googlebookviewer.search.SearchManager;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class SearchManagerUnitTest {
    @Test
    public void search_url_isCorrect() throws Exception {
        String url = "https://www.googleapis.com/books/v1/volumes?q=search&fields=items(id,selfLink,volumeInfo/title,volumeInfo/imageLinks/smallThumbnail)&startIndex=1200&maxResults=" + SearchManager.MAX_RESULTS;
        assertEquals(url, SearchManager.createUrl("search", 30));
    }
}