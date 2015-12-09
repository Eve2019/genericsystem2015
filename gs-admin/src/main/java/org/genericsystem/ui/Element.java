package org.genericsystem.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;

public class Element<N> {
	public final Class<N> classNode;
	public final List<Binding<N, ?, ?>> metaBindings = new ArrayList<>();
	public final List<Binding<N, ?, ?>> bindings = new ArrayList<>();
	private final Element<?> parent;
	private final List<Element<?>> children = new ArrayList<>();
	private final Function<?, ObservableList<?>> getGraphicChildren;

	private List<Boot<N>> boots = new ArrayList<>();

	@Override
	public String toString() {
		return "Element<" + classNode.getSimpleName() + ">";
	}

	@SafeVarargs
	public <PARENTNODE extends Pane> Element(Element<PARENTNODE> parent, Class<N> classNode, Binding<N, ?, ?>... binding) {
		this(parent, classNode, Pane::getChildren, binding);
	}

	@SafeVarargs
	public <PARENTNODE extends Pane> Element(Element<PARENTNODE> parent, Class<N> classNode, List<Binding<N, ?, ?>> metaBindings, Binding<N, ?, ?>... binding) {
		this(parent, classNode, Pane::getChildren, metaBindings, binding);
	}

	@SafeVarargs
	public <PARENTNODE> Element(Element<PARENTNODE> parent, Class<N> classNode, Function<? super PARENTNODE, ObservableList<?>> getGraphicChildren, Binding<N, ?, ?>... binding) {
		this(parent, classNode, getGraphicChildren, Collections.emptyList(), binding);
	}

	@SafeVarargs
	public <PARENTNODE> Element(Element<PARENTNODE> parent, Class<N> classNode, Function<? super PARENTNODE, ObservableList<?>> getGraphicChildren, List<Binding<N, ?, ?>> metaBindings, Binding<N, ?, ?>... binding) {
		this.classNode = classNode;
		this.parent = parent;
		this.metaBindings.addAll(metaBindings);
		this.bindings.addAll(Arrays.asList(binding));
		this.getGraphicChildren = getGraphicChildren;
		if (parent != null)
			parent.<N> getChildren().add(this);
	}

	@SafeVarargs
	public final void addBoots(Boot<N>... boot) {
		this.boots.addAll(Arrays.asList(boot));
	}

	public List<Boot<N>> getBootList() {
		return boots;
	}

	public void addMetaBinding(Binding<N, ?, ?> metaBinding) {
		metaBindings.add(metaBinding);
	}

	@SafeVarargs
	public final void addBinding(Binding<N, ?, ?>... binding) {
		bindings.addAll(Arrays.asList(binding));
	}

	@SuppressWarnings("unchecked")
	public <PARENTNODE> ObservableList<N> getGraphicChildren(PARENTNODE graphicParent) {
		return ((Function<PARENTNODE, ObservableList<N>>) (Function<?, ?>) getGraphicChildren).apply(graphicParent);
	}

	public N apply(Object model) {
		N node = createNode();
		new ViewContext<>(new ModelContext(null, model), this, node, null);
		return node;
	}

	N createNode() {
		try {
			return classNode.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <CHILDNODE> List<Element<CHILDNODE>> getChildren() {
		return (List) children;
	}

	public Element<?> getParent() {
		return parent;
	}

	private Map<List, Map<Element, Integer>> map = new IdentityHashMap<List, Map<Element, Integer>>() {
		@Override
		public Map<Element, Integer> get(Object key) {
			Map<Element, Integer> internal = super.get(key);
			if (internal == null)
				put((List) key, internal = new IdentityHashMap<Element, Integer>() {
					@Override
					public Integer get(Object key) {
						Integer size = super.get(key);
						if (size == null)
							put((Element) key, size = 0);
						return size;
					};
				});
			return internal;
		};
	};

	void incrementSize(List graphicChildren, Element child) {
		Map<Element, Integer> internal = map.get(graphicChildren);
		internal.put(child, internal.get(child) + 1);
	}

	void decrementSize(List graphicChildren, Element child) {
		Map<Element, Integer> internal = map.get(graphicChildren);
		int size = internal.get(child) - 1;
		assert size >= 0;
		if (size == 0)
			internal.remove(child);// remove map if 0 for avoid heap pollution
		else
			internal.put(child, size);
	}

	int computeIndex(List graphicChildren, Element childElement) {
		int indexInChildren = 0;
		for (Element child : getChildren()) {
			indexInChildren += map.get(graphicChildren).get(child);
			if (child == childElement)
				break;
		}
		return indexInChildren;
	}
}
