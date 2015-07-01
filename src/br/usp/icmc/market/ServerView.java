package br.usp.icmc.market;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class ServerView extends Scene {
    private ServerController controller;
    private CSVManager manager;

    private TableView<Product> products;
    private TableView<User> users;

    private TextField userSearch;
    private TextField productSearch;

    private Alert error = new Alert(Alert.AlertType.ERROR);

    public ServerView(Pane pane) {
        super(pane);
        try {
            controller = ServerController.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        products = new TableView<>();
        users = new TableView<>();

        manager = new CSVManager();

        /* MENU BLOCK */
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        Menu menuImport = new Menu("Import");
        Menu menuExport = new Menu("Export");

        MenuItem menuImportUsers = new MenuItem("Users...");
        MenuItem menuImportProducts = new MenuItem("Products...");
        MenuItem menuExportUsers = new MenuItem("Users...");
        MenuItem menuExportProducts = new MenuItem("Products...");

        menuFile.getItems().addAll(menuImport, menuExport);
        menuImport.getItems().addAll(menuImportUsers, menuImportProducts);
        menuExport.getItems().addAll(menuExportUsers, menuExportProducts);
        menuBar.getMenus().addAll(menuFile);
        /* END MENU BLOCK */

		/* DATE PICKER BLOCK */
        Alert datePickerModal = new Alert(Alert.AlertType.CONFIRMATION);
        DatePicker datePicker = new DatePicker();
        datePickerModal.setTitle("Choose a Date");
        datePickerModal.setHeaderText("Choose a date!");
        datePickerModal.getDialogPane().setContent(datePicker);

		/* TABS BLOCK */
        userSearch = new TextField();
        userSearch.setPromptText("Search user by login or name");
        Button addUser = new Button("Add User");
        Button removeUser = new Button("Remove User");
        HBox hBoxUsersTab = new HBox(addUser, removeUser);
        hBoxUsersTab.setAlignment(Pos.CENTER);
        VBox vBoxUsersTab = new VBox(userSearch, users, hBoxUsersTab);
        Tab usersTab = new Tab("Users", vBoxUsersTab);
        usersTab.setClosable(false);

        productSearch = new TextField();
        productSearch.setPromptText("Search product by name or provider");
        Button addProduct = new Button("Add Product");
        Button updateProduct = new Button("Update Product");
        Button askProvider = new Button("Ask Provider");
        HBox hBoxProductsTab = new HBox(addProduct, updateProduct, askProvider);
        hBoxProductsTab.setAlignment(Pos.CENTER);
        VBox vBoxProductsTab = new VBox(productSearch, products, hBoxProductsTab);
        Tab productsTab = new Tab("Products", vBoxProductsTab);
        productsTab.setClosable(false);

        TabPane tabPane = new TabPane(usersTab, productsTab);
        VBox verticalPane = new VBox(menuBar, tabPane);
        verticalPane.setAlignment(Pos.TOP_CENTER);

        verticalPane.setPrefWidth(800);
        verticalPane.setPrefHeight(600);
        pane.getChildren().add(verticalPane);
        /* END TABS BLOCK */


		/* SETUP FUNCTIONS*/
        addColumns();
        fillTable();
        setAddProductButton(addProduct);
        setUpdateProductButton(updateProduct);

        setImportMenu(menuImportUsers, "Users");
        setImportMenu(menuImportProducts, "Products");
        setExportMenu(menuExportUsers, "Users");
        setExportMenu(menuExportProducts, "Products");
		/* END SETUP FUNCTIONS*/
    }

	private void setAskProvider(Button button)
	{
		Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
		ComboBox<String> providerComboBox = new ComboBox<>(FXCollections.observableArrayList(controller.getProviders()));
		providerComboBox.setValue("Provider");
		ObservableList<Product> obsProducts = FXCollections.observableArrayList();


		Pane pane = new Pane();

		dialog.setTitle("Add User");
		dialog.setHeaderText("Insert user information!");
		dialog.getDialogPane().setContent(pane);
		TableView<Product> providerProducts = new TableView<>();
		TableColumn<Product, String> name = new TableColumn<>("Name");
		name.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumn<Product, String> expirationDate = new TableColumn<>("Expiration Date");
		expirationDate.setCellValueFactory(new PropertyValueFactory<>("expirationString"));
		TableColumn<Product, Integer> quantity = new TableColumn<>("Quantity");
		quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		quantity.setEditable(true);

		providerProducts.getColumns().addAll(name, expirationDate, quantity);
		providerProducts.selectionModelProperty().get().setSelectionMode(SelectionMode.MULTIPLE);
		pane.getChildren().addAll(providerComboBox, providerProducts);
		providerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			obsProducts.setAll(controller.getProducts(newValue));
		});

		button.setOnAction(event -> {
			Optional<ButtonType> result = dialog.showAndWait();
		});
	}

    private void addColumns() {
        addUserColumns();
        addProductsColumns();
    }

    /*
		SET IMPORT MENU OF A GIVEN A TYPE
		Add ActionListener to the MenuItem menu that opens a file chooser
	 */
    private void setImportMenu(MenuItem menu, String type)
    {
        Stage fileChooserStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose " + type + " Data File");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"), new ExtensionFilter("All Files", "*.*"));

        menu.setOnAction(event -> {
            File selectedFile = fileChooser.showOpenDialog(fileChooserStage);
            if (selectedFile != null)
            {
                switch (type)
                {
                    case "Users":
                        controller.setUsers(manager.parseUserFile(selectedFile));
                        break;

                    case "Products":
                        controller.setProducts(manager.parseBookFile(selectedFile));
                        break;
                }
                fillTable();
            }
        });
    }

    /*
        SET EXPORT MENU OF A GIVEN A TYPE
        Add ActionListener to the MenuItem menu that opens a file chooser
     */
    private void setExportMenu(MenuItem menu, String type)
    {
        Stage fileChooserStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose " + type + " Data File");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"), new ExtensionFilter("All Files", "*.*"));

        menu.setOnAction(event -> {
            File selectedFile = fileChooser.showSaveDialog(fileChooserStage);
            if (selectedFile != null)
            {
                switch (type)
                {
                    case "Users":
                        manager.writeFile(selectedFile, controller.getUsers());
                        break;

                    case "Products":
                        manager.writeFile(selectedFile, controller.getProducts());
                        break;
                }
            }
        });
    }

    private void setAddProductButton(Button button)
    {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        TextField nameField = new TextField();
        TextField priceField = new TextField();
        TextField quantityField = new TextField();
        TextField expirationDateField = new TextField();
        TextField providerField = new TextField();

        Label nameLabel = new Label("Product Name: ");
        Label priceLabel = new Label("Price: ");
        Label quantityLabel = new Label("Quantity: ");
        Label expirationDateLabel = new Label("Product Expiration Date: ");
        Label providerLabel = new Label("Provider: ");

        GridPane.setHalignment(nameLabel, HPos.RIGHT);
        GridPane.setHalignment(priceLabel, HPos.RIGHT);
        GridPane.setHalignment(quantityLabel, HPos.RIGHT);
        GridPane.setHalignment(expirationDateLabel, HPos.RIGHT);
        GridPane.setHalignment(providerLabel, HPos.RIGHT);

        GridPane pane = new GridPane();

        pane.addColumn(0, nameLabel, priceLabel, quantityLabel, expirationDateLabel, providerLabel);

        pane.addColumn(1, nameField, priceField, quantityField, expirationDateField, providerField);

        dialog.setTitle("Add Product");
        dialog.setHeaderText("Insert product information!");
        dialog.getDialogPane().setContent(pane);

        button.setOnAction(event -> {
            Platform.runLater(() -> nameField.requestFocus());
            Optional<ButtonType> returnValue = dialog.showAndWait();
            if (returnValue.isPresent() && returnValue.get().equals(ButtonType.OK))
            {
                try
                {
                    controller.addProduct(nameField.getText(), priceField.getText(), quantityField.getText(), expirationDateField.getText(), providerField.getText());
                } catch (Exception e)
                {
                    e.printStackTrace();
                    error.setTitle("Error adding product");
                    error.setHeaderText(e.getMessage());
                    error.show();
                }
            }

            nameField.clear();
            priceField.clear();
            quantityField.clear();
            expirationDateField.clear();
            providerField.clear();
        });
    }

    private void setUpdateProductButton(Button button)
    {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        TextField priceField = new TextField();
        TextField quantityField = new TextField();

        Label priceLabel = new Label("Price: ");
        Label quantityLabel = new Label("Quantity: ");

        GridPane.setHalignment(priceLabel, HPos.RIGHT);
        GridPane.setHalignment(quantityLabel, HPos.RIGHT);

        GridPane pane = new GridPane();

        pane.addColumn(0, priceLabel, quantityLabel);

        pane.addColumn(1, priceField, quantityField);

        dialog.setHeaderText("Update product information!");
        dialog.getDialogPane().setContent(pane);

        button.setOnAction(event -> {
            if(products.selectionModelProperty().get().getSelectedItem() != null) {
                Platform.runLater(() -> priceField.requestFocus());
                Optional<ButtonType> returnValue = dialog.showAndWait();
                if (returnValue.isPresent() && returnValue.get().equals(ButtonType.OK) && products.selectionModelProperty().get().getSelectedItem() != null) {
                    dialog.setTitle("Update " + products.selectionModelProperty().get().getSelectedItem().getName());
                    try {
                        products.selectionModelProperty().get().getSelectedItem().setPrice(Float.parseFloat(priceField.getText()));
                        products.selectionModelProperty().get().getSelectedItem().setQuantity(Integer.parseInt(quantityField.getText()));
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        error.setTitle("Error adding product");
                        error.setHeaderText(e.getMessage());
                        error.show();
                    }
                    products.getColumns().get(0).setVisible(false);
                    products.getColumns().get(0).setVisible(true);
                }

                priceField.clear();
                quantityField.clear();
            }
            else
            {
                error.setTitle("Error updating product");
                error.setHeaderText("You must select a product");
                error.show();
            }
        });
    }

    /*
        TABLE COLUMNS BLOCK
        Add columns to user, book and loan tables
        Configure columns
     */
    private void addUserColumns() {
        TableColumn<User, String> login = new TableColumn<>("Login");
        login.setCellValueFactory(new PropertyValueFactory<>("login"));
        TableColumn<User, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<User, String> address = new TableColumn<>("Address");
        address.setCellValueFactory(new PropertyValueFactory<>("address"));
        TableColumn<User, String> phone = new TableColumn<>("Phone");
        phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<User, String> email = new TableColumn<>("E-mail");
        email.setCellValueFactory(new PropertyValueFactory<>("email"));

        users.getColumns().addAll(login, name, address, phone, email);
        users.selectionModelProperty().get().setSelectionMode(SelectionMode.SINGLE);
    }

    private void addProductsColumns() {
        TableColumn<Product, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Product, String> price = new TableColumn<>("Price");
        price.setCellValueFactory(new PropertyValueFactory<>("price"));
        TableColumn<Product, String> expirationDate = new TableColumn<>("Expiration Date");
        expirationDate.setCellValueFactory(new PropertyValueFactory<>("expirationString"));
        TableColumn<Product, String> provider = new TableColumn<>("Provider");
        provider.setCellValueFactory(new PropertyValueFactory<>("provider"));
        TableColumn<Product, Integer> quantity = new TableColumn<>("Quantity");
        quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        products.getColumns().addAll(name, price, quantity, expirationDate, provider);
        products.selectionModelProperty().get().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /*
        Fill tables with controller's data
        Create SortedList and binds a comparator
     */
    private void fillTable() {
        ObservableList<User> obsUser = controller.getUsers();
        ObservableList<Product> obsProduct = controller.getProducts();

        FilteredList<User> filteredUser = new FilteredList<>(obsUser, u -> true);
        FilteredList<Product> filteredProduct = new FilteredList<>(obsProduct, b -> true);

        SortedList<User> sortedUser = new SortedList<>(filteredUser);
        SortedList<Product> sortedProduct = new SortedList<>(filteredProduct);

        sortedUser.comparatorProperty().bind(users.comparatorProperty());
        sortedProduct.comparatorProperty().bind(products.comparatorProperty());

        userSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUser.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty())
                    return true;

                String userFilter = newValue.toLowerCase();

                if (user.getLogin().toLowerCase().contains(userFilter))
                    return true;
                else if (user.getName().toLowerCase().contains(userFilter))
                    return true;
                return false;
            });
        });

        productSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredProduct.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty())
                    return true;

                String bookFilter = newValue.toLowerCase();

                if (product.getName().toLowerCase().contains(bookFilter))
                    return true;
                else if (product.getProvider().contains(bookFilter))
                    return true;
                return false;
            });
        });


        users.setItems(sortedUser);
        products.setItems(sortedProduct);
    }
}