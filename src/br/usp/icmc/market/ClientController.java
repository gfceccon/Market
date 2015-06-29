package br.usp.icmc.market;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClientController {
    private static ClientController instance;
    private ObservableList<Product> products;

    private ClientController() {
        products = FXCollections.observableArrayList();
    }

    public static ClientController getInstance() {
        if (instance == null)
            instance = new ClientController();
        return instance;
    }

    public ObservableList<Product> getProducts() {
        return products;
    }
}
