package br.usp.icmc.market;

import java.util.ArrayList;

public class Request
{
	public ArrayList<Product> products;
	public User user;

	public Request(User user)
	{
		products = new ArrayList<>();
		this.user = user;
	}

}
