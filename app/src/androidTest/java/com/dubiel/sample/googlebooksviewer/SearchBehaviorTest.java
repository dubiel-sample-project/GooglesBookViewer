package com.dubiel.sample.googlebooksviewer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.SystemClock;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.widget.EditText;

import com.dubiel.sample.googlebookviewer.MainActivity;
import com.dubiel.sample.googlebookviewer.R;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SearchBehaviorTest {

    private String searchString;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void initValidString() {
        searchString = "java";
    }

    @Test
    public void changeSearchText_sameActivity() {
        onView(withId(R.id.search)).perform(click());
        onView(isAssignableFrom(EditText.class))
                .perform(typeText(searchString), pressKey(KeyEvent.KEYCODE_ENTER));
        SystemClock.sleep(2000);

        onView(isAssignableFrom(EditText.class))
                .check(matches(withText(searchString)));
        onView(isAssignableFrom(EditText.class))
                .check(matches(withText(activityRule.getActivity().getCurrentSearchTerm())));
    }
}
