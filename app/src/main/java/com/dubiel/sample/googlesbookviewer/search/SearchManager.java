package com.dubiel.sample.googlesbookviewer.search;

import android.content.Context;
import android.os.Handler;

import com.dubiel.sample.googlesbookviewer.MainActivity;
import com.dubiel.sample.googlesbookviewer.search.searchitem.BookListItem;
import com.dubiel.sample.googlesbookviewer.search.searchitem.BookListItems;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SearchManager {

    static final public int TASK_COMPLETE = 1;
    static final public int ALL_TASK_COMPLETE = 2;

    static final public int MAX_RESULTS = 40;

//    public enum CATEGORY {
//        ASSAULT,
//        VEHICLE_THEFT,
//        THEFT,
//        DRUG_NARCOTIC,
//        BURGLARY,
//        ROBBERY,
//        TRESPASSING,
//        FRAUD,
//        MISSING_PERSON,
//        DRUNKENNESS,
//        KIDNAPPING,
//        ALL
//    };
//
//    public enum DATE_RANGE {
//        TWO_WEEKS,
//        FOUR_WEEKS,
//        THREE_MONTHS,
//        SIX_MONTHS,
//        ONE_YEAR,
//        ALL
//    };

    private enum STATUS {
        STARTED,
        RUNNING,
        FINISHED,
        FAILED
    };

    static final private String TAG = "searchmanager";
    static final private int NUM_THREADS = 10;

    static private SearchManager sInstance = null;

    private Context context;
    private ListeningExecutorService mExecutorService;
//    private List<ListenableFuture<List<BookListItem>>> listenableFutures;
    private STATUS mCurrentStatus = STATUS.FINISHED;

    static {
        sInstance = new SearchManager();
    }

    public static SearchManager getInstance() {
        return sInstance;
    }

    private SearchManager() {
        mExecutorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(NUM_THREADS));
//        listenableFutures = new ArrayList<>();
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void startSearch(final List<SearchTask> tasks, final MainActivity.BookItemListCallback callback, final Handler handler)
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
                    /*
                    Log.i("mapmanager", "thread id: " + Long.toString(Thread.currentThread().getId()));
                    Log.i("mapmanager", "thread name: " + Thread.currentThread().getName());
                    Log.i("mapmanager", "before await");
                    Log.i("mapmanager", "latch count: " + latch.getCount());
                    */

                    latch.await(10, TimeUnit.SECONDS);

                    //Log.i("mapmanager", "latch count: " + latch.getCount());
                    //Log.i(TAG, "after await");
                    handler.obtainMessage(SearchManager.ALL_TASK_COMPLETE).sendToTarget();
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

    public SearchTask getSearchTask(int startIndex)
    {
        String url = "https://www.googleapis.com/books/v1/volumes?q=car&fields=items(id,selfLink,volumeInfo/title,volumeInfo/imageLinks/smallThumbnail)&startIndex="+(startIndex * MAX_RESULTS)+"&maxResults="+MAX_RESULTS;
        System.out.println("SearchManager::getSearchTask, url: " + url);
        return new SearchTask(getContext(), url, startIndex);
    }

//    public List<BookListItem> getResults(FutureCallback<List<List<BookListItem>>> callback) {
//        //Log.i(TAG, "thread id: " + Long.toString(Thread.currentThread().getId()));
//        //Log.i(TAG, "thread name: " + Thread.currentThread().getName());
//        //Log.i(TAG, "listenableFutures.size: " + listenableFutures.size());
//
//        final List<BookListItem> successfulResults = new ArrayList<>();
//
//        ListenableFuture<List<List<BookListItem>>> lf = Futures.successfulAsList(listenableFutures);
//
////        try {
////            List<List<Item>> results = lf.get();
////            for(List<Item> result: results) {
////                for(Item item : result) {
////                    Log.i(TAG, "pdid: " + item.pdid);
////                    successfulResults.add(item);
////                }
////                //listenableFutures.remove(result);
////            }
////        } catch(InterruptedException | ExecutionException e) {
////            Log.e(TAG, e.getLocalizedMessage());
////        }
////
////        return successfulResults;
//
//        Futures.addCallback(lf, callback);
//
////        Futures.addCallback(lf, new FutureCallback<List<List<GsonItem>>>() {
////            @Override
////            public void onSuccess(List<List<GsonItem>> results) {
////                Log.i(TAG, "thread id: " + Long.toString(Thread.currentThread().getId()));
////                Log.i(TAG, "thread name: " + Thread.currentThread().getName());
////
////                for(List<GsonItem> result: results) {
////                    for(GsonItem item : result) {
////                        Log.i(TAG, "pdid: " + item.pdid);
////                        successfulResults.add(item);
////                    }
//////                    listenableFutures.remove(result);
////                }
////            }
////
////            @Override
////            public void onFailure(Throwable t) {
////            }
////        });
//
//        return successfulResults;
//    }
}
