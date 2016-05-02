package com.eebbk.studyos.elasticity.overshoot;

public class OverShootConfig {
//	  public double friction;
//	  public double tension;
	  public double velocity ;
	  public double amp;
	  public double freq ;
	  public double decay ;

//	  public static OverShootConfig defaultConfig = OverShootConfig.fromOrigamiTensionAndFriction(40, 7);

	  /**
	   * constructor for the SpringConfig
	   * @param tension tension value for the SpringConfig
	   * @param friction friction value for the SpringConfig
	   */
	  public OverShootConfig(double velocity, double amp,double freq,double decay) {
		  this.velocity = velocity;
		  this.amp = amp;
		  this.freq =freq;
		  this.decay=decay;
	  }

//	  /**
//	   * A helper to make creating a SpringConfig easier with values mapping to the Origami values.
//	   * @param qcTension tension as defined in the Quartz Composition
//	   * @param qcFriction friction as defined in the Quartz Composition
//	   * @return a SpringConfig that maps to these values
//	   */
//	  public static OverShootConfig fromOrigamiTensionAndFriction(double qcTension, double qcFriction) {
//	    return new OverShootConfig(
//	        OrigamiValueConverter.tensionFromOrigamiValue(qcTension),
//	        OrigamiValueConverter.frictionFromOrigamiValue(qcFriction)
//	    );
//	  }
//
//	  /**
//	   * Map values from the Origami POP Animation patch, which are based on a bounciness and speed
//	   * value.
//	   * @param bounciness bounciness of the POP Animation
//	   * @param speed speed of the POP Animation
//	   * @return a SpringConfig mapping to the specified POP Animation values.
//	   */
//	  public static OverShootConfig fromBouncinessAndSpeed(double bounciness, double speed) {
//	    BouncyConversion bouncyConversion = new BouncyConversion(speed, bounciness);
//	    return fromOrigamiTensionAndFriction(
//	        bouncyConversion.getBouncyTension(),
//	        bouncyConversion.getBouncyFriction());
//	  }
}
