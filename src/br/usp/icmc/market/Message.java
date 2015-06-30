package br.usp.icmc.market;

import java.io.Serializable;

public enum Message implements Serializable
{
	REGISTER_USER,
	USER_ALREADY_EXISTS,
	USER_CREATED,
	INCORRECT_PASSWORD,
	LOGIN_USER,
	BUY_PRODUCTS,
	RECEIVE_NOTIFICATION,
	OUT_OF_STOCK,
	OK,
	END
}
