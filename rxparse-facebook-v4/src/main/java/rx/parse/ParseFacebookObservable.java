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

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.Collection;

import rx.Observable;
import rx.bolts.TaskObservable;

public class ParseFacebookObservable {

    public static Observable<ParseUser> link(ParseUser user, com.facebook.AccessToken accessToken) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.linkInBackground(user, accessToken))
            .map(v -> user);
    }

    public static Observable<ParseUser> linkWithPublishPermissions(ParseUser user, Activity activity, Collection<String> permissions) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.linkWithPublishPermissionsInBackground(user, activity, permissions))
            .map(v -> user);
    }

    public static Observable<ParseUser> linkWithPublishPermissions(ParseUser user, Fragment fragment, Collection<String> permissions) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.linkWithPublishPermissionsInBackground(user, fragment, permissions))
            .map(v -> user);
    }

    public static Observable<ParseUser> linkWithReadPermissions(ParseUser user, Activity activity, Collection<String> permissions) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.linkWithReadPermissionsInBackground(user, activity, permissions))
            .map(v -> user);
    }

    public static Observable<ParseUser> linkWithReadPermissions(ParseUser user, Fragment fragment, Collection<String> permissions) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.linkWithReadPermissionsInBackground(user, fragment, permissions))
            .map(v -> user);
    }

    public static Observable<ParseUser> logIn(com.facebook.AccessToken accessToken) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.logInInBackground(accessToken));
    }

    public static Observable<ParseUser> logInWithPublishPermissions(Activity activity, Collection<String> permissions) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.logInWithPublishPermissionsInBackground(activity, permissions));
    }

    public static Observable<ParseUser> logInWithPublishPermissions(Fragment fragment, Collection<String> permissions) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.logInWithPublishPermissionsInBackground(fragment, permissions));
    }

    public static Observable<ParseUser> logInWithReadPermissions(Activity activity, Collection<String> permissions) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.logInWithReadPermissionsInBackground(activity, permissions));
    }

    public static Observable<ParseUser> logInWithReadPermissions(Fragment fragment, Collection<String> permissions) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.logInWithReadPermissionsInBackground(fragment, permissions));
    }

    public static Observable<ParseUser> unlink(ParseUser user) {
        return TaskObservable.deferNullable(() -> ParseFacebookUtils.unlinkInBackground(user))
            .map(v -> user);
    }

}
