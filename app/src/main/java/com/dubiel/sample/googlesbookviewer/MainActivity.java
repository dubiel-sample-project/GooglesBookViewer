package com.dubiel.sample.googlesbookviewer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dubiel.sample.googlesbookviewer.search.*;
import com.dubiel.sample.googlesbookviewer.search.searchitem.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnPartialResultsReadyListener,
        OnResultsReadyListener {

    static final private String TAG = "MainActivity";

    private final MessageHandler messageHandler = new MessageHandler(this);

    private ConcurrentHashMap<String, BookListItem> bookListItems;
    private LoadingCache<Integer, BookListItems> bookListItemsCache;

    private SearchManager mSearchManager;
    private BookItemListAdapter bookItemListAdapter;

    private static class MessageHandler extends Handler {
        private final WeakReference<MainActivity> mainActivityWeakReference;

        public MessageHandler(MainActivity mainActivity) {
            mainActivityWeakReference = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mainActivityWeakReference.get();
            if(null != mainActivity) {
                switch (msg.what) {
                    case SearchManager.TASK_COMPLETE:
                        Bundle data = msg.getData();
                        mainActivity.onPartialResultsReady(data.getInt("current"), data.getInt("max"), data.getInt("totalresults"));
                        break;
                    case SearchManager.ALL_TASK_COMPLETE:
                        mainActivity.onResultsReady();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }

    public class BookItemListCallback implements FutureCallback<BookListItems> {
        private CountDownLatch latch;
        private int max;

        public BookItemListCallback(int size) {
            max = size;
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        public void onSuccess(BookListItems result) {
            if (result == null) {
                latch.countDown();
                return;
            }

            BookListItem[] resultBookListItems = result.getItems();

            int len = resultBookListItems.length;
            for (int i = 0; i < len; i++) {
                if(!bookListItems.containsKey(resultBookListItems[i].getId())) {
                    bookListItems.put(resultBookListItems[i].getId(), resultBookListItems[i]);
                }
            }

            Log.i(TAG, "thread id done: " + Long.toString(Thread.currentThread().getId()));
            Log.i(TAG, "thread name done: " + Thread.currentThread().getName());
            Log.i(TAG, "latch count: " + latch.getCount());

            latch.countDown();

            Bundle data = new Bundle();
            data.putInt("current", (int) latch.getCount());
            data.putInt("max", max);
            data.putInt("totalresults", len);

            Message msg = messageHandler.obtainMessage(SearchManager.TASK_COMPLETE);
            msg.setData(data);
            msg.sendToTarget();
        }

        public void onFailure(Throwable thrown) {
            latch.countDown();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.book_item_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        bookItemListAdapter = new BookItemListAdapter(getApplicationContext());
        recyclerView.setAdapter(bookItemListAdapter);

        bookListItems = new ConcurrentHashMap<>();

        bookListItemsCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build(
                        new CacheLoader<Integer, BookListItems>() {
                            public BookListItems load(Integer key) throws Exception {
                                int startIndex = key * 40;
                                String url = "https://www.googleapis.com/books/v1/volumes?q=car&fields=items(id,selfLink,volumeInfo/title,volumeInfo/imageLinks/smallThumbnail)&startIndex="+startIndex+"&maxResults=40";
//                                SearchTask searchTask = new SearchTask(getApplicationContext(), url);
//                                return getBookListItems(integer);
                                return Ion.with(getApplicationContext())
                                        .load(url)
                                        .as(new TypeToken<BookListItems>() {
                                        }).get();
                            }
                        });

//        mSearchManager = SearchManager.getInstance();
//        mSearchManager.setContext(this);
//
//        doSearch();
        preloadCache();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onResultsReady() {
        System.out.println("MainActivity.onResultsReady, bookListItems.size: " + bookListItems.size());
        bookItemListAdapter.setBookListItems(bookListItems.values().toArray(new BookListItem[0]));
        bookItemListAdapter.notifyDataSetChanged();
    }

    public void onPartialResultsReady(int current, int max, int totalResults) {
        System.out.println("MainActivity.onPartialResultsReady, totalResults: " + totalResults);
    }

    private void doSearch() {
        List<SearchTask> tasks = new ArrayList<>();
        tasks.add(SearchManager.getInstance().getSearchTask());

        BookItemListCallback callback = new BookItemListCallback(tasks.size());
        SearchManager.getInstance().startSearch(tasks, callback, messageHandler);
    }

    private void preloadCache() {
        for(int i = 0; i < 5; i++) {
            try {
                bookListItemsCache.get(i);
            } catch(Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
