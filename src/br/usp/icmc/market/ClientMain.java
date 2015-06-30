package br.usp.icmc.market;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Label lblIP = new Label("Insert the server's IP: ");
        TextField txtIP = new TextField();
        Button btnOK = new Button("OK");
        Button btnCancel = new Button("Cancel");
        HBox hBoxIP = new HBox(lblIP, txtIP);
        HBox hBoxButton = new HBox(btnOK, btnCancel);
        VBox vBox = new VBox(hBoxIP, hBoxButton);

        hBoxIP.setAlignment(Pos.CENTER);
        hBoxButton.setAlignment(Pos.CENTER);
        vBox.setAlignment(Pos.CENTER);

        btnOK.setOnAction(event -> {
            primaryStage.setTitle("Online Market");
            primaryStage.setMaxWidth(800);
            primaryStage.setMaxHeight(550);
            primaryStage.setScene(new ClientView(new Pane(), txtIP.getText()));
        });

        txtIP.setOnAction(event -> btnOK.fire());
        btnCancel.setOnAction(event -> System.exit(0));

        Scene ipChooser = new Scene(vBox);
        primaryStage.setScene(ipChooser);
        primaryStage.setTitle("Input Server's IP");

        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
