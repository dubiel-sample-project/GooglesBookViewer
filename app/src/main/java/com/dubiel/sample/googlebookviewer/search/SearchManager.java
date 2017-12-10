package com.dubiel.sample.googlebookviewer.search;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.dubiel.sample.googlebookviewer.MainActivity;
import com.dubiel.sample.googlebookviewer.R;
import com.dubiel.sample.googlebookviewer.search.searchitem.BookListItems;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class SearchManager {

    static final public int TASK_COMPLETE = 1;
    static final public int ALL_TASK_COMPLETE = 2;

    static final public int MAX_RESULTS = 40;

    static final private String TAG = "searchmanager";
    static final private int NUM_THREADS = 10;
    static final private String SEARCH_URL =
            "https://www.googleapis.com/books/v1/volumes?key=%s&q=%s&fields=items(id,selfLink,volumeInfo/title,volumeInfo/imageLinks/smallThumbnail)&startIndex=%d&maxResults=" + SearchManager.MAX_RESULTS;

    public static String createUrl(String key, String term, int startIndex) {
        return String.format(SearchManager.SEARCH_URL, key, term, startIndex * SearchManager.MAX_RESULTS);
    }

    private enum STATUS {
        STARTED,
        RUNNING,
        FINISHED,
        FAILED
    };

    private Context context;
    private ListeningExecutorService mExecutorService;
    private STATUS mCurrentStatus = STATUS.FINISHED;

    @Inject
    public SearchManager() {
        mExecutorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(NUM_THREADS));
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void startSearch(final List<SearchTask> tasks,
                            final MainActivity.BookItemListCallback callback,
                            final Handler handler,
                            final Boolean resetScrollPosition)
    {
        switch(mCurrentStatus) {
            case STARTED:
            case RUNNING:
                return;
        }

        mCurrentStatus = STATUS.STARTED;

        final CountDownLatch latch = new CountDownLatch(tasks.size());

        Thread t = new Thread() {
            public void run() {
                mCurrentStatus = STATUS.RUNNING;

                for(SearchTask task: tasks) {
                    ListenableFuture<BookListItems> result = mExecutorService.submit(task);
                    callback.setLatch(latch);
                    Futures.addCallback(result, callback);
                }

                try {
                    latch.await(10, TimeUnit.SECONDS);

                    Bundle data = new Bundle();
                    data.putBoolean("resetscroll", resetScrollPosition);

                    Message msg = handler.obtainMessage(SearchManager.ALL_TASK_COMPLETE);
                    msg.setData(data);
                    msg.sendToTarget();
                } catch (InterruptedException e) {
                    mCurrentStatus = STATUS.FAILED;
                } catch (Exception e) {
                    mCurrentStatus = STATUS.FAILED;
                } finally {
                    mCurrentStatus = STATUS.FINISHED;
                }
            }
        };

        t.start();
    }

    public SearchTask getSearchTask(String term, int startIndex)
    {
        String key = getContext().getResources().getString(R.string.google_books_api_key);
        return new SearchTask(getContext(), createUrl(key, term, startIndex), startIndex);
    }

}
