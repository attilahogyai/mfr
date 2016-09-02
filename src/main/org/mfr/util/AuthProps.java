package org.mfr.util;

import java.io.Serializable;

public class AuthProps implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4037919332504319818L;

	private boolean authenticaton_flag = false;
	private String username = "";
	private String password = "";
	
	public AuthProps(boolean authentication_flag, String username, String password) {
		
		this.authenticaton_flag = authentication_flag;
		this.username = username;
		this.password = password;
		
	}
	
	public boolean isAuthenticaton_flag() {
		return authenticaton_flag;
	}
	public void setAuthenticaton_flag(boolean authenticatonFlag) {
		authenticaton_flag = authenticatonFlag;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
