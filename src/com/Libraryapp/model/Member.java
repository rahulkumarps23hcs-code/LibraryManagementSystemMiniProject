package com.Libraryapp.model;

public class Member extends User {
	
	public Member(){
		super(0,null,null,null,null,null, "MEMBER");
	}
	
	public Member(int userId, String name, String email, String mobile, 
			String address, String password) {
		super(userId,name,email,mobile,address,password, "MEMBER");
	}

	@Override
	public void showWelcome() {
		System.out.println("\nWelcome Member : "+ getName());
	}

	
}
