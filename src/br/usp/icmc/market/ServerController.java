package br.usp.icmc.market;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerController
{
	private static ServerController instance;
	private ObservableList<User> users;
	private ObservableList<Product> products;
	private ServerSocket socket;
	private ArrayList<ClientThread> clients;

	private class ServerThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				while (true)
				{
					Socket s = socket.accept();
					if(s != null)
						clients.add(new ClientThread(s));
				}
			}catch (Exception e)
			{

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
					switch (message)
					{
						case REGISTER_USER:
						{
							User user = (User) inputStream.readObject();
							outputStream.writeObject(addUser(user));
						}
							break;
						case LOGIN_USER:
							User u = (User) inputStream.readObject();
							user = getUser(u);
							if(user == null)
								outputStream.writeObject("");
							else
							{
								outputStream.writeObject(user.getSalt());
								u = (User)inputStream.readObject();
								if(loginUser(user, u))
									outputStream.writeObject(Message.OK);
								else
									outputStream.writeObject(Message.INCORRECT_PASSWORD);
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
							boolean hasNext = true;
							while (hasNext)
							{
								Object input = inputStream.readObject();
								if (input instanceof Product)
								{
									Product product = (Product) input;
									//TODO
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

		socket = new ServerSocket(14786);
		clients = new ArrayList<>();

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
	protected synchronized User getUser(User user)
	{
		return users.stream().
				filter(u -> u.getLogin().compareTo(user.getLogin()) == 0).
				findAny().
				get();
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
}
