package br.usp.icmc.market;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerController {
    private static ServerController instance;
    private ObservableList<User> users;
    private ObservableList<Product> products;
    private ServerSocket socket;
    private ArrayList<ClientThread> clients;

    private ServerController() throws IOException {
        users = FXCollections.observableArrayList();
        products = FXCollections.observableArrayList();

        socket = new ServerSocket(14786);
        clients = new ArrayList<>();
    }

    public static ServerController getInstance() throws IOException {
        if (instance == null)
            instance = new ServerController();
        return instance;
    }

    protected synchronized boolean requestProduct(Product request) {
        try {
            products
                    .stream()
                    .filter(filterProduct -> filterProduct.compareTo(request) == 0)
                    .findAny()
                    .ifPresent(product -> product.get(request.getQuantity()));
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    protected synchronized boolean addUser(User user) {
        if (users
                .stream()
                .filter(u -> u.getLogin().compareTo(user.getLogin()) == 0)
                .findAny()
                .isPresent()) {
            return false;
        }
        users.add(user);
        return true;
    }

    protected synchronized User loginUser(User user) {
        User logged = users
                .stream()
                .filter(u -> u.getLogin().compareTo(user.getLogin()) == 0 && u.comparePassword(user))
                .findAny()
                .get();
        return logged;
    }

    public ObservableList<User> getUsers() {
        return users;
    }

    public ObservableList<Product> getProducts() {
        return products;
    }

    private class ServerThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    Socket s = socket.accept();
                    if (s != null)
                        clients.add(new ClientThread(s));
                }
            } catch (Exception e) {

            }
        }
    }

    private class ClientThread extends Thread {
        private Socket socket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;
        private User userLogin;

        public ClientThread(Socket s) throws IOException {
            socket = s;
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

        }

        @Override
        public void run() {
            boolean quit = false;
            try {
                while (!quit) {
                    Message message = (Message) inputStream.readObject();
                    switch (message) {
                        case REGISTER_USER: {
                            User user = (User) inputStream.readObject();
                            outputStream.writeBoolean(addUser(user));
                        }
                        break;

                        case LOGIN_USER: {
                            User user = (User) inputStream.readObject();
                            userLogin = loginUser(user);
                            if (userLogin == null)
                                outputStream.writeBoolean(false);
                            else
                                outputStream.writeBoolean(true);
                        }
                        break;

                        case BUY_PRODUCTS: {
                            boolean hasNext = true;
                            while (hasNext) {
                                Object input = inputStream.readObject();
                                if (input instanceof Product) {
                                    Product product = (Product) input;
                                    outputStream.writeBoolean(requestProduct(product));
                                } else if (input instanceof Message) {
                                    message = (Message) input;
                                    if (message == Message.END)
                                        hasNext = false;
                                }
                            }
                        }
                        break;

                        case RECEIVE_NOTIFICATION: {
                            boolean hasNext = true;
                            while (hasNext) {
                                Object input = inputStream.readObject();
                                if (input instanceof Product) {
                                    Product product = (Product) input;
                                    outputStream.writeBoolean(requestProduct(product));
                                } else if (input instanceof Message) {
                                    message = (Message) input;
                                    if (message == Message.END)
                                        hasNext = false;
                                }
                            }
                        }
                        break;

                        case END:
                            quit = true;
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}
