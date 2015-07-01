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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


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
						case GET_PRODUCTS:
						{
							for(Product p : products)
							{
								outputStream.writeObject(p);
							}
							outputStream.writeObject(Message.END);
						}
							break;
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

	public Product addProduct(String name, String price, String quantity, LocalDate expirationDate, String provider) throws Exception
	{
		Product newProduct = new Product(name, Float.parseFloat(price), Integer.parseInt(quantity), expirationDate, provider); // Instantiates a new user
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

	protected synchronized boolean loginUser(User inputUser)
	{
		Optional<User> result =  users.stream().
				filter(u -> u.getLogin().compareTo(inputUser.getLogin()) == 0).
				filter(u1 -> u1.comparePassword(inputUser)).
				findAny();
		if(result.isPresent())
			return true;
		return false;
	}

	public ObservableList<User> getUsers()
	{
		return users;
	}

	public ObservableList<Product> getProducts()
	{
		return products;
	}

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

	public void provide(ObservableList<Product> obsProducts)
	{
		obsProducts
				.stream()
				.filter(filteredProduct -> filteredProduct.getQuantity() > 0)
				.forEach(obsProduct -> products.stream().filter(product -> product.compareTo(obsProduct) == 0)
						.findAny()
						.ifPresent(p -> p.add(obsProduct.getQuantity())));
	}

	public void notifyUser(ArrayList<Product> newProducts)
	{
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		ArrayList<String> productsOfInterest;

		// Sender's email ID needs to be mentioned
		String from = "smurfiru@gmail.com";//change accordingly
		final String username = "smurfiru@gmail.com";//change accordingly
		final String password = "tempmarket01";//change accordingly

		// Assuming you are sending email through relay.jangosmtp.net
		String host = "smtp.gmail.com";

		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", "587");

		for(Request req : requests)
		{
			productsOfInterest = new ArrayList<>();
			for(Product p : req.products)
			{
				if(newProducts.contains(p))
				{
					productsOfInterest.add(p.getName() + "\n");
				}
			}

			if(!productsOfInterest.isEmpty())
			{
				String msgBody = "Dear " + req.user.getName() + ",\n\n" +
						"We are proud to announce that your requested products have finally arrived.\n" +
						"Here is a list, in case you have forgotten:\n";

				for(String s : productsOfInterest)
				{
					msgBody += s;
				}

				try {
					// Recipient's email ID needs to be mentioned.
					String to = req.user.getEmail();

					// Get the Session object.
					session = Session.getInstance(props,
							new javax.mail.Authenticator() {
								protected PasswordAuthentication getPasswordAuthentication() {
									return new PasswordAuthentication(username, password);
								}
							});

					// Create a default MimeMessage object.
					javax.mail.Message message = new MimeMessage(session);

					// Set From: header field of the header.
					message.setFrom(new InternetAddress(from));

					// Set To: header field of the header.
					message.setRecipients(javax.mail.Message.RecipientType.TO,
							InternetAddress.parse(to));

					// Set Subject: header field
					message.setSubject("New Products in our Stock!");

					// Now set the actual message
					message.setText(msgBody);

					// Send message
					Transport.send(message);

					System.out.println("Sent message successfully....");
				} catch (AddressException e) {
					// ...
				} catch (MessagingException e) {
					// ...
				}
			}
		}
	}
}
