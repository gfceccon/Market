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

    private ClientController() {
        products = FXCollections.observableArrayList();
    }

    public void connect(String IP){
        try {
            socket = new Socket(IP, 14786);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ClientController getInstance() {
        if (instance == null)
            instance = new ClientController();
        return instance;
    }

    public ObservableList<Product> getProducts() {
        return products;
    }

    public Message addUser(String name, String address, String phone, String email, String login, String password){
        User newUser = new User(name, address, phone, email, login, password);

        try {
            outputStream.writeObject(Message.REGISTER_USER);
            outputStream.writeObject(newUser);
            return (Message)inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ObservableList<Product> refreshProducts() {
        //TODO
        return null;
    }

    public void buyProduct(){
        //TODO
    }

    public boolean login(String username, String password){
        User user = new User();

        user.setLogin(username);
        user.setPassword(password);

        try {
            outputStream.writeObject(Message.LOGIN_USER);
            outputStream.writeObject(user);

            Object input =  inputStream.readObject();
            if(input instanceof Message)
            {
            }
            else
            {
                String salt = (String)input;
                if (salt.isEmpty())
                {
                    //TODO
                }
                else
                {
                    user.setSalt(salt);
                    outputStream.writeObject(user);
                    switch ((Message) inputStream.readObject())
                    {
                        case INCORRECT_PASSWORD:
                            return false;

                        case OK:
                            return true;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }
}
