package br.usp.icmc.market;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientController {
    private static ClientController instance;
    private ObservableList<Product> products;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private ClientController(String IP) {
        products = FXCollections.observableArrayList();

        try {
            socket = new Socket(IP, 14786);
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ClientController getInstance(String IP) {
        if (instance == null)
            instance = new ClientController(IP);
        return instance;
    }

    public ObservableList<Product> getProducts() {
        return products;
    }

    public void addUser(String name, String address, String phone, String email, String login, String password){
        User newUser = new User(name, address, phone, email, login, password);

        try {
            outputStream.writeObject(Message.REGISTER_USER);
            outputStream.writeObject(newUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Product> refreshProducts() {
        //TODO
        return null;
    }

    public void buyProduct(){
        //TODO
    }
}
