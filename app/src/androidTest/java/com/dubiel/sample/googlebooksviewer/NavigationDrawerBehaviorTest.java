package com.dubiel.sample.googlebooksviewer;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.SystemClock;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.EditText;

import com.dubiel.sample.googlebookviewer.MainActivity;
import com.dubiel.sample.googlebookviewer.R;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NavigationDrawerBehaviorTest {

    private String searchString;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void initValidString() {
        searchString = "London";
    }

    @Test
    public void openDrawer_sameActivity() {
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
    }

    @Test
    public void openDrawer_clickItem() {
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
        SystemClock.sleep(1000);

        onView(ViewMatchers.withId(R.id.left_drawer))
                .perform(RecyclerViewActions.actionOnItemAtPosition(5, click()));
    }

    @Test
    public void openDrawer_clickItemAndConfirmSearch() {
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
        SystemClock.sleep(1000);

        onView(ViewMatchers.withId(R.id.left_drawer))
                .perform(RecyclerViewActions.actionOnItemAtPosition(5, click()));
        SystemClock.sleep(5000);

        onView(withId(R.id.book_item_list_recycler_view))
                .check(matches(hasDescendant(withText(searchString))));
    }
}
