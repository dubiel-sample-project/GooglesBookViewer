package com.dubiel.sample.googlesbookviewer.search;

import android.content.Context;

import com.dubiel.sample.googlesbookviewer.search.searchitem.BookListItems;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class SearchTask implements Callable<BookListItems>
{
    static final private String TAG = "searchtask";

    private Context context;
    private String url;
    private int startIndex;

    public SearchTask(Context context, String url, int startIndex) {
        this.context = context;
        this.startIndex = startIndex;
        this.url = url;
    }

    public BookListItems call() {
        try {
            //Log.i("mapmanager", "thread id start: " + Long.toString(Thread.currentThread().getId()));
            //Log.i("mapmanager", "thread name start: " + Thread.currentThread().getName());

            BookListItems bookListItems = Ion.with(getContext())
                    .load(getUrl())
                    .as(new TypeToken<BookListItems>() {
                    }).get();
            bookListItems.startIndex = this.startIndex;

            return bookListItems;
        } catch(InterruptedException | ExecutionException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public Context getContext() {
        return context;
    }

    public String getUrl() {
        return url;
    }

    public int getStartIndex() {
        return startIndex;
    }
}