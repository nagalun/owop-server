package me.andreww7985.owopserver.network;

public class LoginInfo {
	public enum Rank {
		NOBODY, NORMAL, ADMIN 
	}
	
	public final String name;
	private Rank rank;
	
	public LoginInfo(final String name) {
		this.name = name;
	}
	
	public Rank getRank() {
		return rank;
	}
	
	public void logout() {
		
	}
	
	public String toString() {
		return "'" + name + "'";
	}
}
