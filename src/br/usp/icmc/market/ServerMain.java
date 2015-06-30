package br.usp.icmc.market;

import javafx.application.Application;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ServerMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new ServerView(new Pane()));
        primaryStage.setTitle("Market Management System");
        primaryStage.setWidth(800);
        primaryStage.setMaxHeight(550);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
