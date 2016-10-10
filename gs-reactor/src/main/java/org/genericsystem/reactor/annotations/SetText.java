package org.genericsystem.reactor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.genericsystem.reactor.annotations.SetText.SetTexts;
import org.genericsystem.reactor.gscomponents.GSTagImpl;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Repeatable(SetTexts.class)
public @interface SetText {
	Class<? extends GSTagImpl>[] path() default {};

	String value();

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	public @interface SetTexts {
		SetText[] value();
	}
}
