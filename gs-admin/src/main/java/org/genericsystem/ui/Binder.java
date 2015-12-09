package org.genericsystem.ui;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;

public interface Binder<N, SUBMODEL, WRAPPER> {

	default void init(Function<? super SUBMODEL, WRAPPER> applyOnModel, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement) {
		init(applyOnModel.apply(modelContext.getModel()), modelContext, viewContext, childElement);
	}

	void init(WRAPPER wrapper, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement);

	public static <N, SUBMODEL, T extends Event> Binder<N, SUBMODEL, T> actionBinder(Function<N, ObjectProperty<EventHandler<T>>> applyOnNode) {
		return new Binder<N, SUBMODEL, T>() {
			@Override
			public void init(Function<? super SUBMODEL, T> applyOnModel, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement) {
				applyOnNode.apply(viewContext.getNode()).set(event -> applyOnModel.apply(modelContext.getModel()));
			}

			@Override
			public void init(T wrapper, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement) {
			}
		};

	}

	public static <N, SUBMODEL, T> Binder<N, SUBMODEL, T> genericActionBinder(Function<N, ObjectProperty<Consumer<Event>>> applyOnNode) {
		return new Binder<N, SUBMODEL, T>() {
			@Override
			public void init(Function<? super SUBMODEL, T> applyOnModel, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement) {
				applyOnNode.apply(viewContext.getNode()).set(event -> applyOnModel.apply(modelContext.getModel()));

			}

			@Override
			public void init(T wrapper, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement) {
			}
		};

	}

	public static <N, SUBMODEL, W> Binder<N, SUBMODEL, Property<W>> propertyReverseBinder(Function<N, Property<W>> applyOnNode) {
		return new Binder<N, SUBMODEL, Property<W>>() {
			@Override
			public void init(Property<W> wrapper, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement) {
				wrapper.bind(applyOnNode.apply(viewContext.getNode()));
			}
		};
	}

	public static <N, SUBMODEL, W> Binder<N, SUBMODEL, ObservableValue<W>> propertyBinder(Function<N, Property<W>> applyOnNode) {
		return new Binder<N, SUBMODEL, ObservableValue<W>>() {
			@Override
			public void init(ObservableValue<W> wrapper, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement) {
				applyOnNode.apply(viewContext.getNode()).bind(wrapper);
			}
		};
	}

	public static <N, SUBMODEL, W> Binder<N, SUBMODEL, Property<W>> propertyBiDirectionalBinder(Function<N, Property<W>> applyOnNode) {
		return new Binder<N, SUBMODEL, Property<W>>() {
			@Override
			public void init(Property<W> wrapper, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement) {
				applyOnNode.apply(viewContext.getNode()).bindBidirectional(wrapper);
			}
		};
	}

	public static <N, SUBMODEL, W> Binder<N, SUBMODEL, Property<Boolean>> observableListBinder(Function<N, ObservableList<W>> applyOnNode, W styleClass) {
		return new Binder<N, SUBMODEL, Property<Boolean>>() {

			@Override
			public void init(Property<Boolean> wrapper, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement) {
				ObservableList<W> styleClasses = applyOnNode.apply(viewContext.getNode());
				Consumer<Boolean> consumer = bool -> {
					if (bool)
						styleClasses.add(styleClass);
					else
						styleClasses.remove(styleClass);
				};
				consumer.accept(wrapper.getValue());
				wrapper.addListener((o, ov, nv) -> consumer.accept(nv));
			}
		};
	}

	public static <N, SUBMODEL, W> Binder<N, SUBMODEL, ObservableList<W>> foreachBinder() {
		return new Binder<N, SUBMODEL, ObservableList<W>>() {

			@Override
			public void init(ObservableList<W> wrapper, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement) {

				List<ModelContext> children = modelContext.getChildren();

				class ForEachList extends AbstractList<W> implements ListChangeListener<W> {
					{
						addAll(wrapper);
					}

					@SuppressWarnings("unchecked")
					@Override
					public W get(int index) {
						return (W) children.get(index).getModel();
					}

					@Override
					public int size() {
						return children.size();
					}

					@SuppressWarnings("unchecked")
					@Override
					public void add(int index, W model) {
						ModelContext childContext = new ModelContext(modelContext, model);
						new ViewContext(viewContext, childContext, childElement, childElement.nodeClass.isAssignableFrom(model.getClass()) ? model : childElement.createNode(viewContext.getNode()));
						children.add(index, childContext);
					}

					@Override
					public W set(int index, W element) {
						W remove = remove(index);
						add(index, element);
						return remove;
					}

					@Override
					public W remove(int index) {
						ModelContext removed = children.remove(index);
						for (ViewContext<?> internalViewContext : removed.getViewContexts())
							internalViewContext.destroyChild();
						return removed.getModel();
					}

					@Override
					public void onChanged(Change<? extends W> change) {
						while (change.next()) {
							if (change.wasPermutated()) {
								subList(change.getFrom(), change.getTo()).clear();
								addAll(change.getFrom(), change.getList().subList(change.getFrom(), change.getTo()));
							} else {
								if (change.wasRemoved())
									subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();

								if (change.wasAdded())
									addAll(change.getFrom(), change.getAddedSubList());
							}
						}
					}
				}
				wrapper.addListener(new ForEachList());
			}
		};
	}

	public static <N, SUBMODEL, W> Binder<N, SUBMODEL, ObservableValue<W>> selectorBinder() {
		return new Binder<N, SUBMODEL, ObservableValue<W>>() {

			@Override
			public void init(ObservableValue<W> wrapper, ModelContext modelContext, ViewContext<N> viewContext, Element<SUBMODEL> childElement) {
				List<ModelContext> children = modelContext.getChildren();
				wrapper.addListener((o, oldModel, newModel) -> {
					if (oldModel == newModel)
						return;
					if (oldModel != null) {
						ModelContext removed = children.remove(0);
						for (ViewContext<?> internalViewContext : removed.getViewContexts())
							internalViewContext.destroyChild();
					}
					if (newModel != null) {
						ModelContext childContext = new ModelContext(modelContext, newModel);
						new ViewContext(viewContext, childContext, childElement, childElement.nodeClass.isAssignableFrom(newModel.getClass()) ? newModel : childElement.createNode(viewContext.getNode()));
						children.add(childContext);
						assert children.size() == 1;
					}
				});
			}
		};
	}
}
