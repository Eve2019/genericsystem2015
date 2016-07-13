package org.genericsystem.reactor.composite;

import org.genericsystem.reactor.model.GenericModel;
import org.genericsystem.reactor.model.GenericModel.StringExtractor;
import org.genericsystem.reactor.model.ObservableListExtractor;

public interface CompositeTag<M extends GenericModel> {

	default StringExtractor getStringExtractor() {
		return StringExtractor.SIMPLE_CLASS_EXTRACTOR;
	}

	default ObservableListExtractor getObservableListExtractor() {
		return ObservableListExtractor.SUBINSTANCES;
	}

	public void forEach(StringExtractor stringExtractor, ObservableListExtractor observableListExtractor);
}