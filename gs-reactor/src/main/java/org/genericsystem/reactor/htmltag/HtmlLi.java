package org.genericsystem.reactor.htmltag;

import org.genericsystem.reactor.Tag;
import org.genericsystem.reactor.gscomponents.GSTagImpl;

/**
 * @author Nicolas Feybesse
 *
 */
public class HtmlLi extends GSTagImpl {

	public HtmlLi() {
	}

	public HtmlLi(Tag parent) {
		super(parent);
	}

	@Override
	public String getTag() {
		return "li";
	}
}