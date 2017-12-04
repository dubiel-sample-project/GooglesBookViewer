package com.dubiel.sample.googlebookviewer;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.dubiel.sample.googlebookviewer.search.*;
import com.dubiel.sample.googlebookviewer.search.searchitem.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.FutureCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class MainActivity extends AppCompatActivity
        implements DrawerListAdapter.OnDrawerItemClickListener,
        OnPartialResultsReadyListener,
        OnResultsReadyListener {

    static final private String TAG = "MainActivity";
    static final private int CACHE_MAX_SIZE = 5;

    private final MessageHandler messageHandler = new MessageHandler(this);

    private Cache<Integer, BookListItems> bookListItemsCache;

    @Inject
    SearchManager searchManager;

    private BookItemListAdapter bookItemListAdapter;
    private RecyclerView drawerList;
    private String currentSearchTerm;
    private ProgressBar spinner;
    private Boolean cacheLoading = false;

    private static class MessageHandler extends Handler {
        private final WeakReference<MainActivity> mainActivityWeakReference;

        public MessageHandler(MainActivity mainActivity) {
            mainActivityWeakReference = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mainActivityWeakReference.get();
            if(null != mainActivity) {
                Bundle data = msg.getData();
                switch (msg.what) {
                    case SearchManager.TASK_COMPLETE:
                        mainActivity.onPartialResultsReady(data.getInt("current"), data.getInt("max"),
                                data.getInt("totalresults"), data.getInt("startindex"));
                        break;
                    case SearchManager.ALL_TASK_COMPLETE:
                        mainActivity.onResultsReady(data.getBoolean("resetscroll"));
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

            if(result.getItems().length == 0) {
                return;
            }

            bookListItemsCache.put(result.getStartIndex(), result);
            latch.countDown();

            Bundle data = new Bundle();
            data.putInt("current", (int) latch.getCount());
            data.putInt("max", max);
            data.putInt("totalresults", result.getItems().length);
            data.putInt("startindex", result.getStartIndex());

            Message msg = messageHandler.obtainMessage(SearchManager.TASK_COMPLETE);
            msg.setData(data);
            msg.sendToTarget();
        }

        public void onFailure(Throwable thrown) {
            latch.countDown();
        }
    }

    public String getCurrentSearchTerm() {
        return currentSearchTerm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidInjection.inject(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (RecyclerView) findViewById(R.id.left_drawer);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        drawerList.setLayoutManager(linearLayoutManager);

        drawerList.setHasFixedSize(true);

        String[] popularSearchTerms = getResources().getStringArray(R.array.category_array);
        currentSearchTerm = popularSearchTerms[0];

        drawerList.setAdapter(new DrawerListAdapter(popularSearchTerms, this));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(R.string.popular_searches);
                invalidateOptionsMenu();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.book_item_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//                int totalItem = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

//                System.out.println("dx: " + dx);
//                System.out.println("dy: " + dy);
//                System.out.println("firstVisibleItemPosition: " + firstVisibleItemPosition);

                if (!cacheLoading) {
                    if (dy < 0) {
                        int cacheKey = (int)Math.floor((firstVisibleItemPosition - 1) / SearchManager.MAX_RESULTS);
                        if(!(bookListItemsCache.getIfPresent(cacheKey) instanceof BookListItems)) {
                            updateCache(firstVisibleItemPosition - 1);
                        }
                    } else if (dy > 0) {
                        int cacheKey = (int)Math.floor((lastVisibleItem + 1) / SearchManager.MAX_RESULTS);
                        if(!(bookListItemsCache.getIfPresent(cacheKey) instanceof BookListItems)) {
                            updateCache(lastVisibleItem + 1);
                        }
                    }
                }
            }
        });

        bookListItemsCache = CacheBuilder.newBuilder()
                .maximumSize(CACHE_MAX_SIZE)
                .removalListener(new RemovalListener<Integer, BookListItems>() {
                    public void onRemoval(RemovalNotification<Integer, BookListItems> removalNotification) {
                        updateBookItemListAdapterItemCount();
                    }
                })
                .build();

        searchManager.setContext(this);

        bookItemListAdapter = new BookItemListAdapter(getApplicationContext(), bookListItemsCache);
        recyclerView.setAdapter(bookItemListAdapter);

        search();
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
        getMenuInflater().inflate(R.menu.main, menu);

        android.app.SearchManager searchManager = (android.app.SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchTerm = query;
                search();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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

    @Override
    public void onDrawerItemClick(View view, int position) {
        String categoryString = getResources().getStringArray(R.array.category_array)[position];

        currentSearchTerm = categoryString;
        search();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void onResultsReady(boolean resetScrollPosition) {
        cacheLoading = false;
        spinner.setVisibility(View.GONE);
        bookItemListAdapter.notifyDataSetChanged();

        if(resetScrollPosition) {
            ((RecyclerView) findViewById(R.id.book_item_list_recycler_view)).scrollToPosition(0);
        }
    }

    public void onPartialResultsReady(int current, int max, int totalResults, int startIndex) {
        cacheLoading = false;
        updateBookItemListAdapterItemCount();
    }

    private void search() {
        spinner.setVisibility(View.VISIBLE);
        bookListItemsCache.invalidateAll();

        List<SearchTask> tasks = new ArrayList<>();
        for(int i = 0; i < CACHE_MAX_SIZE; i++) {
            tasks.add(searchManager.getSearchTask(currentSearchTerm, i));
        }

        BookItemListCallback callback = new BookItemListCallback(tasks.size());
        searchManager.startSearch(tasks, callback, messageHandler, true);
    }

    private void updateCache(int key) {
        if(cacheLoading) {
            return;
        }

        cacheLoading = true;
        int cacheKey = (int)Math.floor(key / SearchManager.MAX_RESULTS);

//        System.out.println("key: " + key);
//        System.out.println("cacheKey: " + cacheKey);
//        System.out.println("cache.size: " + bookListItemsCache.size());

//        BookListItems bookListItems = bookListItemsCache.getIfPresent(cacheKey);
        if(bookListItemsCache.getIfPresent(cacheKey) instanceof BookListItems) {
            cacheLoading = false;
            spinner.setVisibility(View.GONE);
            return;
        }

        spinner.setVisibility(View.VISIBLE);

//        String url = SearchManager.createUrl(currentSearchTerm, key);
//        System.out.println("updateCache, url: " + url);

        List<SearchTask> tasks = new ArrayList<>();
        tasks.add(searchManager.getSearchTask(currentSearchTerm, cacheKey));

        BookItemListCallback callback = new BookItemListCallback(tasks.size());
        searchManager.startSearch(tasks, callback, messageHandler, false);
    }

    private void updateBookItemListAdapterItemCount() {
        if(bookListItemsCache.asMap().keySet().size() == 0) {
            bookItemListAdapter.setItemCount(0);
            return;
        }

        int maxKey = Collections.max(bookListItemsCache.asMap().keySet());

//        System.out.println("updateBookItemListAdapterItemCount, maxKey: " + maxKey);
//        System.out.println("updateBookItemListAdapterItemCount, getifpresent: " + (bookListItemsCache.getIfPresent(maxKey) instanceof BookListItems));

        int itemCount = maxKey * SearchManager.MAX_RESULTS;
        BookListItems bookListItems = bookListItemsCache.getIfPresent(maxKey);
        if(bookListItems instanceof BookListItems) {
//            System.out.println("updateBookItemListAdapterItemCount, length: " + bookListItemsCache.getIfPresent(maxKey).getItems().length);
            itemCount += bookListItems.getItems().length;
        }
//        System.out.println("updateBookItemListAdapterItemCount, itemCount: " + itemCount);

        bookItemListAdapter.setItemCount(itemCount);
    }
}
