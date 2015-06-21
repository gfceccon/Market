package br.usp.icmc.market;

/*
	Interface of User/Book/Loan to CSV Manager
	Parse Object to CSV string and vice versa
	An object has an specific number of arguments/variables
 */
public interface CSVSerializable
{
	//Parse string arguments to this object
	void parse(String[] args) throws Exception;

	//Return CSV format string
	String[] toCSV() throws Exception;

	//Get number of arguments or variables
	int getNumberOfArguments();
}
