package org.genericsystem.todoApp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.genericsystem.todoApp.IElement.Element;
import org.genericsystem.todoApp.binding.Binders.ClickBinder;
import org.genericsystem.todoApp.binding.Binders.EnterBinder;
import org.genericsystem.todoApp.binding.Binders.ForeachBinder;
import org.genericsystem.todoApp.binding.Binders.TextBinder;
import org.genericsystem.todoApp.binding.Binding;

public class TodoList {

	public StringProperty name = new SimpleStringProperty();
	public ObservableList<Todo> todos = FXCollections.observableArrayList();

	public void create() {
		Todo todo = new Todo(name.getValue());
		todos.add(todo);
	}

	public void remove(Todo todo) {
		this.todos.remove(todo);
		System.out.println("kkk");
	}

	public Node init() throws IllegalArgumentException, IllegalAccessException {

		Field attributeTodos = null;
		Field nameAttribute = null;
		Field attributeTodo = null;
		Method methodRemove = null;
		Method methodCreate = null;
		try {
			nameAttribute = TodoList.class.getField("name");
			attributeTodos = TodoList.class.getField("todos");
			attributeTodo = Todo.class.getField("stringProperty");
			methodRemove = TodoList.class.getMethod("remove", Todo.class);
			methodCreate = TodoList.class.getMethod("create");
		} catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
			e.printStackTrace();
		}

		List<IElement> content = new ArrayList<IElement>();
		List<IElement> contentRoot = new ArrayList<IElement>();
		IElement elmVBoxRoot = new Element(VBox.class, "", contentRoot, null);

		IElement elmVBox = new Element(VBox.class, "", content, Binding.bindTo(attributeTodos, ForeachBinder.foreach()));
		IElement elmLabel = new Element(Label.class, "", null, Binding.bindTo(attributeTodo, TextBinder.textBind()));
		IElement elmButtonRemove = new Element(Button.class, "remove", null, Binding.bindTo(methodRemove, ClickBinder.methodBind()));
		IElement elmButtonCreate = new Element(Button.class, "create", null, Binding.bindTo(methodCreate, ClickBinder.methodBind()));

		IElement elmTextField = new Element(TextField.class, "", null, Binding.bindTo(nameAttribute, EnterBinder.enterBind()));

		content.add(elmLabel);
		content.add(elmButtonRemove);
		contentRoot.add(elmVBox);
		contentRoot.add(elmTextField);
		contentRoot.add(elmButtonCreate);
		return elmVBoxRoot.apply(this).node;
	}
}
