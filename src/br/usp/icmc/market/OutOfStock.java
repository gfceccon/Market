package br.usp.icmc.market;

/**
 * Created by gustavo.ceccon on 30/06/2015.
 */
public class OutOfStock extends Throwable
{
	public OutOfStock()
	{
		super("Product out of stock!");
	}
}
