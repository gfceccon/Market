package br.usp.icmc.market;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;

import java.util.Optional;

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
        Button buyButton = new Button("Buy Selected Products");
        HBox hBoxProductsTab = new HBox(buyButton);
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
        setBuyProduct(buyButton);
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
        products.selectionModelProperty().get().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void setBuyProduct(Button button) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        ObservableList<Product> obsProducts = products.getSelectionModel().getSelectedItems();
        TableView<Product> selectedProducts = new TableView<>();

        dialog.setTitle("Buy Products");
        dialog.setHeaderText("Please place your order!");
        dialog.getDialogPane().setContent(selectedProducts);

        TableColumn<Product, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Product, String> expirationDate = new TableColumn<>("Expiration Date");
        expirationDate.setCellValueFactory(new PropertyValueFactory<>("expirationString"));
        expirationDate.setPrefWidth(100);
        TableColumn<Product, Integer> quantity = new TableColumn<>("Quantity");
        quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantity.setOnEditCommit(t -> (t.getTableView().getItems().get(t.getTablePosition().getRow())).setQuantity(t.getNewValue()));

        selectedProducts.setEditable(true);
        selectedProducts.getColumns().addAll(name, expirationDate, quantity);

        button.setOnAction(event -> {
            Optional<ButtonType> result = dialog.showAndWait();
            selectedProducts.setItems(obsProducts);
            result.ifPresent(r ->
            {
                if(r == ButtonType.OK)
                {
                    controller.buyProducts(obsProducts);

                    if(obsProducts.size() != 0) {
                        // TODO (quando tá faltando produtos)
                    }

                    products.getColumns().get(0).setVisible(false);
                    products.getColumns().get(0).setVisible(true);
                }
            });
        });
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
