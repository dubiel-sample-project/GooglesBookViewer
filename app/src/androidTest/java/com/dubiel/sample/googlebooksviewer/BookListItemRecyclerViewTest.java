package com.dubiel.sample.googlebooksviewer;


import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.os.SystemClock;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.dubiel.sample.googlebookviewer.MainActivity;
import com.dubiel.sample.googlebookviewer.R;
import com.dubiel.sample.googlebookviewer.bookdetail.BookDetailActivity;

import org.hamcrest.Matcher;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class BookListItemRecyclerViewTest {

    private static final int ITEM_POSITION = 1;

    private String searchString;
    private String selfLink;
    private String infoLink;

    @Rule
    public IntentsTestRule<MainActivity> activityRule = new IntentsTestRule<>(
            MainActivity.class);

    @Before
    public void initValidString() {
        searchString = "Cats";
        selfLink = "https://www.googleapis.com/books/v1/volumes/VNGKFkvmvM0C";
        infoLink = "https://play.google.com/store/books/details?id=VNGKFkvmvM0C&source=gbs_api";
    }

    @Test
    public void checkBookItemListHasSearchString() {
        SystemClock.sleep(5000);
        onView(withId(R.id.book_item_list_recycler_view))
                .check(matches(hasDescendant(withText(searchString))));
    }

    @Test
    public void scrollToItemPosition_checkBookDetailActivityIntent() {
        onView(ViewMatchers.withId(R.id.book_item_list_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(ITEM_POSITION, click()));
        intended(hasComponent(new ComponentName(getTargetContext(), BookDetailActivity.class)));
    }

    @Test
    public void scrollToItemPosition_checkBookDetailActivityBundleArgument() {
        Matcher expectedIntent = allOf(hasAction(Intent.ACTION_VIEW), hasData(selfLink));
        intending(expectedIntent).respondWith(new Instrumentation.ActivityResult(0, null));
        onView(ViewMatchers.withId(R.id.book_item_list_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        SystemClock.sleep(5000);
        onView(withId(R.id.book_detail_item_info_link))
                .check(matches(withText(infoLink)));
    }

}