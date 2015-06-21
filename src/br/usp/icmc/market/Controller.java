package br.usp.icmc.market;

/**
 * Created by gustavo.ceccon on 19/06/2015.
 */
public class Controller
{
	private static Controller instance;

	private Controller()
	{
	}
	public static Controller getInstance()
	{
		if(instance == null)
			instance = new Controller();
		return instance;
	}
}
