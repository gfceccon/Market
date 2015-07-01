package br.usp.icmc.market;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

public class ServerController
{
	private static ServerController instance;
	private ObservableList<User> users;
	private ObservableList<Product> products;
	private ArrayList<Request> requests;
	private ServerSocket socket;
	private ArrayList<ClientThread> clients;

	private class ServerThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					Socket s = socket.accept();
					if(s != null)
					{
						ClientThread t = new ClientThread(s);
						clients.add(t);
						t.start();
					}
				}
			}catch (Exception e)
			{
                e.printStackTrace();
			}
		}
	}

	private class ClientThread extends Thread
	{
		private Socket socket;
		private ObjectInputStream inputStream;
		private ObjectOutputStream outputStream;
		private User user;

		public ClientThread(Socket s) throws IOException
		{
			socket = s;
			inputStream = new ObjectInputStream(socket.getInputStream());
			outputStream = new ObjectOutputStream(socket.getOutputStream());
		}
		@Override
		public void run()
		{
			boolean quit = false;
			try
			{
				while(!quit)
				{
					Message message = (Message)inputStream.readObject();
					System.out.println(message);
					switch (message)
					{
						case REGISTER_USER:
						{
							User u = (User) inputStream.readObject();
							outputStream.writeObject(addUser(u));
						}
							break;
						case LOGIN_USER:
						{
							User u = (User) inputStream.readObject();
							user = getUser(u);
							System.out.println(user);
							if (user == null)
								outputStream.writeObject("");
							else
							{
								outputStream.writeObject(user.getSalt());
								u = (User) inputStream.readObject();
								if (loginUser(user, u))
									outputStream.writeObject(Message.OK);
								else
									outputStream.writeObject(Message.INCORRECT_PASSWORD);
							}
						}
							break;
						case BUY_PRODUCTS:
						{
							boolean hasNext = true;
							while (hasNext)
							{
								Object input = inputStream.readObject();
								if (input instanceof Product)
								{
									Product product = (Product) input;
									outputStream.writeObject(requestProduct(product));
								}
								else if (input instanceof Message)
								{
									message = (Message) input;
									if (message == Message.END)
										hasNext = false;
								}
							}
						}
							break;
						case RECEIVE_NOTIFICATION:
						{
							Request request = new Request(user);
							boolean hasNext = true;
							while (hasNext)
							{
								Object input = inputStream.readObject();
								if (input instanceof Product)
								{
									Product product = (Product) input;
									request.products.add(product);
								}
								else if (input instanceof Message)
								{
									message = (Message) input;
									if (message == Message.END)
										hasNext = false;
								}
							}
							requests.add(request);
						}
							break;
						case END:
							quit = true;
							break;
					}
				}
			}catch (Exception e)
			{
			}
		}
	}

	private ServerController() throws IOException
	{
		users = FXCollections.observableArrayList();
		products = FXCollections.observableArrayList();
		requests = new ArrayList<>();

		socket = new ServerSocket(14786);
		clients = new ArrayList<>();

        new ServerThread().start();
	}

	public static ServerController getInstance() throws IOException
	{
		if(instance == null)
			instance = new ServerController();
		return instance;
	}

	protected synchronized Message requestProduct(Product request)
	{
		try
		{
			products.stream().filter(filterProduct -> filterProduct.compareTo(request) == 0).findAny().ifPresent(product -> product.get(request.getQuantity()));
		}catch(IllegalArgumentException e)
		{
			return Message.OUT_OF_STOCK;
		}
		return Message.OK;
	}

	public ArrayList<Product> getProducts(String provider)
	{
		ArrayList<Product> providerProducts = new ArrayList<>();
		products.stream().filter(filterProduct -> filterProduct.getProvider().compareTo(provider) == 0).forEach(p ->
		{
			Product copy = p.clone();
			providerProducts.add(copy);
		});
		return providerProducts;
	}

	public void setUsers(ObservableList<User> users) {
		this.users = users;
	}

	public void setProducts(ObservableList<Product> products) {
		this.products = products;
	}

	protected synchronized Message addUser(User user)
	{
		if(users.stream().
				filter(u -> u.getLogin().compareTo(user.getLogin()) == 0).
				findAny().
				isPresent())
		{
			return Message.USER_ALREADY_EXISTS;
		}
		users.add(user);
		return Message.USER_CREATED;
	}

	public Product addProduct(String name, String price, String quantity, String expirationDate, String provider) throws Exception
	{
		Product newProduct = new Product(name, Float.parseFloat(price), Integer.parseInt(quantity), LocalDate.parse(expirationDate, DateTimeFormatter.ofPattern("dd'/'MM'/'yyyy")), provider); // Instantiates a new user
		products.add(newProduct); // Adds the user to the list in memory

		return newProduct;
	}

	protected synchronized User getUser(User user)
	{
		Optional<User> result =  users.stream().
				filter(u -> u.getLogin().compareTo(user.getLogin()) == 0).
				findAny();
		if(result.isPresent())
			return result.get();
		return null;
	}

	protected synchronized boolean loginUser(User login, User inputUser)
	{
		return users.stream().
				filter(u -> u.getLogin().compareTo(login.getLogin()) == 0 && u.comparePassword(inputUser)).
				findAny().
				isPresent();
	}

	public ObservableList<User> getUsers()
	{
		return users;
	}

	public ObservableList<Product> getProducts()
	{
		return products;
	}

	public ArrayList<String> getProviders()
	{
		ArrayList<String> providers = new ArrayList<>();
		products.stream().map(Product::getProvider).distinct().forEach(providers::add);
		return providers;
	}

	public void provide(ObservableList<Product> obsProducts)
	{
		obsProducts.stream().filter(filteredProduct -> filteredProduct.getQuantity() > 0).
				forEach(obsProduct -> products.stream().filter(product -> product.compareTo(obsProduct) == 0).findAny().ifPresent(p -> p.add(obsProduct.getQuantity())));
	}
}
