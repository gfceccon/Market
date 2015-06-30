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

public class ClientView extends Scene {
    private ClientController controller;
    private TableView<Product> products;
    private TextField productSearch;

    public ClientView(Pane pane) {
        super(pane);
        controller = ClientController.getInstance();

        products = new TableView<>();

		/* TABS BLOCK */
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

        TabPane tabPane = new TabPane(productsTab);
        VBox verticalPane = new VBox(tabPane);
        verticalPane.setAlignment(Pos.TOP_CENTER);

        verticalPane.setPrefWidth(800);
        verticalPane.setPrefHeight(600);
        pane.getChildren().add(verticalPane);
        /* END TABS BLOCK */

		/* SETUP FUNCTIONS*/
        addProductsColumns();
        fillTable();
        /* END SETUP FUNCTIONS*/
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

        products.getColumns().addAll(name, price, expirationDate, provider);
        products.selectionModelProperty().get().setSelectionMode(SelectionMode.SINGLE);
    }

    /*
        Fill tables with controller's data
        Create SortedList and binds a comparator
     */
    private void fillTable() {
        ObservableList<Product> obsProduct = controller.getProducts();
        FilteredList<Product> filteredProduct = new FilteredList<>(obsProduct, b -> true);
        SortedList<Product> sortedProduct = new SortedList<>(filteredProduct);

        sortedProduct.comparatorProperty().bind(products.comparatorProperty());

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

        products.setItems(sortedProduct);
    }
}
