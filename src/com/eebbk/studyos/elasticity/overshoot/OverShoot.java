/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.eebbk.studyos.elasticity.overshoot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import com.eebbk.studyos.elasticity.system.BaseElasticitySystem;
import com.eebbk.studyos.elasticity.system.Elasticity;
import com.eebbk.studyos.elasticity.system.ElasticityListener;

import android.util.Log;

/**
 * Classical spring implementing Hooke's law with configurable friction and
 * tension.
 */
public class OverShoot implements Elasticity {

	// unique incrementer id for springs
	private static int ID = 0;

	// maximum amount of time to simulate per physics iteration in seconds (4
	// frames at 60 FPS)
	private static final double MAX_DELTA_TIME_SEC = 0.064;
	// fixed timestep to use in the physics solver in seconds
	private static final double SOLVER_TIMESTEP_SEC = 0.001;
	private OverShootConfig mOverShootConfig;
	// private boolean mOvershootClampingEnabled;

	// storage for the current and prior physics state while integration is
	// occurring
	// private static class PhysicsState {
	// double position;
	// double velocity;
	// }

	// unique id for the spring in the system
	private final String mId;
	// all physics simulation objects are final and reused in each processing
	// pass
	// private final PhysicsState mCurrentState = new PhysicsState();
	// private final PhysicsState mPreviousState = new PhysicsState();
	// private final PhysicsState mTempState = new PhysicsState();
	// private double mStartValue;
	// private double mEndValue;
	// private boolean mWasAtRest = true;
	// thresholds for determining when the spring is at rest
	// private double mRestSpeedThreshold = 0.005;
	// private double mDisplacementFromRestThreshold = 0.005;
	private double mTimeAccumulator = 0;
	private final CopyOnWriteArraySet<ElasticityListener> mListeners = new CopyOnWriteArraySet<ElasticityListener>();

	private final BaseElasticitySystem mOverShootSystem;

	private double mValue;
	private double mPreviousValue;
//	private List<Double> mValues;

	private double mValueIncreace;

	/**
	 * create a new spring
	 */
	public OverShoot(BaseElasticitySystem system) {
		if (system == null) {
			throw new IllegalArgumentException("Spring cannot be created outside of a BaseSpringSystem");
		}
		mOverShootSystem = system;
		mId = "over:" + ID++;
		mValue = 1000;
		mValueIncreace = 1000; 
//		mValues = new ArrayList<Double>();
//		setOverShootConfig(new OverShootConfig(1, 1, 1, 1));
	}

	/**
	 * Destroys this Spring, meaning that it will be deregistered from its
	 * BaseSpringSystem so it won't be iterated anymore and will clear its set
	 * of listeners. Do not use the Spring after calling this, doing so may just
	 * cause an exception to be thrown.
	 */
	public void destroy() {
		mListeners.clear();
		mOverShootSystem.deregisterElasticity(this);
	}

	/**
	 * get the unique id for this spring
	 * 
	 * @return the unique id
	 */
	public String getId() {
		return mId;
	}

	/**
	 * set the config class
	 * 
	 * @param springConfig
	 *            config class for the spring
	 * @return this Spring instance for chaining
	 */
	public OverShoot setOverShootConfig(OverShootConfig overShootConfig) {
		if (overShootConfig == null) {
			throw new IllegalArgumentException("springConfig is required");
		}
		mOverShootConfig = overShootConfig;
//		mOverShootConfig.velocity = overShootConfig.velocity;
//		mOverShootConfig.amp = overShootConfig.amp;
//		mOverShootConfig.decay = overShootConfig.decay;
//		mOverShootConfig.freq = overShootConfig.freq;
		return this;
	}

	/**
	 * retrieve the spring config for this spring
	 * 
	 * @return the SpringConfig applied to this spring
	 */
	public OverShootConfig getSpringConfig() {
		return mOverShootConfig;
	}

