package br.usp.icmc.market;

import java.util.ArrayList;

/* Product
 * Represents a user request for products out of stock
 * This class is not csv serializable and is stored on main memory
 * This class is server-sided
 */
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
