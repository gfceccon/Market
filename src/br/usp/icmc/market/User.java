package br.usp.icmc.market;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public abstract class User implements CSVSerializable
{
	//User information
	public String login;
	public String name;
	public String contact;
	public String email;
	public String address;
	public String cpf;

	//Controller information
	public int maxBookCount;				//
	public int maxLoanTime;
	public LocalDate banDate;


	/*
		Getters and Setters (TableView essentials)
	 */
	public String getLogin()
	{
		return login;
	}

	public void setLogin(String login)
	{
		this.login = login;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getContact()
	{
		return contact;
	}

	public void setContact(String contact)
	{
		this.contact = contact;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getCpf()
	{
		return cpf;
	}

	public void setCpf(String cpf)
	{
		this.cpf = cpf;
	}

	public int getMaxBookCount()
	{
		return maxBookCount;
	}

	public void setMaxBookCount(int maxBookCount)
	{
		this.maxBookCount = maxBookCount;
	}

	public int getMaxLoanTime()
	{
		return maxLoanTime;
	}

	public void setMaxLoanTime(int maxLoanTime)
	{
		this.maxLoanTime = maxLoanTime;
	}

	public String getBanDate()
	{
		if (banDate != null)
			return banDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		return "-";
	}

	public void setBanDate(LocalDate newBanDate)
	{
		if (newBanDate != null)
			this.banDate = newBanDate;
	}

	//Get children class name without package
	public String getType()
	{
		return this.getClass().getSimpleName();
	}

	@Override
	public void parse(String[] args) throws Exception
	{
		this.login = args[1];
		this.name = args[2];
		this.contact = args[3];
		this.email = args[4];
		this.address = args[5];
		this.cpf = args[6];
		this.maxBookCount = Integer.parseInt(args[7]);
		this.maxLoanTime = Integer.parseInt(args[8]);

		if (args[9].equals("-"))
		{
			this.setBanDate(null);
		}
		else
		{
			this.setBanDate(LocalDate.parse(args[9], DateTimeFormatter.ofPattern("dd'/'MM'/'yyyy")));
		}
	}

	public String[] toCSV(String[] ret) throws Exception
	{
		ret[1] = this.login;
		ret[2] = this.name;
		ret[3] = this.contact;
		ret[4] = this.email;
		ret[5] = this.address;
		ret[6] = this.cpf;
		ret[7] = Integer.toString(this.maxBookCount);
		ret[8] = Integer.toString(this.maxLoanTime);

		if (this.banDate == null)
		{
			ret[9] = "-";
		}
		else
		{
			ret[9] = banDate.format(DateTimeFormatter.ofPattern("dd'/'MM'/'yyyy"));
		}

		return ret;
	}
}
