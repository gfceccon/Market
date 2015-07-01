package br.usp.icmc.market;

import java.io.Serializable;

public enum Message implements Serializable
{
	REGISTER_USER,
	USER_ALREADY_EXISTS,
	USER_CREATED,
	INCORRECT_PASSWORD,
	LOGIN_USER,
	GET_PRODUCTS,
	BUY_PRODUCTS,
	RECEIVE_NOTIFICATION,
	NOTIFICATION,
	OUT_OF_STOCK,
	OK,
	END
}