	/**
	 * Set the displaced value to determine the displacement for the spring from
	 * the rest value. This value is retained and used to calculate the
	 * displacement ratio. The default signature also sets the Spring at rest to
	 * facilitate the common behavior of moving a spring to a new position.
	 * 
	 * @param currentValue
	 *            the new start and current value for the spring
	 * @return the spring for chaining
	 */
	// public OverShoot setCurrentValue(double currentValue) {
	// return setCurrentValue(currentValue, true);
	// }

	/**
	 * The full signature for setCurrentValue includes the option of not setting
	 * the spring at rest after updating its currentValue. Passing setAtRest
	 * false means that if the endValue of the spring is not equal to the
	 * currentValue, the physics system will start iterating to resolve the
	 * spring to the end value. This is almost never the behavior that you want,
	 * so the default setCurrentValue signature passes true.
	 * 
	 * @param currentValue
	 *            the new start and current value for the spring
	 * @param setAtRest
	 *            optionally set the spring at rest after updating its current
	 *            value. see {@link com.facebook.rebound.Spring#setAtRest()}
	 * @return the spring for chaining
	 */
	// public OverShoot setCurrentValue(double currentValue, boolean setAtRest)
	// {
	// mStartValue = currentValue;
	// mCurrentState.position = currentValue;
	// mSpringSystem.activateElasticity(this.getId());
	// for (ElasticityListener listener : mListeners) {
	// listener.onSpringUpdate(this);
	// }
	// if (setAtRest) {
	// setAtRest();
	// }
	// return this;
	// }

	public OverShoot setVelocity(double velocity) {
		mOverShootConfig.velocity = velocity;
		mOverShootSystem.activateElasticity(this.getId());
		for (ElasticityListener listener : mListeners) {
			listener.onSpringUpdate(this);
		}
		return this;
	}

	public void reset() {
		mTimeAccumulator = 0;
		mValue = 1000;
	}

	/**
	 * Get the displacement value from the last time setCurrentValue was called.
	 * 
	 * @return displacement value
	 */
	// public double getStartValue() {
	// return mStartValue;
	// }

	/**
	 * Get the current
	 * 
	 * @return current value
	 */
	public double getValue() {
		return mValue;
	}

	/**
	 * get the displacement of the springs current value from its rest value.
	 * 
	 * @return the distance displaced by
	 */
	// public double getCurrentDisplacementDistance() {
	// return getDisplacementDistanceForState(mCurrentState);
	// }

	/**
	 * get the displacement from rest for a given physics state
	 * 
	 * @param state
	 *            the state to measure from
	 * @return the distance displaced by
	 */
	// private double getDisplacementDistanceForState(PhysicsState state) {
	// return Math.abs(mEndValue - state.position);
	// }

	/**
	 * set the rest value to determine the displacement for the spring
	 * 
	 * @param endValue
	 *            the endValue for the spring
	 * @return the spring for chaining
	 */
	// public OverShoot setEndValue(double endValue) {
	// if (mEndValue == endValue && isAtRest()) {
	// return this;
	// }
	// mStartValue = getCurrentValue();
	// mEndValue = endValue;
	// mSpringSystem.activateElasticity(this.getId());
	// for (ElasticityListener listener : mListeners) {
	// listener.onSpringEndStateChange(this);
	// }
	// return this;
	// }

	/**
	 * get the rest value used for determining the displacement of the spring
	 * 
	 * @return the rest value for the spring
	 */
	// public double getEndValue() {
	// return mEndValue;
	// }

	/**
	 * set the velocity on the spring in pixels per second
	 * 
	 * @param velocity
	 *            velocity value
	 * @return the spring for chaining
	 */
	// public OverShoot setVelocity(double velocity) {
	// if (velocity == mCurrentState.velocity) {
	// return this;
	// }
	// mCurrentState.velocity = velocity;
	// mSpringSystem.activateElasticity(this.getId());
	// return this;
	// }

	/**
	 * get the velocity of the spring
	 * 
	 * @return the current velocity
	 */
	// public double getVelocity() {
	// return mCurrentState.velocity;
	// }

