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

import bolts.Task;
import rx.schedulers.*;
import rx.Observable;
import rx.functions.*;
import rx.observables.*;

import com.parse.*;

import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.app.Activity;

public class ParseObservable<T extends ParseObject> {
    private Class<T> mSubClass;

    public ParseObservable(Class<T> subclass) {
        mSubClass = subclass;
    }

    @Deprecated
    public static <T extends ParseObject> ParseObservable<T> from(Class<T> subclass) {
        return of(subclass);
    }

    @Deprecated
    public static <T extends ParseObject> ParseObservable<T> of(Class<T> subclass) {
        return new ParseObservable<T>(subclass);
    }

    @Deprecated
    public ParseQuery<T> getQuery() {
        /* error: incompatible types: ParseQuery<ParseUser> cannot be converted to ParseQuery<T>
        if (mSubClass.equals(ParseUser.class)) {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            return query;
        }
        */
        return ParseQuery.getQuery(mSubClass);
    }

    @Deprecated
    public Observable<T> find() {
        return find(getQuery());
    }

    // Bolts2Rx
    // Bolts.Task2Observable
    // RxBolts
    // RxTask
    // BoltsObservable
    // TaskObservable
    public static <R> Observable<R> just(Task<R> task, boolean nullable) {
        return Observable.create(sub -> {
            task.continueWith(t -> {
                android.util.Log.d("ParseObservable", "task: " + task);
                if (t.isCancelled()) {
                    android.util.Log.d("ParseObservable", "cancel");
                    // NOTICE: doOnUnsubscribe(() -> Observable.just(query) in outside
                    sub.unsubscribe(); //sub.onCompleted();?
                } else if (t.isFaulted()) {
                    Throwable error = t.getError();
                    android.util.Log.d("ParseObservable", "error: " + error);
                    sub.onError(error);
                } else {
                    R r = t.getResult();
                    android.util.Log.d("ParseObservable", "r: " + r);
                    if (nullable || r != null) sub.onNext(r);
                    sub.onCompleted();
                }
                return null;
            });
        });
    }

    public static <R> Observable<R> justNullable(Task<R> task) {
        return just(task, true);
    }

    public static <R> Observable<R> just(Task<R> task) {
        return just(task, false);
    }

    @Deprecated
    public static <R> Observable<R> just(Func0<Task<R>> task) {
        return defer(task);
    }

    public static <R> Observable<R> defer(Func0<Task<R>> task) {
        return Observable.defer(() -> just(task.call()));
    }

