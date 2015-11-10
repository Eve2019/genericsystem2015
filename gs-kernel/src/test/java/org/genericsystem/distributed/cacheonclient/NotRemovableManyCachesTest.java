package org.genericsystem.distributed.cacheonclient;

import org.genericsystem.api.core.exceptions.AliveConstraintViolationException;
import org.genericsystem.api.core.exceptions.OptimisticLockConstraintViolationException;
import org.genericsystem.api.core.exceptions.ReferentialIntegrityConstraintViolationException;
import org.genericsystem.common.Generic;
import org.genericsystem.common.HeavyCache;
import org.testng.annotations.Test;

@Test
public class NotRemovableManyCachesTest extends AbstractTest {

	public void test001_aliveEx() {
		CocClientEngine engine = new CocClientEngine();
		HeavyCache cache = engine.getCurrentCache();
		HeavyCache cache2 = engine.newCache().start();
		Generic car = engine.addInstance("Car");
		Generic color = car.addAttribute("Color");
		Generic myBmw = car.addInstance("myBmw");
		Generic myBmwRed = myBmw.addHolder(color, "red");
		cache.start();
		catchAndCheckCause(() -> myBmwRed.remove(), AliveConstraintViolationException.class);

	}

	public void test003_aliveEx() {
		CocClientEngine engine = new CocClientEngine();
		HeavyCache cache = engine.getCurrentCache();
		HeavyCache cache2 = engine.newCache().start();
		Generic car = engine.addInstance("Car");
		Generic color = car.addAttribute("Color");
		Generic myBmw = car.addInstance("myBmw");
		Generic myBmwRed = myBmw.addHolder(color, "red");
		cache.start();
		Generic car2 = engine.addInstance("Car2");
		Generic myBmw2 = car2.addInstance("myBmw2");
		catchAndCheckCause(() -> myBmw2.addHolder(color, "red2"), AliveConstraintViolationException.class);
	}

	public void test001_referenceEx() {
		CocClientEngine engine = new CocClientEngine();
		HeavyCache cache = engine.getCurrentCache();
		Generic car = engine.addInstance("Car");
		cache.flush();
		HeavyCache cache2 = engine.newCache().start();
		Generic color = car.addAttribute("Color");
		Generic myBmw = car.addInstance("myBmw");
		catchAndCheckCause(() -> car.remove(), ReferentialIntegrityConstraintViolationException.class);
	}

	public void test002_referenceEx() {
		CocClientEngine engine = new CocClientEngine();
		HeavyCache cache = engine.getCurrentCache();
		HeavyCache cache2 = engine.newCache().start();
		HeavyCache cache3 = engine.newCache().start();
		Generic car = engine.addInstance("Car");
		Generic color = car.addAttribute("Color");
		Generic myBmw = car.addInstance("myBmw");
		cache3.flush();
		cache2.start();
		cache2.shiftTs();
		Generic myBmwRed = myBmw.addHolder(color, "red");
		cache2.flush();
		cache.start();
		cache.shiftTs();
		catchAndCheckCause(() -> car.remove(), ReferentialIntegrityConstraintViolationException.class);
	}

	public void test001_() {
		CocClientEngine engine = new CocClientEngine();
		Generic car = engine.addInstance("Car");
		Generic myCar1 = car.addInstance("myCar1");
		HeavyCache cache1 = engine.getCurrentCache();
		cache1.flush();
		myCar1.remove();
		cache1.flush();
		HeavyCache cache2 = engine.newCache().start();
		catchAndCheckCause(() -> myCar1.remove(), AliveConstraintViolationException.class);
		cache2.flush();
	}

	public void test002_() {
		CocClientEngine engine = new CocClientEngine();
		Generic car = engine.addInstance("Car");
		Generic myCar = car.addInstance("myCar");
		HeavyCache cache = engine.getCurrentCache();
		cache.flush();
		HeavyCache cache2 = engine.newCache().start();
		myCar.remove();
		cache.start();
		myCar.remove();
		cache.flush();
		cache2.start();
		catchAndCheckCause(() -> cache2.flush(), OptimisticLockConstraintViolationException.class);
		// try {
		// cache2.tryFlush();
		// } catch (ConcurrencyControlException e) {
		//
		// e.printStackTrace();
		// }
	}
}