	/**
	 * Sets the speed at which the spring should be considered at rest.
	 * 
	 * @param restSpeedThreshold
	 *            speed pixels per second
	 * @return the spring for chaining
	 */
	// public OverShoot setRestSpeedThreshold(double restSpeedThreshold) {
	// mRestSpeedThreshold = restSpeedThreshold;
	// return this;
	// }

	/**
	 * Returns the speed at which the spring should be considered at rest in
	 * pixels per second
	 * 
	 * @return speed in pixels per second
	 */
	// public double getRestSpeedThreshold() {
	// return mRestSpeedThreshold;
	// }

	/**
	 * set the threshold of displacement from rest below which the spring should
	 * be considered at rest
	 * 
	 * @param displacementFromRestThreshold
	 *            displacement to consider resting below
	 * @return the spring for chaining
	 */
	// public OverShoot setRestDisplacementThreshold(double
	// displacementFromRestThreshold) {
	// mDisplacementFromRestThreshold = displacementFromRestThreshold;
	// return this;
	// }

	/**
	 * get the threshold of displacement from rest below which the spring should
	 * be considered at rest
	 * 
	 * @return displacement to consider resting below
	 */
	// public double getRestDisplacementThreshold() {
	// return mDisplacementFromRestThreshold;
	// }

	/**
	 * Force the spring to clamp at its end value to avoid overshooting the
	 * target value.
	 * 
	 * @param overshootClampingEnabled
	 *            whether or not to enable overshoot clamping
	 * @return the spring for chaining
	 */
	// public OverShoot setOvershootClampingEnabled(boolean
	// overshootClampingEnabled) {
	// mOvershootClampingEnabled = overshootClampingEnabled;
	// return this;
	// }

	/**
	 * Check if overshoot clamping is enabled.
	 * 
	 * @return is overshoot clamping enabled
	 */
	// public boolean isOvershootClampingEnabled() {
	// return mOvershootClampingEnabled;
	// }

	/**
	 * Check if the spring is overshooting beyond its target.
	 * 
	 * @return true if the spring is overshooting its target
	 */
	// public boolean isOvershooting() {
	// return mOverShootConfig.tension > 0 &&
	// ((mStartValue < mEndValue && getCurrentValue() > mEndValue) ||
	// (mStartValue > mEndValue && getCurrentValue() < mEndValue));
	// }

	/**
	 * advance the physics simulation in SOLVER_TIMESTEP_SEC sized chunks to
	 * fulfill the required realTimeDelta. The math is inlined inside the loop
	 * since it made a huge performance impact when there are several springs
	 * being advanced.
	 * 
	 * @param realDeltaTime
	 *            clock drift
	 */
	
	public void advance(double realDeltaTime) {
//		Log.v("djh", "advance");
		boolean isAtRest = isAtRest();

		if (isAtRest) {
			mValue = 0;
		}

		double adjustedDeltaTime = realDeltaTime;
		if (realDeltaTime > MAX_DELTA_TIME_SEC) {
			adjustedDeltaTime = MAX_DELTA_TIME_SEC;
		}

		mTimeAccumulator += adjustedDeltaTime;

		double v = mOverShootConfig.velocity;
		double amp = mOverShootConfig.amp;
		double freq = mOverShootConfig.freq;
		double decay = mOverShootConfig.decay;
//		Log.v("djh", " mOverShootConfig.decay " + mOverShootConfig.decay);
		double T = mTimeAccumulator;
		double value = v * amp * Math.sin(freq * 2 * T * Math.PI) / Math.exp(decay * T);
//		Log.v("djh2", Math.exp(decay * T)+"");
//		Log.v("djh", " value " + value);
		mValueIncreace = value;
		mPreviousValue = mValue;
//		mValue = mValue + value;
		mValue = value;
		boolean notifyActivate = false;
		// if (mWasAtRest) {
		// mWasAtRest = false;
		// notifyActivate = true;
		// }
		boolean notifyAtRest = false;
		if (isAtRest) {
			notifyAtRest = true;
		}
		for (ElasticityListener listener : mListeners) {
			if (notifyActivate) {
				listener.onSpringActivate(this);
			}

			listener.onSpringUpdate(this);

			if (notifyAtRest) {
				listener.onSpringAtRest(this);
			}
		}
	}

