package br.usp.icmc.market;

/* OutOfStock
 * Out of stock exception
 */
public class OutOfStock extends Throwable
{
	public OutOfStock()
	{
		super("Product out of stock!");
	}
}
