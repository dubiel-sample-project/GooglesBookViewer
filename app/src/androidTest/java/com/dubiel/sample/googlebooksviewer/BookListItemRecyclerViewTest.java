package com.dubiel.sample.googlebooksviewer;


import android.content.ComponentName;
import android.os.SystemClock;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.dubiel.sample.googlebookviewer.BookItemListAdapter;
import com.dubiel.sample.googlebookviewer.MainActivity;
import com.dubiel.sample.googlebookviewer.R;
import com.dubiel.sample.googlebookviewer.bookdetail.BookDetailActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class BookListItemRecyclerViewTest {

    private static final int ITEM_POSITION = 1;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Rule
    public ActivityTestRule<BookDetailActivity> bookDetailActivityRule = new ActivityTestRule<>(
            BookDetailActivity.class);

    @Test
        public void scrollToItemPosition_checkBookDetailActivityIntent() {
        onView(ViewMatchers.withId(R.id.book_item_list_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(ITEM_POSITION, click()));
        SystemClock.sleep(5000);
        intended(hasComponent(new ComponentName(getTargetContext(), BookDetailActivity.class)));

    }

    @Test
    public void scrollToItemPosition_checkBookDetailActivityText() {
        onView(ViewMatchers.withId(R.id.book_item_list_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(ITEM_POSITION, click()));
        SystemClock.sleep(5000);

//        String itemElementText = activityRule.getActivity().getResources().getString(
//            R.string.) + String.valueOf(ITEM_BELOW_THE_FOLD);
//        String itemElementText = bookDetailActivityRule.getActivity().getResources().getString(
//                R.string.) + String.valueOf(ITEM_BELOW_THE_FOLD);
//        onView(ViewMatchers.withId(R.id.book_detail_item_title).).check(matches(isDisplayed()));
    }

//    @Test
//    public void scrollToItemPosition_checkBookDetailActivityBundleArgument() {
//        onView(ViewMatchers.withId(R.id.book_item_list_recycler_view))
//                .perform(RecyclerViewActions.actionOnItemAtPosition(ITEM_POSITION, click()));
//        SystemClock.sleep(5000);
//        intended(hasComponent(new ComponentName(getTargetContext(), BookDetailActivity.class)));
//    }

//    @Test
//    public void itemInMiddleOfList_hasSpecialText() {
//        // First, scroll to the view holder using the isInTheMiddle matcher.
////        onView(ViewMatchers.withId(R.id.recyclerView))
////                .perform(RecyclerViewActions.scrollToHolder(isInTheMiddle()));
////
////        // Check that the item has the special text.
////        String middleElementText =
////                mActivityRule.getActivity().getResources().getString(R.string.middle);
////        onView(withText(middleElementText)).check(matches(isDisplayed()));
//    }

//    private static Matcher<BookItemListAdapter.ViewHolder> isInTheMiddle() {
//        return new TypeSafeMatcher<BookItemListAdapter.ViewHolder>() {
//            @Override
//            protected boolean matchesSafely(BookItemListAdapter.ViewHolder bookItemListAdapterHolder) {
////                return bookItemListAdapterHolder.getTitle().getText();
//            }
//
//            @Override
//            public void describeTo(Description description) {
//                description.appendText("item in the middle");
//            }
//        };
//    }
}