package com.eebbk.studyos.elasticity.system;

import com.eebbk.studyos.elasticity.overshoot.OverShoot;

public interface Elasticity {

	// public Elasticity(BaseElasticitySystem baseElasticitySystem) {
	// // TODO Auto-generated constructor stub
	// }

	public double getValue();

	public String getId();

	public boolean systemShouldAdvance();

	public void advance(double d);

	public Elasticity addListener(ElasticityListener newListener);

	public Elasticity removeListener(ElasticityListener listenerToRemove);
}
