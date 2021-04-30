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

package me.modernpage.util;

import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.modernpage.data.local.dao.RateLimiterDao;
import me.modernpage.data.local.entity.Limiter;

/**
 * Utility class that decides whether we should fetch some data or not.
 */
@Singleton
public class RateLimiter {
    private final long timeout = TimeUnit.DAYS.toMillis(15);
    private final RateLimiterDao mRateLimiterDao;

    @Inject
    public RateLimiter(RateLimiterDao rateLimiterDao) {
        mRateLimiterDao = rateLimiterDao;
    }

    public synchronized boolean shouldFetch(String key) {
        Limiter lastFetched = mRateLimiterDao.findRateLimiterByKey(key);
        if (lastFetched == null) {
            return true;
        }
        long now = now();
        return now - lastFetched.getFetchTime() > timeout;
    }

    public synchronized void set(String key) {
        mRateLimiterDao.insert(new Limiter(key, now()));
    }

    private long now() {
        return SystemClock.uptimeMillis();
    }

    public synchronized void reset(String key) {
        mRateLimiterDao.delete(key);
    }
}