    public static <R extends ParseObject> Observable<R> find(ParseQuery<R> query) {
        return Observable.defer(() -> just(query.findInBackground()))
                .flatMap(l -> Observable.from(l))
            .doOnUnsubscribe(() -> Observable.just(query)
                .doOnNext(q -> q.cancel())
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {}, e -> {}));
    }

    public static <R extends ParseObject> Observable<Integer> count(ParseQuery<R> query) {
        return Observable.defer(() -> just(query.countInBackground()))
            .doOnUnsubscribe(() -> Observable.just(query)
                .doOnNext(q -> q.cancel())
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {}, e -> {}));

    }

    @Deprecated
    public Observable<Integer> count() {
        return count(getQuery());
    }

    public static <R extends ParseObject> Observable<R> pin(R object) {
        return Observable.defer(() -> justNullable(object.pinInBackground()))
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> pin(List<R> objects) {
        return Observable.defer(() -> justNullable(ParseObject.pinAllInBackground(objects)))
                .flatMap(v -> Observable.from(objects));
    }

    public static <R extends ParseObject> Observable<R> pin(String name, R object) {
        return Observable.defer(() -> justNullable(object.pinInBackground(name)))
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> pin(String name, List<R> objects) {
        return Observable.defer(() -> justNullable(ParseObject.pinAllInBackground(name, objects)))
                .flatMap(v -> Observable.from(objects));
    }

    public static <R extends ParseObject> Observable<R> unpin(R object) {
        return Observable.defer(() -> justNullable(object.unpinInBackground()))
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> unpin(List<R> objects) {
        return Observable.defer(() -> justNullable(ParseObject.unpinAllInBackground(objects)))
                .flatMap(v -> Observable.from(objects));
    }

    public static <R extends ParseObject> Observable<R> unpin(String name, R object) {
        return Observable.defer(() -> justNullable(object.unpinInBackground(name)))
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> unpin(String name, List<R> objects) {
        return Observable.defer(() -> justNullable(ParseObject.unpinAllInBackground(name, objects)))
                .flatMap(v -> Observable.from(objects));
    }

    public static <R extends ParseObject> Observable<R> all(ParseQuery<R> query) {
        return count(query).flatMap(c -> all(query, c));
    }

    /** limit 10000 by skip */
    public static <R extends ParseObject> Observable<R> all(ParseQuery<R> query, int count) {
        final int limit = 1000; // limit limitation
        query.setSkip(0);
        query.setLimit(limit);
        Observable<R> find = find(query);
        for (int i = limit; i < count; i+= limit) {
            if (i >= 10000) break; // skip limitation
            query.setSkip(i);
            query.setLimit(limit);
            find.concatWith(find(query));
        }
        return find.distinct(o -> o.getObjectId());
    }

    public static <R extends ParseObject> Observable<R> first(ParseQuery<R> query) {
        return Observable.defer(() -> just(query.getFirstInBackground()))
            .doOnUnsubscribe(() -> Observable.just(query)
                .doOnNext(q -> q.cancel())
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {}, e -> {}));
    }

    @Deprecated
    public Observable<T> get(String objectId) {
        return get(mSubClass, objectId);
    }

    public static <R extends ParseObject> Observable<R> get(Class<R> clazz, String objectId) {
        ParseQuery<R> query = ParseQuery.getQuery(clazz);
        return Observable.defer(() -> just(query.getInBackground(objectId)))
            .doOnUnsubscribe(() -> Observable.just(query)
                .doOnNext(q -> q.cancel())
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {}, e -> {}));
    }

    @Deprecated
    /**
     * {@link ParseFacebookUtils.logInWithReadPermissionsInBackground}
     */
    public static Observable<ParseUser> loginWithFacebook(Activity activity, Collection<String> permissions) {
        return logInWithReadPermissionsInBackground(activity, permissions);
    }

    public static Observable<ParseUser> logInWithReadPermissionsInBackground(Activity activity, Collection<String> permissions) {
        // TODO
        return Observable.create(sub -> {
            ParseFacebookUtils.logIn(permissions, activity, Callbacks.login((user, e) -> {
                if (e != null) {
                    sub.onError(e);
                } else {
                    sub.onNext(user);
                    sub.onCompleted();
                }
            }));
        });
    }

    @Deprecated
    /**
     * {@link ParseFacebookUtils.logInWithReadPermissionsInBackground}
     */
    public static Observable<ParseUser> loginWithFacebook(Activity activity) {
        return loginWithFacebook(activity, Arrays.asList("public_profile", "email"));
    }

    public static <R> Observable<R> callFunction(String name, Map<String, R> params) {
        return Observable.defer(() -> justNullable(ParseCloud.callFunctionInBackground(name, params)));
    }

    public static <R extends ParseObject> Observable<R> save(R object) {
        return Observable.defer(() -> justNullable(object.saveInBackground()))
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> save(List<R> objects) {
        return Observable.defer(() -> justNullable(ParseObject.saveAllInBackground(objects)))
                .flatMap(v -> Observable.from(objects));
    }

    public static <R extends ParseObject> Observable<R> saveEventually(R object) {
        return Observable.defer(() -> justNullable(object.saveEventually()))
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> fetchIfNeeded(R object) {
        return Observable.defer(() -> justNullable(object.fetchIfNeededInBackground()));
    }

    public static <R extends ParseObject> Observable<R> fetchIfNeeded(List<R> objects) {
        return Observable.defer(() -> justNullable(ParseObject.fetchAllIfNeededInBackground(objects)))
                .flatMap(l -> Observable.from(l));
    }

    public static <R extends ParseObject> Observable<R> delete(R object) {
        return Observable.defer(() -> justNullable(object.deleteInBackground()))
                .map(v -> object);
    }

    public static <R extends ParseObject> Observable<R> delete(List<R> objects) {
        return Observable.defer(() -> justNullable(ParseObject.deleteAllInBackground(objects)))
                .flatMap(v -> Observable.from(objects));
    }

    public static Observable<String> subscribe(String channel) {
        android.util.Log.d("ParseObservable", "subscribe: channel: " + channel);

        return Observable.defer(() -> justNullable(ParsePush.subscribeInBackground(channel)))
                .doOnNext(v -> android.util.Log.d("ParseObservable", "doOnNext: " + v))
                .map(v -> channel);
    }

    public static Observable<String> unsubscribe(String channel) {
        android.util.Log.d("ParseObservable", "unsubscribe, channel: " + channel);

        return Observable.defer(() -> justNullable(ParsePush.unsubscribeInBackground(channel)))
                .map(v -> channel);
    }

    // TODO
    // ParsePush
    // send(JSONObject data, ParseQuery<ParseInstallation> query)
    // send()
    // sendMessage(String message)
    //
    // ParseObject
    // refresh()
    // fetchFromLocalDatastore()
    //
    // ParseUser
    // becomeInBackground()
    // enableRevocableSessionInBackground
    // logInInBackground(String username, String password)
    // logOutInBackground()
    // requestPasswordResetInBackground(String email)
    // signUpInBackground()
}