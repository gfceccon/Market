package br.usp.icmc.market;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientController {
    private static ClientController instance;
    private ObservableList<Product> products;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private ClientController() {
    }

    public void connect(String IP){
        try {
            Socket socket = new Socket(IP, 14786);
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
        if(products == null)
            products = refreshProducts();
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
        ArrayList<Product> newList = new ArrayList<>();
        try
        {
            outputStream.writeObject(Message.GET_PRODUCTS);
            Object input;
            boolean quit = false;
            while(!quit)
            {
                input = inputStream.readObject();
                if(input instanceof Message)
                {
                    switch ((Message)input)
                    {
                        case END:
                            quit = true;
                            break;
                    }
                }
                else if(input instanceof Product)
                {
                    Product p = (Product) input;
                    newList.add(p);
                }
            }

        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return FXCollections.observableArrayList(newList);
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
            String salt = (String)input;
            if (salt.isEmpty())
            {
                //TODO
            }
            else
            {
                user = new User();
                user.setLogin(username);
                user.setPassword(password);
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

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

}
