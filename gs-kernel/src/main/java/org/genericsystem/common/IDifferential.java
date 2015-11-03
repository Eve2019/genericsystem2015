package org.genericsystem.common;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.genericsystem.api.core.Snapshot;
import org.genericsystem.api.core.exceptions.ConcurrencyControlException;
import org.genericsystem.api.core.exceptions.OptimisticLockConstraintViolationException;
import org.genericsystem.defaults.DefaultVertex;

/**
 * @author Nicolas Feybesse
 *
 */
public interface IDifferential<T extends DefaultVertex<T>> {
	long getTs();

	Snapshot<T> getDependencies(T generic);

	void apply(Snapshot<T> removes, Snapshot<T> adds) throws ConcurrencyControlException, OptimisticLockConstraintViolationException;

	default ObservableList<Generic> getObservableDependencies(Generic generic) {
		return FXCollections.emptyObservableList();
	};

	default void applyObservable(ObservableList<Generic> removesOb, ObservableList<Generic> addsOb) {
	};

}
