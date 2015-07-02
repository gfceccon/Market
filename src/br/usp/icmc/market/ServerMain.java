package br.usp.icmc.market;

import javafx.application.Application;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/* Server Main
 * JavaFX application
 * Calls Server View
 */
public class ServerMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ServerView server = new ServerView(new Pane());
        primaryStage.setScene(server);
        primaryStage.setTitle("Market Management System");
        primaryStage.setWidth(800);
        primaryStage.setMaxHeight(550);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest((e) -> server.onExit());
        primaryStage.show();
    }
}
