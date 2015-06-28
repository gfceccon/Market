package br.usp.icmc.market;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by gustavo.ceccon on 19/06/2015.
 */
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

		public ClientThread(Socket s) throws IOException
		{
			socket = s;
			inputStream = new ObjectInputStream(s.getInputStream());
			outputStream = new ObjectOutputStream(s.getOutputStream());

		}
		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					Product product = (Product)inputStream.readObject();
					outputStream.writeBoolean(requestProduct(product));
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

	protected synchronized boolean requestProduct(Product request)
	{
		try
		{
			products.stream().filter(filterProduct -> filterProduct.compareTo(request) == 0).findAny().ifPresent(product -> product.get(request.getQuantity()));
		}catch(IllegalArgumentException e)
		{
			return false;
		}
		return true;
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
