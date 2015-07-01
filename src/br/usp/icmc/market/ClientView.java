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
        Alert buyProductsDialog = new Alert(Alert.AlertType.CONFIRMATION);
        Alert outofStockDialog = new Alert(Alert.AlertType.CONFIRMATION);

        TableView<Product> selectedProducts = new TableView<>();
        TableView<Product> outOfStockProducts = new TableView<>();

        buyProductsDialog.setTitle("Buy Products");
        buyProductsDialog.setHeaderText("Please place your order!");
        buyProductsDialog.getDialogPane().setContent(selectedProducts);

        outofStockDialog.setTitle("Out of Stock");
        outofStockDialog.setHeaderText("We are out of stock for these products!");
        outofStockDialog.getDialogPane().setContent(new VBox(outOfStockProducts, new Label("Do you want to receive a notification for theses items?")));

        TableColumn<Product, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Product, String> expirationDate = new TableColumn<>("Expiration Date");
        expirationDate.setCellValueFactory(new PropertyValueFactory<>("expirationString"));
        expirationDate.setPrefWidth(100);
        TableColumn<Product, Integer> quantity = new TableColumn<>("Quantity");
        quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantity.setOnEditCommit(t -> (t.getTableView().getItems().get(t.getTablePosition().getRow())).setQuantity(t.getNewValue()));

        TableColumn<Product, String> name1 = new TableColumn<>("Name");
        name1.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Product, String> expirationDate1 = new TableColumn<>("Expiration Date");
        expirationDate1.setCellValueFactory(new PropertyValueFactory<>("expirationString"));
        expirationDate1.setPrefWidth(100);
        TableColumn<Product, Integer> quantity1 = new TableColumn<>("Quantity");
        quantity1.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantity1.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantity1.setOnEditCommit(t -> (t.getTableView().getItems().get(t.getTablePosition().getRow())).setQuantity(t.getNewValue()));

        selectedProducts.setEditable(true);
        selectedProducts.getColumns().addAll(name, expirationDate, quantity);

        outOfStockProducts.setEditable(true);
        outOfStockProducts.getColumns().addAll(name1, expirationDate1, quantity1);

        button.setOnAction(event -> {
            ObservableList<Product> obsProducts = products.getSelectionModel().getSelectedItems();
            selectedProducts.setItems(obsProducts);
            quantity.setVisible(false);
            quantity.setVisible(true);
            Optional<ButtonType> result = buyProductsDialog.showAndWait();
            result.ifPresent(r ->
            {
                if (r == ButtonType.OK)
                {
                    ObservableList<Product> outOfStock = controller.buyProducts(obsProducts);

                    if (outOfStock.size() > 0)
                    {
                        outOfStockProducts.setItems(outOfStock);
                        quantity1.setVisible(false);
                        quantity1.setVisible(true);
                        Optional<ButtonType> result1 = outofStockDialog.showAndWait();
                        result1.ifPresent((b) -> {
                            if (b == ButtonType.OK)
                                controller.requestProducts(outOfStock);
                        });
                    }
                }
            });
            refresh();

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

                String productFilter = newValue.toLowerCase();

                return product.getName().toLowerCase().contains(productFilter) || product.getProvider().toLowerCase().contains(productFilter);
            });
        });

        products.setItems(sortedProduct);
    }

    private void refresh()
    {
        controller.refreshProducts();
        products.getColumns().get(0).setVisible(false);
        products.getColumns().get(0).setVisible(true);

    }
}
