package org.genericsystem.reactor.modelproperties;

import org.genericsystem.reactor.Context;
import org.genericsystem.reactor.HtmlDomNode;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public interface AttributesDefaults extends MapStringDefaults {

	public static final String ATTRIBUTES = "attributes";

	default ObservableMap<String, String> getDomNodeAttributes(Context model) {
		return getDomNodeMap(model, ATTRIBUTES, HtmlDomNode::getAttributesListener, tag -> FXCollections.observableHashMap());
	}
}