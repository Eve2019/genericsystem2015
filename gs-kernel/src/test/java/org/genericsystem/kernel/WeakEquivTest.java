package org.genericsystem.kernel;

import java.util.Arrays;
import org.genericsystem.api.core.ApiStatics;
import org.genericsystem.common.Generic;
import org.testng.annotations.Test;

@Test
public class WeakEquivTest extends AbstractTest {

	public void test001() {
		Engine root = new Engine();
		Generic car = root.addInstance("Car");
		Generic color = root.addInstance("Color");
		Generic carColor = root.addInstance("CarColor", car, color);
		carColor.enableSingularConstraint(ApiStatics.BASE_POSITION);
		Generic myBmw = car.addInstance("myBmw");
		Generic green = color.addInstance("green");
		Generic myBmwGreen = carColor.addInstance("myBmwGreen", myBmw, green);
		Generic yellow = color.addInstance("yellow");
		assert myBmwGreen.equiv(carColor, "myBmwYellow", Arrays.asList(myBmw, yellow));
	}

	public void test002() {
		Engine root = new Engine();
		Generic car = root.addInstance("Car");
		Generic color = root.addInstance("Color");
		Generic carColor = root.addInstance("CarColor", car, color);
		carColor.enableSingularConstraint(ApiStatics.BASE_POSITION);
		carColor.enableReferentialIntegrity(ApiStatics.BASE_POSITION);
		Generic myBmw = car.addInstance("myBmw");
		Generic green = color.addInstance("green");
		Generic yellow = color.addInstance("yellow");
		Generic myBmwGreen = carColor.addInstance("myBmwGreen", myBmw, green);
		assert !myBmwGreen.equiv(carColor, "myBmwYellow", Arrays.asList(myBmw, yellow));
	}

	public void test003() {
		Engine root = new Engine();
		Generic car = root.addInstance("Car");
		Generic color = root.addInstance("Color");
		Generic carColor = root.addInstance("CarColor", car, color);
		carColor.enableSingularConstraint(ApiStatics.TARGET_POSITION);
		carColor.enableReferentialIntegrity(ApiStatics.TARGET_POSITION);
		Generic myBmw = car.addInstance("myBmw");
		Generic green = color.addInstance("green");
		Generic yellow = color.addInstance("yellow");
		Generic myBmwGreen = carColor.addInstance("myBmwGreen", myBmw, green);
		assert !myBmwGreen.equiv(carColor, "myBmwYellow", Arrays.asList(myBmw, yellow));
	}

	public void test004() {
		Engine root = new Engine();
		Generic car = root.addInstance("Car");
		Generic color = root.addInstance("Color");
		Generic carColor = car.addAttribute("CarColor", color);
		carColor.enableSingularConstraint(ApiStatics.TARGET_POSITION);
		assert root.getMetaAttribute().isReferentialIntegrityEnabled(ApiStatics.TARGET_POSITION);
		Generic myBmw = car.addInstance("myBmw");
		car.addInstance("myAudi");
		Generic green = color.addInstance("green");
		color.addInstance("yellow");
		Generic myBmwGreen = carColor.addInstance("myBmwGreen", myBmw, green);
		carColor.addInstance(myBmwGreen, "myAudiGreen", myBmw, green);
	}

	public void test005() {
		Engine root = new Engine();
		Generic car = root.addInstance("Car");
		Generic color = root.addInstance("Color");
		Generic carColor = root.addInstance("CarColor", car, color);
		carColor.enableSingularConstraint(ApiStatics.TARGET_POSITION);
		carColor.disableReferentialIntegrity(ApiStatics.TARGET_POSITION);
		Generic myBmw = car.addInstance("myBmw");
		Generic green = color.addInstance("green");
		Generic myBmwGreen = carColor.addInstance("myBmwGreen", myBmw, green);
		carColor.setInstance("myBmwGreen2", myBmw, green);
		assert !myBmwGreen.isAlive();
	}

	public void test006() {
		Engine root = new Engine();
		Generic car = root.addInstance("Car");
		Generic color = car.addAttribute("Color");
		Generic defaultColor = color.addInstance("Red", car);
		Generic carColor = root.addInstance("CarColor", car, color);
		carColor.enableSingularConstraint(ApiStatics.BASE_POSITION);
		carColor.enableReferentialIntegrity(ApiStatics.BASE_POSITION);
		Generic myBmw = car.addInstance("myBmw");
		Generic green = color.addInstance("green", myBmw);
		Generic myBmwGreen = carColor.addInstance("myBmwGreen", myBmw, green);
		assert !myBmwGreen.equiv(carColor, "myBmwYellow", Arrays.asList(myBmw, defaultColor));
	}
}
