package com.dubiel.sample.googlesbookviewer;

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
import android.widget.SearchView;

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
        implements DrawerListAdapter.OnDrawerItemClickListener,
        OnPartialResultsReadyListener,
        OnResultsReadyListener {

    static final private String TAG = "MainActivity";

    private final MessageHandler messageHandler = new MessageHandler(this);

//    private ConcurrentHashMap<String, BookListItem> bookListItems;
    private LoadingCache<Integer, BookListItems> bookListItemsCache;

    private SearchManager searchManager;
    private BookItemListAdapter bookItemListAdapter;
    private RecyclerView drawerList;
    private String currentSearchTerm;

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
            System.out.println("booklistitems: " + result.getStartIndex());

            if (result == null) {
                latch.countDown();
                return;
            }

            bookListItemsCache.put(result.getStartIndex(), result);

//            BookListItem[] resultBookListItems = result.getItems();
//
//            int len = resultBookListItems.length;
//            for (int i = 0; i < len; i++) {
//                if(!bookListItems.containsKey(resultBookListItems[i].getId())) {
//                    bookListItems.put(resultBookListItems[i].getId(), resultBookListItems[i]);
//                }
//            }

//            Log.i(TAG, "thread id done: " + Long.toString(Thread.currentThread().getId()));
//            Log.i(TAG, "thread name done: " + Thread.currentThread().getName());
//            Log.i(TAG, "latch count: " + latch.getCount());

            latch.countDown();

            Bundle data = new Bundle();
            data.putInt("current", (int) latch.getCount());
            data.putInt("max", max);
            data.putInt("totalresults", result.getItems().length);

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
        drawerList = (RecyclerView) findViewById(R.id.left_drawer);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        drawerList.setLayoutManager(linearLayoutManager);

//        mDrawerLayout.setDrawerShadow(R.mipmap.drawer_shadow, GravityCompat.START);
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

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.book_item_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

//        bookListItems = new ConcurrentHashMap<>();

        bookListItemsCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(
                        new CacheLoader<Integer, BookListItems>() {
                            public BookListItems load(Integer key) throws Exception {
                                int startIndex = key * SearchManager.MAX_RESULTS;
                                String url = String.format(SearchManager.SEARCH_URL, currentSearchTerm, startIndex);
//                                "https://www.googleapis.com/books/v1/volumes?q=car&fields=items(id,selfLink,volumeInfo/title,volumeInfo/imageLinks/smallThumbnail)&startIndex="+startIndex+"&maxResults=" + SearchManager.MAX_RESULTS;
//                                SearchTask searchTask = new SearchTask(getApplicationContext(), url);
//                                return getBookListItems(integer);
                                BookListItems result = Ion.with(getApplicationContext())
                                        .load(url)
                                        .as(new TypeToken<BookListItems>() {
                                        }).get();
                                bookItemListAdapter.notifyDataSetChanged();

                                return result;
                            }
                        });

        searchManager = SearchManager.getInstance();
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
                System.out.println("onQueryTextSubmit: " + query);
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

//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        System.out.println("onNavigationItemSelected: " + id);
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }

    @Override
    public void onDrawerItemClick(View view, int position) {
        setTitle(getResources().getStringArray(R.array.category_array)[position]);

        String categoryString = getResources().getStringArray(R.array.category_array)[position];
        System.out.println("onDrawerItemClick: " + categoryString);

        currentSearchTerm = categoryString;
        search();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void onResultsReady() {
        System.out.println("MainActivity.onResultsReady");
//        bookItemListAdapter.setBookListItems(bookListItemsCache);
        bookItemListAdapter.notifyDataSetChanged();
        ((RecyclerView)findViewById(R.id.book_item_list_recycler_view)).scrollToPosition(0);
    }

    public void onPartialResultsReady(int current, int max, int totalResults) {
        System.out.println("MainActivity.onPartialResultsReady, totalResults: " + totalResults);
    }

    private void search() {
        bookListItemsCache.invalidateAll();

        List<SearchTask> tasks = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            tasks.add(SearchManager.getInstance().getSearchTask(currentSearchTerm, i));
        }

        BookItemListCallback callback = new BookItemListCallback(tasks.size());
        SearchManager.getInstance().startSearch(tasks, callback, messageHandler);
    }
}
