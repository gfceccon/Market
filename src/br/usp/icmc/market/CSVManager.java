package br.usp.icmc.market;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;

/*
	CSV manager
	Parse csv files to users, books and loans
	Write user, book or loan list to file
 */
public class CSVManager
{
	/*
		PARSE FILE BLOCK
		Parse a file to an ObservableList
	 */
	public ObservableList<User> parseUserFile(File userFile)
	{
		ObservableList<User> l = FXCollections.observableArrayList();

		BufferedReader reader;
		String csvLine;

		try
		{
			reader = new BufferedReader(new FileReader(userFile));
			while ((csvLine = reader.readLine()) != null)
			{
				l.add(this.parseUser(csvLine));
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return l;
	}

	public ObservableList<Book> parseBookFile(File bookFile)
	{
		ObservableList<Book> l = FXCollections.observableArrayList();
		BufferedReader reader;
		String csvLine;

		try
		{
			reader = new BufferedReader(new FileReader(bookFile));
			while ((csvLine = reader.readLine()) != null)
			{
				l.add(this.parseBook(csvLine));
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return l;
	}

	public ObservableList<Loan> parseLoanFile(File loanFile)
	{
		ObservableList<Loan> l = FXCollections.observableArrayList();
		BufferedReader reader;
		String csvLine;

		try
		{
			reader = new BufferedReader(new FileReader(loanFile));
			while ((csvLine = reader.readLine()) != null)
			{
				l.add(this.parseLoan(csvLine));
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return l;
	}
	/*
		END PARSE FILE BLOCK
	 */

	/*
		Write a serializable list to a file
	 */
	public void writeFile(File fp, ObservableList<? extends CSVSerializable> l)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(fp));
			for (CSVSerializable item : l)
			{
				try
				{
					String[] arguments = item.toCSV();
					writer.write(String.join(",", arguments));
					writer.newLine();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			writer.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/*
		SINGLE PARSE BLOCK
		Parse a single line to a serializable entity
	 */
	private User parseUser(String csv)
	{
		String[] arguments = csv.split(",");

		try
		{
			switch (arguments[0])
			{
				case "Student":
				{
					Student ret = new Student();
					ret.parse(arguments);
					return ret;
				}
				case "Teacher":
				{
					Teacher ret = new Teacher();
					ret.parse(arguments);
					return ret;
				}
				case "Community":
				{
					Community ret = new Community();
					ret.parse(arguments);
					return ret;
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private Book parseBook(String csv)
	{
		String[] arguments = csv.split(",");

		try
		{
			switch (arguments[0])
			{
				case "Text":
				{
					Text ret = new Text();
					ret.parse(arguments);
					return ret;
				}
				case "General":
				{
					General ret = new General();
					ret.parse(arguments);
					return ret;
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private Loan parseLoan(String csv)
	{
		String[] arguments = csv.split(",");

		try
		{
			Loan ret = new Loan();
			ret.parse(arguments);
			return ret;
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/*
		END SINGLE PARSE BLOCK
	 */
}
