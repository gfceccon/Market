package br.usp.icmc.market;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class ClientMain extends Application {
    private ClientController controller;
    private Stage primaryStage;
    Alert error;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        controller = ClientController.getInstance();
        connectScene();

        error = new Alert(Alert.AlertType.ERROR);
        primaryStage.setTitle("Online Market");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void connectScene(){
        Label lblIP = new Label("Server's IP: ");
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
            primaryStage.setMaxWidth(800);
            primaryStage.setMaxHeight(550);

            controller .connect(txtIP.getText());
            loginScene();
        });

        txtIP.setOnAction(event -> btnOK.fire());
        btnCancel.setOnAction(event -> System.exit(0));

        Scene connectScene = new Scene(vBox);
        primaryStage.setScene(connectScene);
    }

    private void loginScene(){
        Label lblUser = new Label("Username: ");
        Label lblPassword = new Label("Password: ");

        TextField txtUser = new TextField();
        PasswordField txtPassword = new PasswordField();

        Button btnOK = new Button("OK");
        Button btnCancel = new Button("Cancel");
        Button btnNewUser = new Button("New User");

        GridPane pane = new GridPane();
        pane.addColumn(0, lblUser, lblPassword);
        pane.addColumn(1, txtUser, txtPassword);

        HBox hBoxButton = new HBox(btnNewUser, btnOK, btnCancel);
        VBox vBox = new VBox(pane, hBoxButton);

        GridPane.setHalignment(lblUser, HPos.RIGHT);
        GridPane.setHalignment(lblPassword, HPos.RIGHT);
        pane.setAlignment(Pos.CENTER);
        hBoxButton.setAlignment(Pos.CENTER);
        vBox.setAlignment(Pos.CENTER);

        btnOK.setOnAction(event -> {
            if(controller.login(txtUser.getText(), txtPassword.getText())){
                primaryStage.setScene(new ClientView(new Pane()));
            } else {
                error.setContentText("Incorrect Username or Password!");
                error.show();
            }
        });

        btnCancel.setOnAction(event -> System.exit(0));

        Scene loginScene = new Scene(vBox);

        setAddUserButton(btnNewUser);
        primaryStage.setScene(loginScene);
    }

    private void setAddUserButton(Button button) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        TextField nameField = new TextField();
        TextField addressField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField loginField = new TextField();
        PasswordField passwordField = new PasswordField();

        Label nameLabel = new Label("Name: ");
        Label addressLabel = new Label("Address: ");
        Label phoneLabel = new Label("Phone: ");
        Label emailLabel = new Label("Email: ");
        Label loginLabel = new Label("Login: ");
        Label passwordLabel = new Label("Password: ");

        GridPane.setHalignment(nameLabel, HPos.RIGHT);
        GridPane.setHalignment(addressLabel, HPos.RIGHT);
        GridPane.setHalignment(phoneLabel, HPos.RIGHT);
        GridPane.setHalignment(emailLabel, HPos.RIGHT);
        GridPane.setHalignment(loginLabel, HPos.RIGHT);
        GridPane.setHalignment(passwordLabel, HPos.RIGHT);

        GridPane pane = new GridPane();

        pane.addColumn(0, nameLabel, addressLabel, phoneLabel, emailLabel, loginLabel, passwordLabel);

        pane.addColumn(1, nameField, addressField, phoneField, emailField, loginField, passwordField);

        dialog.setTitle("Add User");
        dialog.setHeaderText("Insert user information!");
        dialog.getDialogPane().setContent(pane);

        button.setOnAction(event -> {
            Platform.runLater(nameField::requestFocus);
            Optional<ButtonType> returnValue = dialog.showAndWait();
            if (returnValue.isPresent() && returnValue.get().equals(ButtonType.OK)) {
                try {
                    Message response = controller.addUser(nameField.getText(), addressField.getText(), phoneField.getText(), emailField.getText(), loginField.getText(), passwordField.getText());
                    if(response == null)
                    {
                        error.setContentText("Error creating user, please try again later.");
                        error.show();
                    }
                    else switch (response)
                    {
                        case USER_ALREADY_EXISTS:
                            error.setContentText("This login already exists!");
                            error.show();
                            break;
                        case USER_CREATED:
                            Alert UserCreatedAlert = new Alert(Alert.AlertType.INFORMATION);
                            UserCreatedAlert.setTitle("User Created");
                            UserCreatedAlert.setHeaderText("User successfully created!");
                            UserCreatedAlert.show();
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    /*error.setTitle("Error adding user");
                    error.setHeaderText(e.getMessage());
                    error.show();*/
                }
            }

            nameField.clear();
            addressField.clear();
            phoneField.clear();
            emailField.clear();
            loginField.clear();
            passwordField.clear();
        });
    }
}
