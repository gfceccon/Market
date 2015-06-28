package br.usp.icmc.market;

import javafx.collections.ObservableList;


public class ClientController
{
	private static ClientController instance;
	private ObservableList<Product> products;

	public static ClientController getInstance()
	{
		return instance;
	}

	public ObservableList<Product> getProducts()
	{
		return products;
	}
}
