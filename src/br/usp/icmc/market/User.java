package br.usp.icmc.market;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/* User
 * Represents a user
 * Product is  serializable (42 by default all serializable classes), CSV serializable
 */
public class User implements Serializable, CSVSerializable
{
	static final long serialVersionUID = 42L;

	//User information
	private String name;
	private String address;
	private String phone;
	private String email;
	private String login;
	private String password;
	private String salt;

	public String getSalt()
	{
		return salt;
	}

	// Set salt and re-crypto password
    public void setSalt(String salt) {
        this.salt = salt;

        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(salt.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest(this.password.getBytes());
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
			for (byte aByte : bytes)
			{
				sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
			}
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        this.password = generatedPassword;
    }

    public User(){}

	// Create user and randomize salt
	public User(String name, String address, String phone, String email, String login, String password)
	{
		this.name = name;
		this.address = address;
		this.phone = phone;
		this.email = email;
		this.login = login;
		this.password = password;

        //Always use a SecureRandom generator
        SecureRandom sr;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
            //Create array for salt
            byte[] saltByte = new byte[16];
            //Get a random salt
            sr.nextBytes(saltByte);
            //return salt
            setSalt(saltByte.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public void setLogin(String login)
	{
		this.login = login;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getName()
	{
		return name;
	}

	public String getAddress()
	{
		return address;
	}

	public String getPhone()
	{
		return phone;
	}

	public String getEmail()
	{
		return email;
	}

	public String getLogin()
	{
		return login;
	}

	public boolean comparePassword(User user)
	{
		return password.compareTo(user.password) == 0;
	}

	@Override
	public int getNumberOfArguments() {
		return 7;
	}

	@Override
	public void parse(String[] args) throws Exception {
		if (args.length != getNumberOfArguments())
			throw new IllegalArgumentException("Wrong number of arguments!");

		this.name = args[0];
		this.address = args[1];
		this.phone = args[2];
		this.email = args[3];
		this.login = args[4];
		this.password = args[5];
		this.salt = args[6];
	}

	@Override
	public String[] toCSV() throws Exception {
		String[] ret = new String[getNumberOfArguments()];

		ret[0] = this.name;
		ret[1] = this.address;
		ret[2] = this.phone;
		ret[3] = this.email;
		ret[4] = this.login;
		ret[5] = this.password;
		ret[6] = this.salt;

		return ret;
	}
}
