/*
 * Copyright (c) 2015-present, 8tory. Inc.
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.parse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import bolts.Task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static mocker.Mocker.mocker;

// Avoid cannot be accessed from outside package
public class ParseObservableTest {

    @Before
    public void setUp() {
        ParseTestUtils.setTestParseUser();
        ParseObject.registerSubclass(ParseUser.class);
    }

    @After
    public void tearDown() {
        ParseObject.unregisterSubclass(ParseUser.class);
        ParseCorePlugins.getInstance().reset();
        Parse.disableLocalDatastore();
    }

    @Test
    public void testParseObservableAllNextAfterCompleted() {
        ParseQueryController queryController = mock(ParseQueryController.class);
        ParseCorePlugins.getInstance().registerQueryController(queryController);

        List<ParseUser> users = Arrays.asList(
                mocker(ParseUser.class).when(user -> user.getObjectId()).thenReturn(user -> "1_" + user.hashCode()).mock(),
                mocker(ParseUser.class).when(user -> user.getObjectId()).thenReturn(user -> "2_" + user.hashCode()).mock(),
                mocker(ParseUser.class).when(user -> user.getObjectId()).thenReturn(user -> "3_" + user.hashCode()).mock());

        Task<List<ParseUser>> task = Task.forResult(users);
        when(queryController.findAsync(
                    any(ParseQuery.State.class),
                    any(ParseUser.class),
                    any(Task.class))
            ).thenReturn(task);
        when(queryController.countAsync(
                    any(ParseQuery.State.class),
                    any(ParseUser.class),
                    any(Task.class))).thenReturn(Task.<Integer>forResult(users.size()));

        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.setUser(new ParseUser());

        rx.assertions.RxAssertions.assertThat(rx.parse.ParseObservable.all(query))
            .withoutErrors()
            .expectedValues(users)
            .completes();

        try {
            ParseTaskUtils.wait(task);
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    public void testParseObservableFindNextAfterCompleted() {
        List<ParseUser> users = Arrays.asList(mock(ParseUser.class), mock(ParseUser.class), mock(ParseUser.class));
        ParseQueryController queryController = mock(ParseQueryController.class);
        ParseCorePlugins.getInstance().registerQueryController(queryController);

        Task<List<ParseUser>> task = Task.forResult(users);
        when(queryController.findAsync(
                    any(ParseQuery.State.class),
                    any(ParseUser.class),
                    any(Task.class))
            ).thenReturn(task);
            //).thenThrow(IllegalStateException.class);

        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.setUser(new ParseUser());

        rx.assertions.RxAssertions.assertThat(rx.parse.ParseObservable.find(query))
            .withoutErrors()
            .expectedValues(users)
            .completes();

        try {
            ParseTaskUtils.wait(task);
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    public void testBlockingFind() {
        ParseQueryController queryController = mock(ParseQueryController.class);
        ParseCorePlugins.getInstance().registerQueryController(queryController);
        Task<List<ParseUser>> task = Task.forResult(Arrays.asList(mock(ParseUser.class), mock(ParseUser.class), mock(ParseUser.class)));
        when(queryController.findAsync(any(ParseQuery.State.class), any(ParseUser.class), any(Task.class))).thenReturn(task);

        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);

        try {
            assertEquals(query.find(), rx.parse.ParseObservable.find(query).toList().toBlocking().single());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
