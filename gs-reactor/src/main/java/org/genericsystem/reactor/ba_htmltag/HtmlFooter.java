package org.genericsystem.reactor.ba_htmltag;

import org.genericsystem.reactor.Tag;
import org.genericsystem.reactor.ca_gscomponents.GSTagImpl;

/**
 * @author Nicolas Feybesse
 *
 */
public class HtmlFooter extends GSTagImpl {

	public HtmlFooter() {
	}

	public HtmlFooter(Tag parent) {
		super(parent);
	}

	@Override
	public String getTag() {
		return "footer";
	}
}