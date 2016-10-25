package org.genericsystem.reactor.gscomponents;

import org.genericsystem.reactor.htmltag.HtmlButton;
import org.genericsystem.reactor.htmltag.HtmlLabel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.genericsystem.common.Statics;
import org.genericsystem.reactor.Context;
import org.genericsystem.reactor.Tag;

import javafx.beans.binding.Bindings;

public class GSMonitor extends GSDiv {

	public GSMonitor(Tag parent) {
		this(parent, FlexDirection.ROW);
	}

	public GSMonitor(Tag parent, FlexDirection direction) {
		super(parent, direction);
		addStyle("justify-content", "space-around");
		addStyle("padding", "10px");
		new HtmlButton(this) {
			{
				setText("Save");
				bindAction(Context::flush);
			}
		};

		new HtmlLabel(this) {
			{
				bindText(context -> Bindings.createStringBinding(() -> {
					Long tsMs = (Long) context.getTsObservableValue().getValue() / Statics.MILLI_TO_NANOSECONDS;
					Date dateMs = new Date(tsMs);
					Instant instant = Instant.ofEpochMilli(dateMs.getTime());
					LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
					return "Last update : " + ldt.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss"));
				}, context.getTsObservableValue()));
			}
		};
		middlePart();
		new HtmlButton(this) {
			{
				setText("Cancel");
				bindAction(Context::cancel);
			}
		};
		// new HtmlButton(this) {
		// {
		// setText("Collect");
		// bindAction(model -> System.gc());
		// }
		// };
	}

	protected void middlePart() {
	}

}