	/**
	 * Check if this spring should be advanced by the system. * The rule is if
	 * the spring is currently at rest and it was at rest in the previous
	 * advance, the system can skip this spring
	 * 
	 * @return should the system process this spring
	 */
	private int mSystemShouldAdvanceIndex;

	public boolean systemShouldAdvance() {
		// boolean systemShouldAdvance;
//		mValues.add(mValue);
//		if (mValues.size() >= 5) {
//			mValues.remove(0);
//		}
//		if (mValues.size() > 4) {
//			if (mValues.get(0) == mValues.get(1) && mValues.get(1) == mValues.get(2)
//					&& mValues.get(2) == mValues.get(3)) {
//				return true;
//			}
//		}
		if (isAtRest()) {
			return true;
		}
//		return mValueIncreace > 0.00001 || mValueIncreace < -0.00001;
		return mValue > 0.00001 || mValue < -0.00001;
	}

	/**
	 * Check if the spring was at rest in the prior iteration. This is used for
	 * ensuring the ending callbacks are fired as the spring comes to a rest.
	 * 
	 * @return true if the spring was at rest in the prior iteration
	 */
	// public boolean wasAtRest() {
	// return mWasAtRest;
	// }

	/**
	 * check if the current state is at rest
	 * 
	 * @return is the spring at rest
	 */
	public boolean isAtRest() {
		return mValue == 1000;
	}

	/**
	 * Set the spring to be at rest by making its end value equal to its current
	 * value and setting velocity to 0.
	 * 
	 * @return this object
	 */
	// public OverShoot setAtRest() {
	// mEndValue = mCurrentState.position;
	// mTempState.position = mCurrentState.position;
	// mCurrentState.velocity = 0;
	// return this;
	// }

	/**
	 * linear interpolation between the previous and current physics state based
	 * on the amount of timestep remaining after processing the rendering delta
	 * time in timestep sized chunks.
	 * 
	 * @param alpha
	 *            from 0 to 1, where 0 is the previous state, 1 is the current
	 *            state
	 */
	// private void interpolate(double alpha) {
	// mCurrentState.position = mCurrentState.position * alpha +
	// mPreviousState.position *(1-alpha);
	// mCurrentState.velocity = mCurrentState.velocity * alpha +
	// mPreviousState.velocity *(1-alpha);
	// }

	/** listeners **/

	/**
	 * add a listener
	 * 
	 * @param newListener
	 *            to add
	 * @return the spring for chaining
	 */
	public OverShoot addListener(ElasticityListener newListener) {
		if (newListener == null) {
			throw new IllegalArgumentException("newListener is required");
		}
		mListeners.add(newListener);
		return this;
	}

	/**
	 * remove a listener
	 * 
	 * @param listenerToRemove
	 *            to remove
	 * @return the spring for chaining
	 */
	public OverShoot removeListener(ElasticityListener listenerToRemove) {
		if (listenerToRemove == null) {
			throw new IllegalArgumentException("listenerToRemove is required");
		}
		mListeners.remove(listenerToRemove);
		return this;
	}

	/**
	 * remove all of the listeners
	 * 
	 * @return the spring for chaining
	 */
	public OverShoot removeAllListeners() {
		mListeners.clear();
		return this;
	}

	/**
	 * This method checks to see that the current spring displacement value is
	 * equal to the input, accounting for the spring's rest displacement
	 * threshold.
	 * 
	 * @param value
	 *            The value to compare the spring value to
	 * @return Whether the displacement value from the spring is within the
	 *         bounds of the compare value, accounting for threshold
	 */
	// public boolean currentValueIsApproximately(double value) {
	// return Math.abs(getCurrentValue() - value) <=
	// getRestDisplacementThreshold();
	// }

}
