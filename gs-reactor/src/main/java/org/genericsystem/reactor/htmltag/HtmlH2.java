package org.genericsystem.reactor.htmltag;

import org.genericsystem.reactor.Tag;
import org.genericsystem.reactor.gscomponents.GSTagImpl;

/**
 * @author Nicolas Feybesse
 *
 */
public class HtmlH2 extends GSTagImpl {

	public HtmlH2() {

	}

	public HtmlH2(Tag parent) {
		super(parent);
	}

	public HtmlH2(Tag parent, String text) {
		super(parent);
		setText(text);
	}

	@Override
	public String getTag() {
		return "h2";
	}
}