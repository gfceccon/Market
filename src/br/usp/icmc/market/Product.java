package br.usp.icmc.market;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/* Product
 * Represents a product
 * Product is comparable by id, serializable (42 by default all serializable classes), CSV serializable and cloneable
 */
public class Product implements Comparable<Product>, Serializable, CSVSerializable, Cloneable
{
	static final long serialVersionUID = 42L;

	private UUID id;
	private String name;
	private float price;
	private int quantity;
	private LocalDate expirationDate;
	private String provider;

	public Product(){}

	public Product(String name, float price, int quantity, LocalDate expirationDate, String provider)
	{
		id = UUID.randomUUID();
		this.name = name;
		this.price = price;
		this.expirationDate = expirationDate;
		this.provider = provider;
		this.quantity = quantity;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public float getPrice()
	{
		return price;
	}

	public void setPrice(float price)
	{
		this.price = price;
	}

	public String getExpirationString()
	{
		return expirationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	public void setExpirationString(String date) throws DateTimeParseException
	{
		expirationDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	public LocalDate getExpirationDate()
	{
		return expirationDate;
	}

	public void setExpirationDate(LocalDate expirationDate)
	{
		this.expirationDate = expirationDate;
	}

	public String getProvider()
	{
		return provider;
	}

	public void setProvider(String provider)
	{
		this.provider = provider;
	}

	public UUID getId()
	{
		return id;
	}

	@Override
	public int compareTo(Product p)
	{
		return this.getId().compareTo(p.getId());
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	public void get(int quantity) throws IllegalArgumentException
	{
		if(quantity > this.quantity)
			throw new IllegalArgumentException();
		this.quantity -= quantity;
	}

	@Override
	public int getNumberOfArguments() {
		return 6;
	}

	@Override
	public void parse(String[] args) throws Exception {
		if (args.length != getNumberOfArguments())
			throw new IllegalArgumentException("Wrong number of arguments!");

		this.id = UUID.fromString(args[0]);
		this.name = args[1];
		this.price = Float.parseFloat(args[2]);
		this.quantity = Integer.parseInt(args[3]);
		this.expirationDate = LocalDate.parse(args[4], DateTimeFormatter.ofPattern("dd'/'MM'/'yyyy"));
		this.provider = args[5];
	}

	@Override
	public String[] toCSV() throws Exception {
		String[] ret = new String[getNumberOfArguments()];

		ret[0] = this.id.toString();
		ret[1] = this.name;
		ret[2] = Float.toString(this.price);
		ret[3] = Integer.toString(this.quantity);
		ret[4] = this.expirationDate.format(DateTimeFormatter.ofPattern("dd'/'MM'/'yyyy"));
		ret[5] = this.provider;

		return ret;
	}

	// Deep clone
	public Product clone()
	{
		Product object = null;
		try
		{
			object = (Product) super.clone();
			object.id = UUID.fromString(this.id.toString());
			object.expirationDate = LocalDate.of(expirationDate.getYear(),
					expirationDate.getMonth(),
					expirationDate.getDayOfMonth());
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return object;
	}

	// Add quantity
	public void add(int quantity)
	{
		if(quantity > 0)
			setQuantity(this.quantity + quantity);
	}
}
