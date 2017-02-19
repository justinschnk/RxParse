/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse;

import bolts.Task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ParseTestUtils {

  /**
   * In our unit test, lots of controllers need to use currentParseUser. A normal
   * currentUserController will try to read user off disk which will throw exception since we do
   * not have android environment. This function register a mock currentUserController and simply
   * return a mock ParseUser for currentParseUser and null for currentSessionToken. It will make
   * ParseUser.getCurrentUserAsync() and ParseUser.getCurrentSessionTokenAsync() work.
   */
  public static void setTestParseUser() {
    ParseCurrentUserController currentUserController = mock(ParseCurrentUserController.class);
    when(currentUserController.getAsync()).thenReturn(Task.forResult(mock(ParseUser.class)));
    when(currentUserController.getCurrentSessionTokenAsync())
        .thenReturn(Task.<String>forResult(null));
    ParseCorePlugins.getInstance().registerCurrentUserController(currentUserController);
  }

}
