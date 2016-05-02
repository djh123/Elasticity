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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import android.util.Log;

/**
 * BaseSpringSystem maintains the set of springs within an Application context. It is responsible for
 * Running the spring integration loop and maintains a registry of all the Springs it solves for.
 * In addition to listening to physics events on the individual Springs in the system, listeners
 * can be added to the BaseSpringSystem itself to provide pre and post integration setup.
 */
public class BaseElasticitySystem {

  private final Map<String, Elasticity> mElasticityRegistry = new HashMap<String, Elasticity>();
  private final Set<Elasticity> mActiveElasticity = new CopyOnWriteArraySet<Elasticity>();
  private final ElasticityLooper mElasticityLooper;
  private final CopyOnWriteArraySet<ElasticitySystemListener> mListeners = new CopyOnWriteArraySet<ElasticitySystemListener>();
  private boolean mIdle = true;

  /**
   * create a new BaseSpringSystem
   * @param springLooper parameterized springLooper to allow testability of the
   *        physics loop
   */
  public BaseElasticitySystem(ElasticityLooper looper) {
    if (looper == null) {
      throw new IllegalArgumentException("springLooper is required");
    }
    mElasticityLooper = looper;
    mElasticityLooper.setSpringSystem(this);
  }

  /**
   * check if the system is idle
   * @return is the system idle
   */
  public boolean getIsIdle() {
    return mIdle;
  }

  /**
   * create a spring with a random uuid for its name.
   * @return the spring
   */
  public Elasticity createElasticity(Elasticity elasticity) {
//    Elasticity spring = new Elasticity(this);
		registerElasticity(elasticity);
		return elasticity;
  }

  /**
   * get a spring by name
   * @param id id of the spring to retrieve
   * @return Spring with the specified key
   */
  public Elasticity getElasticityById(String id) {
    if (id == null) {
      throw new IllegalArgumentException("id is required");
    }
    return mElasticityRegistry.get(id);
  }

  /**
   * return all the springs in the simulator
   * @return all the springs
   */
  public List<Elasticity> getAllElasticity() {
    Collection<Elasticity> collection = mElasticityRegistry.values();
    List<Elasticity> list;
    if (collection instanceof List) {
      list = (List<Elasticity>)collection;
    } else {
      list = new ArrayList<Elasticity>(collection);
    }
    return Collections.unmodifiableList(list);
  }

  /**
   * Registers a Spring to this BaseSpringSystem so it can be iterated if active.
   * @param elasticity the Spring to register
   */
  void registerElasticity(Elasticity elasticity) {
    if (elasticity == null) {
      throw new IllegalArgumentException("spring is required");
    }
    if (mElasticityRegistry.containsKey(elasticity.getId())) {
      throw new IllegalArgumentException("spring is already registered"); }
    mElasticityRegistry.put(elasticity.getId(), elasticity);
  }

  /**
   * Deregisters a Spring from this BaseSpringSystem, so it won't be iterated anymore. The Spring should
   * not be used anymore after doing this.
   *
   * @param elasticity the Spring to deregister
   */
  public void deregisterElasticity(Elasticity elasticity) {
    if (elasticity == null) {
      throw new IllegalArgumentException("spring is required");
    }
    mActiveElasticity.remove(elasticity);
    mElasticityRegistry.remove(elasticity.getId());
  }

  /**
   * update the springs in the system
   * @param deltaTime delta since last update in millis
   */
  void advance(double deltaTime) {
	  Log.v("djh","elasticity.deltaTime()");
    for (Elasticity elasticity : mActiveElasticity) {
  	  Log.v("djh","elasticity.mActiveElasticity()");
      if (elasticity.systemShouldAdvance()) {
    		Log.v("djh","elasticity.systemShouldAdvance()");
        elasticity.advance(deltaTime / 1000.0);
      } else {
        mActiveElasticity.remove(elasticity);
      }
    }
  }

  /**
   * loop the system until idle
   * @param elapsedMillis elapsed milliseconds
   */
  public void loop(double elapsedMillis) {
    for (ElasticitySystemListener listener : mListeners) {
      listener.onBeforeIntegrate(this);
    }
    advance(elapsedMillis);
    if (mActiveElasticity.isEmpty()) {
      mIdle = true;
    }
    for (ElasticitySystemListener listener : mListeners) {
      listener.onAfterIntegrate(this);
    }
    if (mIdle) {
      mElasticityLooper.stop();
    }
  }

  /**
   * This is used internally by the {@link Elasticity}s created by this {@link AnimattionSystem} to notify
   * it has reached a state where it needs to be iterated. This will add the spring to the list of
   * active springs on this system and start the iteration if the system was idle before this call.
   * @param springId the id of the Spring to be activated
   */
  public void activateElasticity(String id) {
    Elasticity spring = mElasticityRegistry.get(id);
    if (spring == null) {
      throw new IllegalArgumentException("id " + id + " does not reference a registered spring");
    }
    mActiveElasticity.add(spring);
    if (getIsIdle()) {
      mIdle = false;
      mElasticityLooper.start();
    }
  }

  /** listeners **/

  /**
   * Add new listener object.
   * @param newListener listener
   */
  public void addListener(ElasticitySystemListener newListener) {
    if (newListener == null) {
      throw new IllegalArgumentException("newListener is required");
    }
    mListeners.add(newListener);
  }

  /**
   * Remove listener object.
   * @param listenerToRemove listener
   */
  public void removeListener(ElasticitySystemListener listenerToRemove) {
    if (listenerToRemove == null) {
      throw new IllegalArgumentException("listenerToRemove is required");
    }
    mListeners.remove(listenerToRemove);
  }
  /**
   * Remove all listeners.
   */
  public void removeAllListeners() {
    mListeners.clear();
  }
}


