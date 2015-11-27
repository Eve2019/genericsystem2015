package org.genericsystem.todoApp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModelContext {

	protected List<ViewContext> viewContexts = new ArrayList<>();
	protected List<ModelContext> children = new AbstractList<ModelContext>() {

		List<ModelContext> wrappedList = new ArrayList<ModelContext>();

		@Override
		public ModelContext get(int index) {
			return wrappedList.get(index);
		}

		@Override
		public int size() {
			return wrappedList.size();
		}

		@Override
		public ModelContext remove(int index) {
			ModelContext removed = wrappedList.remove(index);
			for (ViewContext viewContext : removed.viewContexts)
				viewContext.getParent().destroyChild(viewContext);
			return removed;
		};

		@Override
		public void add(int index, ModelContext element) {
			wrappedList.add(index, element);
		};
	};
	protected ModelContext parent;
	protected Object model;

	public ModelContext(ModelContext parent, Object model) {
		this.parent = parent;
		this.model = model;
	}

	public ModelContext resolve(Class<?> clazz) {
		if (clazz.isAssignableFrom(model.getClass()))
			return this;
		else if (parent == null)
			throw new IllegalStateException("Unable to resolve method class : " + clazz);
		else
			return parent.resolve(clazz);
	}

	public ModelContext resolve(Method method) {
		if (method.getDeclaringClass().isAssignableFrom(model.getClass()))
			return this;
		else if (parent == null)
			throw new IllegalStateException("Unable to resolve method : " + method);
		else
			return parent.resolve(method);
	}

	public ModelContext resolve(Field field) {
		if (field.getDeclaringClass().isAssignableFrom(this.model.getClass()))
			return this;
		else if (parent != null)
			return parent.resolve(field);
		throw new IllegalStateException("Unable to resolve field : " + field);
	}

	public Object getModel() {
		return this.model;
	}

	public ModelContext getParent() {
		return this.parent;
	}

	public void setModel(Object model) {
		this.model = model;
	}

	public List<ModelContext> getChildren() {
		return this.children;
	}

	public void destroyChildrenContext(Object model) {
		Iterator<ModelContext> iterator = children.iterator();
		while (iterator.hasNext()) {
			ModelContext child = iterator.next();
			if (child.getModel() == model) {
				for (ViewContext viewContext : child.viewContexts)
					viewContext.getParent().destroyChild(viewContext);
				iterator.remove();
			}
		}
	}

	public void register(ViewContext viewContext) {
		this.viewContexts.add(viewContext);
	}

	public ModelContext createChild(Object child) {
		ModelContext childContext = new ModelContext(this, child);
		this.children.add(childContext);
		return childContext;
	}
}
