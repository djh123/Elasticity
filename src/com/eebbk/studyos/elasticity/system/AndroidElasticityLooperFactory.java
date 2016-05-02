/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.eebbk.studyos.elasticity.system;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Choreographer;

/**
 * Android version of the spring looper that uses the most appropriate frame callback mechanism
 * available. It uses Android's {@link Choreographer} when available, otherwise it uses a
 * {@link Handler}.
 */
abstract class AndroidElasticityLooperFactory {

  /**
   * Create an Android {@link com.facebook.rebound.SpringLooper} for the detected Android platform.
   * @return a SpringLooper
   */
  public static ElasticityLooper createSpringLooper() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      return ChoreographerAndroidElasticityLooper.create();
    } else {
      return LegacyAndroidElasticityLooper.create();
    }
  }

  /**
   * The base implementation of the Android spring looper, using a {@link Handler} for the
   * frame callbacks.
   */
  private static class LegacyAndroidElasticityLooper extends ElasticityLooper {

    private final Handler mHandler;
    private final Runnable mLooperRunnable;
    private boolean mStarted;
    private long mLastTime;

    /**
     * @return an Android spring looper using a new {@link Handler} instance
     */
    public static ElasticityLooper create() {
      return new LegacyAndroidElasticityLooper(new Handler());
    }

    public LegacyAndroidElasticityLooper(Handler handler) {
      mHandler = handler;
      mLooperRunnable = new Runnable() {
        @Override
        public void run() {
          if (!mStarted || mElasticitySystem == null) {
            return;
          }
          long currentTime = SystemClock.uptimeMillis();
          mElasticitySystem.loop(currentTime - mLastTime);
          mLastTime = currentTime;
          mHandler.post(mLooperRunnable);
        }
      };
    }

    @Override
    public void start() {
      if (mStarted) {
        return;
      }
      mStarted = true;
      mLastTime = SystemClock.uptimeMillis();
      mHandler.removeCallbacks(mLooperRunnable);
      mHandler.post(mLooperRunnable);
    }

    @Override
    public void stop() {
      mStarted = false;
      mHandler.removeCallbacks(mLooperRunnable);
    }
  }

  /**
   * The Jelly Bean and up implementation of the spring looper that uses Android's
   * {@link Choreographer} instead of a {@link Handler}
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private static class ChoreographerAndroidElasticityLooper extends ElasticityLooper {

    private final Choreographer mChoreographer;
    private final Choreographer.FrameCallback mFrameCallback;
    private boolean mStarted;
    private long mLastTime;

    /**
     * @return an Android spring choreographer using the system {@link Choreographer}
     */
    public static ChoreographerAndroidElasticityLooper create() {
      return new ChoreographerAndroidElasticityLooper(Choreographer.getInstance());
    }

    public ChoreographerAndroidElasticityLooper(Choreographer choreographer) {
      mChoreographer = choreographer;
      mFrameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
          if (!mStarted || mElasticitySystem == null) {
            return;
          }
          long currentTime = SystemClock.uptimeMillis();
          mElasticitySystem.loop(currentTime - mLastTime);
          mLastTime = currentTime;
          mChoreographer.postFrameCallback(mFrameCallback);
        }
      };
    }

    @Override
    public void start() {
      if (mStarted) {
        return;
      }
      mStarted = true;
      mLastTime = SystemClock.uptimeMillis();
      mChoreographer.removeFrameCallback(mFrameCallback);
      mChoreographer.postFrameCallback(mFrameCallback);
    }

    @Override
    public void stop() {
      mStarted = false;
      mChoreographer.removeFrameCallback(mFrameCallback);
    }
  }
}