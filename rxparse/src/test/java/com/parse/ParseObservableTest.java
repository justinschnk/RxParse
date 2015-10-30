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
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import bolts.Continuation;
import bolts.Task;

import static org.hamcrest.CoreMatchers.instanceOf;
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

import rx.functions.*;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.*;
import java.util.Collections;

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
  public void testParseObservableFindNextAfterCompleted() {
    ParseUser user = mock(ParseUser.class);
    ParseQueryController queryController = mock(ParseQueryController.class);
    ParseCorePlugins.getInstance().registerQueryController(queryController);

    Task<List<ParseUser>> task = Task.forResult(Collections.singletonList(user));
    when(queryController.findAsync(
            any(ParseQuery.State.class),
            any(ParseUser.class),
            any(Task.class))
    ).thenReturn(task);

    ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
    query.setUser(new ParseUser());

    final AtomicBoolean completed = new AtomicBoolean(false);
    rx.parse.ParseObservable.all(query)
        //.observeOn(Schedulers.newThread())
        //.subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<ParseObject>() {
        @Override public void call(ParseObject it) {
            System.out.println("onNext: " + it);
            if (completed.get()) {
                fail("Should've onNext after completed.");
            }
        }
    }, new Action1<Throwable>() {
        @Override public void call(Throwable e) {
            System.out.println("onError: " + e);
        }
    }, new Action0() {
        @Override public void call() {
            System.out.println("onCompleted");
            completed.set(true);
        }
    });

    try {
        ParseTaskUtils.wait(task);
    } catch (Exception e) {
        // do nothing
    }
  }

}
