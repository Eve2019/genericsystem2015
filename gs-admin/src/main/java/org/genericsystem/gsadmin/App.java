package org.genericsystem.gsadmin;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.genericsystem.gsadmin.AbstractGenericList.GenericList;

public class App extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Scene scene = new Scene(new Group());
		stage.setTitle("Generic System Reactive Example");
		new GenericList().init(((Group) scene.getRoot()));
		stage.setScene(scene);
		stage.show();
	}
}
