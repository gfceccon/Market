package br.usp.icmc.market;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
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
        Label lblIP = new Label("Server's IP: ");
        Label lblUser = new Label("Username: ");
        Label lblPassword = new Label("Password: ");

        TextField txtIP = new TextField();
        TextField txtUser = new TextField();
        PasswordField txtPassword = new PasswordField();

        Button btnOK = new Button("OK");
        Button btnCancel = new Button("Cancel");
        Button btnNewUser = new Button("New User");

        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.addColumn(0, lblIP, lblUser, lblPassword);
        pane.addColumn(1, txtIP, txtUser, txtPassword);

        HBox hBoxButton = new HBox(btnNewUser, btnOK, btnCancel);
        VBox vBox = new VBox(pane, hBoxButton);

        GridPane.setHalignment(lblIP, HPos.RIGHT);
        GridPane.setHalignment(lblUser, HPos.RIGHT);
        GridPane.setHalignment(lblPassword, HPos.RIGHT);

        hBoxButton.setAlignment(Pos.CENTER);
        vBox.setAlignment(Pos.CENTER);

        btnOK.setOnAction(event -> {
            primaryStage.setMaxWidth(800);
            primaryStage.setMaxHeight(550);
            primaryStage.setScene(new ClientView(new Pane(), txtIP.getText()));
        });

        txtIP.setOnAction(event -> btnOK.fire());
        btnCancel.setOnAction(event -> System.exit(0));

        Scene ipChooser = new Scene(vBox);
        primaryStage.setScene(ipChooser);
        primaryStage.setTitle("Online Market");

        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
