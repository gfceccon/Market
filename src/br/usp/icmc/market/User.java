package br.usp.icmc.market;

import java.io.Serializable;

public class User implements Serializable {
    static final long serialVersionUID = 42L;

    //User information
    private String name;
    private String address;
    private String phone;
    private String email;
    private String login;
    private String password;
    private String salt;

    public User(String name, String address, String phone, String email, String login, String password) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.login = login;
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public boolean comparePassword(User user) {
        return password.compareTo(this.password) == 0;
    }
}