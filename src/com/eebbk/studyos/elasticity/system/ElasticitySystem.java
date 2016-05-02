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

/**
 * This is a wrapper for BaseSpringSystem that provides the convenience of automatically providing
 * the AndroidSpringLooper dependency in {@link ElasticitySystem#create}.
 */
public class ElasticitySystem extends BaseElasticitySystem {

  /**
   * Create a new SpringSystem providing the appropriate constructor parameters to work properly
   * in an Android environment.
   * @return the SpringSystem
   */
  public static ElasticitySystem create() {
    return new ElasticitySystem(AndroidElasticityLooperFactory.createSpringLooper());
  }

  private ElasticitySystem(ElasticityLooper springLooper) {
    super(springLooper);
  }

}