package com.Libraryapp.model;

public class Admin extends User {

    public Admin() {
        super(0, null, null, null, null, null, "ADMIN");
    }

    public Admin(int userId, String name, String email, String mobile,
                 String address, String password) {
        super(userId, name, email, mobile, address, password, "ADMIN");
    }

    @Override
    public void showWelcome() {
        System.out.println("\nWelcome Library Admin: " + getName());
    }
}
