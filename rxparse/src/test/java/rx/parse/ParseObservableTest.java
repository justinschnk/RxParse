/*
 * Copyright (C) 2015 8tory, Inc
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

package rx.parse;

import com.parse.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
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
import mocker.Mocker;

public class ParseObservableTest {

    @Test
    public void testParseObservableAllNextAfterCompleted() {
        List<ParseUser> users = Arrays.asList(
                Mocker.of(ParseUser.class).when(user -> user.getObjectId()).thenReturn(user -> "1_" + user.hashCode()).mock(),
                Mocker.of(ParseUser.class).when(user -> user.getObjectId()).thenReturn(user -> "2_" + user.hashCode()).mock(),
                Mocker.of(ParseUser.class).when(user -> user.getObjectId()).thenReturn(user -> "3_" + user.hashCode()).mock());

        rx.assertions.RxAssertions.assertThat(rx.parse.ParseObservable.all(Mocker.of(ParseQuery.class)
                    .when(query -> query.countInBackground()).thenReturn(query -> Task.forResult(users.size()))
                    .when(query -> query.findInBackground()).thenReturn(query -> Task.forResult(users))
                    .when(query -> query.setSkip(any(int.class))).thenReturn(query -> null)
                    .when(query -> query.setLimit(any(int.class))).thenReturn(query -> null).mock()))
            .withoutErrors()
            .expectedValues(users)
            .completes();
    }

    @Test
    public void testParseObservableFindNextAfterCompleted() {
        List<ParseUser> users = Arrays.asList(mock(ParseUser.class), mock(ParseUser.class), mock(ParseUser.class));
        ParseQuery<ParseUser> query = (ParseQuery<ParseUser>) mock(ParseQuery.class);
        when(query.findInBackground()).thenReturn(Task.forResult(users));

        rx.assertions.RxAssertions.assertThat(rx.parse.ParseObservable.find(query))
            .withoutErrors()
            .expectedValues(users)
            .completes();
    }

    @Test
    public void testBlockingFind() {
        List<ParseUser> users = Arrays.asList(mock(ParseUser.class), mock(ParseUser.class), mock(ParseUser.class));
        ParseQuery<ParseUser> query = (ParseQuery<ParseUser>) mock(ParseQuery.class);
        when(query.findInBackground()).thenReturn(Task.forResult(users));
        try {
            when(query.find()).thenReturn(users);

            assertEquals(query.find(), rx.parse.ParseObservable.find(query).toList().toBlocking().single());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
