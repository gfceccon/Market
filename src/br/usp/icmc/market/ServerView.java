package br.usp.icmc.market;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ServerView extends Scene {
    private ServerController controller;

    private TableView<Product> products;
    private TableView<User> users;

    private TextField userSearch;
    private TextField productSearch;

    public ServerView(Pane pane) {
        super(pane);
        try {
            controller = ServerController.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        products = new TableView<>();
        users = new TableView<>();

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
        VBox verticalPane = new VBox(tabPane);
        verticalPane.setAlignment(Pos.TOP_CENTER);

        verticalPane.setPrefWidth(800);
        verticalPane.setPrefHeight(600);
        pane.getChildren().add(verticalPane);
        /* END TABS BLOCK */


		/* SETUP FUNCTIONS*/
        addColumns();
        fillTable();
		/* END SETUP FUNCTIONS*/
    }

    private void addColumns() {
        addUserColumns();
        addProductsColumns();
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
        products.selectionModelProperty().get().setSelectionMode(SelectionMode.SINGLE);
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