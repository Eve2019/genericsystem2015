package org.genericsystem.reactor.context;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.genericsystem.common.Generic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@FunctionalInterface
public interface ObservableListExtractor extends Function<Generic[], ObservableList<Generic>> {

	public static ObservableListExtractor from(Class<?>... genericClasses) {
		return gs -> FXCollections.observableArrayList(Arrays.stream(genericClasses).map(gs[0].getRoot()::<Generic> find).collect(Collectors.toList()));
	}

	public static final ObservableListExtractor INSTANCES = generics -> {
		// System.out.println("INSTANCES : " + Arrays.toString(generics) + " " + generics[0].getObservableInstances());
		return generics[0].getObservableInstances();
	};

	public static final ObservableListExtractor SUBINSTANCES = generics -> {
		// System.out.println("INSTANCES : " + Arrays.toString(generics) + " " + generics[0].getObservableSubInstances());
		return generics[0].getObservableSubInstances();
	};

	public static final ObservableListExtractor SUBINSTANCES_OF_META = generics -> {
		return generics[0].getMeta().getObservableSubInstances();
	};

	public static final ObservableListExtractor ATTRIBUTES_OF_TYPE = generics -> {
		// System.out.println("ATTRIBUTES_OF_TYPE : " + Arrays.toString(generics) + " " + generics[0].getObservableAttributes().filtered(attribute ->
		// attribute.isCompositeForInstances(generics[0])));
		return generics[0].getObservableAttributes().filtered(attribute -> attribute.isCompositeForInstances(generics[0]));
	};

	public static final ObservableListExtractor ATTRIBUTES_OF_INSTANCES = generics -> {
		// System.out.println("ATTRIBUTES_OF_INSTANCES : " + Arrays.toString(generics) + " " + generics[1].getObservableAttributes().filtered(attribute ->
		// attribute.isCompositeForInstances(generics[1])));
		return generics[1].getObservableAttributes().filtered(attribute -> attribute.isCompositeForInstances(generics[1]));
	};

	public static final ObservableListExtractor COMPONENTS = generics -> {
		// System.out.println("COMPONENTS : " + Arrays.toString(generics) + " " + generics[0].getComponents());
		return FXCollections.observableList(generics[0].getComponents());
	};

	public static final ObservableListExtractor HOLDERS = generics -> {
		// System.out.println("HOLDERS : " + Arrays.toString(generics) + " " + generics[1].getObservableHolders(generics[0]));

		ObservableList<Generic> holders = generics[1].getObservableHolders(generics[0]);
		// holders.addListener((ListChangeListener) c -> System.out.println(c));
		return holders;
	};

	public static final ObservableListExtractor OTHER_COMPONENTS_1 = gs -> ObservableListExtractor.COMPONENTS.apply(gs).filtered(g -> !gs[1].inheritsFrom(g));

	public static final ObservableListExtractor OTHER_COMPONENTS_2 = gs -> ObservableListExtractor.COMPONENTS.apply(gs).filtered(g -> !gs[2].inheritsFrom(g));

	public static class ATTRIBUTES_OF_TYPE implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			return ATTRIBUTES_OF_TYPE.apply(generics);
		}
	}

	public static class COMPONENTS implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			return COMPONENTS.apply(generics);
		}
	}

	public static class OTHER_COMPONENTS_1 implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			return OTHER_COMPONENTS_1.apply(generics);
		}
	}

	public static class OTHER_COMPONENTS_2 implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			return OTHER_COMPONENTS_2.apply(generics);
		}
	}

	public static class INSTANCES implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			return INSTANCES.apply(generics);
		}
	}

	public static class SUBINSTANCES implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			return SUBINSTANCES.apply(generics);
		}
	}

	public static class SUBINSTANCES_OF_META implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			return SUBINSTANCES_OF_META.apply(generics);
		}
	}

	public static class SUBINSTANCES_OF_LINK_COMPONENT implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			return ObservableListExtractor.SUBINSTANCES.apply(ObservableListExtractor.COMPONENTS.apply(generics).filtered(g -> !generics[2].inheritsFrom(g)).stream().toArray(Generic[]::new));
		}
	}

	public static class SUBINSTANCES_OF_RELATION_COMPONENT implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			return ObservableListExtractor.SUBINSTANCES.apply(ObservableListExtractor.COMPONENTS.apply(generics).filtered(g -> !generics[1].inheritsFrom(g)).stream().toArray(Generic[]::new));
		}
	}

	public static class ATTRIBUTES_OF_INSTANCES implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			return ATTRIBUTES_OF_INSTANCES.apply(generics);
		}
	}

	public static class HOLDERS implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			return HOLDERS.apply(generics);
		}
	}

	public static class NO_FOR_EACH implements ObservableListExtractor {
		@Override
		public ObservableList<Generic> apply(Generic[] generics) {
			throw new IllegalStateException();
		}
	}
}