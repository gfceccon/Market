package br.usp.icmc.market;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/* Server Controller
 * Manage client connection, input and output of products and product list
 * Manage user login, creation and requests
 * Manage socket threads (multiple connections)
 */
public class ServerController
{
	private static ServerController instance;
	private ServerThread server;
	private ObservableList<User> users;
	private ObservableList<Product> products;
	private ArrayList<Request> requests;
	private ServerSocket socket;
	private ArrayList<ClientThread> clients;

	// Server socket thread, accepts new connections and create client threads
	private class ServerThread extends Thread
	{
		public boolean quit = false;
		@Override
		public void run()
		{
			try
			{
				while(!quit)
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
				if(!quit)
                	e.printStackTrace();
			}
		}
	}

	// Client socket thread, controls client communication (user, products and requests)
	private class ClientThread extends Thread
	{
		public boolean quit = false;

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
			try
			{
				while(!quit)
				{
					//Reads the message header
					Message message = (Message)inputStream.readObject();
					switch (message)
					{
						// Refresh client product list
						case GET_PRODUCTS:
						{
							outputStream.reset();
							for(Product p : products)
							{
								outputStream.writeObject(p);
							}
							outputStream.writeObject(Message.END);
						}
							break;
						// Register new user
						case REGISTER_USER:
						{
							User u = (User) inputStream.readObject();
							outputStream.writeObject(addUser(u));
						}
							break;
						// Login user (with auth)
						case LOGIN_USER:
						{
							User u = (User) inputStream.readObject();
							user = getUser(u);
							if (user == null)
								outputStream.writeObject("");
							else
							{
								outputStream.writeObject(user.getSalt());
								User u1 = (User) inputStream.readObject();
								if (loginUser(u1))
									outputStream.writeObject(Message.OK);
								else
									outputStream.writeObject(Message.INCORRECT_PASSWORD);
							}
						}
							break;
						// Buy product list, returning availability of stock
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
						// User request
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
				if(!quit)
					e.printStackTrace();
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

        server = new ServerThread();
		server.start();
	}

	public static ServerController getInstance() throws IOException
	{
		if(instance == null)
			instance = new ServerController();
		return instance;
	}

	// Request a product, checking availability in stock and returning the correct Message
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

	public void setUsers(ObservableList<User> users) {
		this.users = users;
	}

	public void setProducts(ObservableList<Product> products) {
		this.products = products;
	}

	// [Synchronized] Add user, checking for errors
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

	// Register individual product
	public Product addProduct(String name, String price, String quantity, LocalDate expirationDate, String provider) throws Exception
	{
		Product newProduct = new Product(name, Float.parseFloat(price), Integer.parseInt(quantity), expirationDate, provider); // Instantiates a new user
		products.add(newProduct); // Adds the user to the list in memory

		return newProduct;
	}

	// Gets user by login, if exists
	protected synchronized User getUser(User user)
	{
		Optional<User> result =  users.stream().
				filter(u -> u.getLogin().compareTo(user.getLogin()) == 0).
				findAny();
		if(result.isPresent())
			return result.get();
		return null;
	}

	// [Synchronized] Login user with auth
	protected synchronized boolean loginUser(User inputUser)
	{
		Optional<User> result =  users.stream().
				filter(u -> u.getLogin().compareTo(inputUser.getLogin()) == 0).
				filter(u1 -> u1.comparePassword(inputUser)).
				findAny();
		return result.isPresent();
	}

	public ObservableList<User> getUsers()
	{
		return users;
	}

	public ObservableList<Product> getProducts()
	{
		return products;
	}

	// Get product list by provider
	public ArrayList<Product> getProducts(String provider)
	{
		ArrayList<Product> providerProducts = new ArrayList<>();
		products.stream().filter(filterProduct -> filterProduct.getProvider().compareTo(provider) == 0).forEach(p ->
		{
			Product copy = p.clone();
			p.setQuantity(0);
			providerProducts.add(copy);
		});
		return providerProducts;
	}

	public ArrayList<String> getProviders()
	{
		ArrayList<String> providers = new ArrayList<>();
		products.stream().map(Product::getProvider).distinct().forEach(providers::add);
		return providers;
	}

	// Add products to stock
	public void provide(ObservableList<Product> obsProducts)
	{
		obsProducts
				.stream()
				.filter(filteredProduct -> filteredProduct.getQuantity() > 0)
				.forEach(obsProduct -> products.stream().filter(product -> product.compareTo(obsProduct) == 0)
						.findAny()
						.ifPresent(p -> p.add(obsProduct.getQuantity())));
	}

	// On exit call
	public void destroy() throws IOException
	{
		for (ClientThread t : clients)
		{
			t.quit = true;
			t.socket.close();
		}
		server.quit = true;
		socket.close();
	}

	public void notifyUser(ArrayList<Product> newProducts)
	{
		Properties props = new Properties();
		Session.getDefaultInstance(props, null);
		final ArrayList<String> productsOfInterest = new ArrayList<>();

		String from = "smurfiru@gmail.com";
		final String username = "smurfiru@gmail.com";
		final String password = "tempmarket01";

		String host = "smtp.gmail.com";

		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", "587");

		for(Request request : requests)
		{
			for(Product p : request.products)
			{
				newProducts.stream().filter(product -> product.compareTo(p) == 0 && product.getQuantity() > p.getQuantity()).findAny().ifPresent(product1 -> {
					request.products.remove(product1);
					productsOfInterest.add(p.getName() + "\n");
				});
			}
			if(request.products.isEmpty())
				requests.remove(request);

			if(!productsOfInterest.isEmpty())
			{
				String msgBody = "Dear " + request.user.getName() + ",\n\n" +
						"We are proud to announce that your requested products have finally arrived.\n" +
						"Here is a list, in case you have forgotten:\n";

				for(String s : productsOfInterest)
				{
					msgBody += s;
				}

				try {
					String to = request.user.getEmail();

					Session session = Session.getInstance(props,
							new javax.mail.Authenticator() {
								protected PasswordAuthentication getPasswordAuthentication() {
									return new PasswordAuthentication(username, password);
								}
							});

					javax.mail.Message message = new MimeMessage(session);

					message.setFrom(new InternetAddress(from, "Online Market System(no-reply)"));

					message.setRecipients(javax.mail.Message.RecipientType.TO,
							InternetAddress.parse(to));

					message.setSubject("New Products in our Stock!");
					message.setText(msgBody);
					Transport.send(message);
				}
				catch (MessagingException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
