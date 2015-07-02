package br.usp.icmc.market;

import java.io.Serializable;

/* Message
 * Message system server-client
 */
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
	OUT_OF_STOCK,
	OK,
	END
}
