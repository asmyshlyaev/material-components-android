/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.design.widget;

import static android.support.design.testutils.AppBarLayoutMatchers.isCollapsed;
import static android.support.design.testutils.SwipeUtils.swipeUp;
import static android.support.design.testutils.TestUtilsMatchers.hasZ;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.design.testapp.AppBarLayoutCollapsePinActivity;
import android.support.design.testapp.R;
import android.support.design.testutils.PollingCheck;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppBarWithCollapsingToolbarStateRestoreTest {
  @Rule
  public final ActivityTestRule<AppBarLayoutCollapsePinActivity> activityTestRule =
      new ActivityTestRule<>(AppBarLayoutCollapsePinActivity.class);

  private int oldOrientation;

  @Before
  public void setUp() {
    oldOrientation = activityTestRule.getActivity().getResources().getConfiguration().orientation;
  }

  @After
  public void tearDown() {
    activityTestRule
        .getActivity()
        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    PollingCheck.waitFor(
        new PollingCheck.PollingCheckCondition() {
          private final Activity activity = activityTestRule.getActivity();

          @Override
          public boolean canProceed() {
            return activity.getResources().getConfiguration().orientation == oldOrientation;
          }
        });
  }

  @Test
  public void testRotateAndRestore() {
    Activity activity = activityTestRule.getActivity();
    final AppBarLayout appBar = (AppBarLayout) activity.findViewById(R.id.app_bar);

    // Swipe up and collapse the AppBarLayout
    onView(withId(R.id.coordinator_layout))
        .perform(
            swipeUp(
                appBar.getLeft() + (appBar.getWidth() / 2),
                appBar.getBottom() + 20,
                appBar.getHeight()));
    onView(withId(R.id.app_bar)).check(matches(hasZ())).check(matches(isCollapsed()));

    // Now rotate the Activity
    final int newOrientation =
        oldOrientation == Configuration.ORIENTATION_PORTRAIT
            ? Configuration.ORIENTATION_LANDSCAPE
            : Configuration.ORIENTATION_PORTRAIT;
    activity.setRequestedOrientation(
        newOrientation == Configuration.ORIENTATION_PORTRAIT
            ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    PollingCheck.waitFor(
        new PollingCheck.PollingCheckCondition() {
          @Override
          public boolean canProceed() {
            return activity.getResources().getConfiguration().orientation == newOrientation;
          }
        });

    // And check that the app bar still is restored correctly
    onView(withId(R.id.app_bar)).check(matches(hasZ())).check(matches(isCollapsed()));
  }
}
