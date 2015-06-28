package br.usp.icmc.market;

import java.io.Serializable;

public enum Message implements Serializable
{
	REGISTER_USER,
	LOGIN_USER,
	BUY_PRODUCTS,
	RECEIVE_NOTIFICATION,
	END
}
