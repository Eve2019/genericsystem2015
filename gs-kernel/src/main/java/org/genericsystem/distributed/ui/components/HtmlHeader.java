package org.genericsystem.distributed.ui.components;

import org.genericsystem.distributed.ui.HtmlElement;
import org.genericsystem.distributed.ui.HtmlElement.HtmlDomNode;
import org.genericsystem.distributed.ui.Model;

/**
 * @author Nicolas Feybesse
 *
 */
public class HtmlHeader<M extends Model> extends HtmlElement<M, HtmlHeader<M>, HtmlDomNode> {

	public HtmlHeader(HtmlElement<?, ?, ?> parent) {
		super(parent, HtmlDomNode.class);
	}

	@Override
	protected HtmlDomNode createNode(Object parent) {
		return new HtmlDomNode("header");
	}
}
