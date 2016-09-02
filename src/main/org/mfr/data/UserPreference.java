package org.mfr.data;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

import org.mfr.web.action.LoginComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPreference implements Serializable{
	private static final Logger logger=LoggerFactory.getLogger(UserPreference.class);
	private String loginName=null;
	private String language=null;
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String serialize(){
		StringBuffer sb=new StringBuffer();
		if(loginName!=null){
			sb.append("loginName").append("=").append(loginName).append("|");
		}
		if(language!=null){
			sb.append("language").append("=").append(language).append("|");
		}
		return sb.toString();
	}
	public static UserPreference create(String compString){
		UserPreference userPref=new UserPreference();
		StringTokenizer st=new StringTokenizer(compString, "|");
		while(st.hasMoreTokens()){
			String token=st.nextToken();
			if(token!=null && token.length()>1){
				String [] array=token.split("=");
				try {
					Method m=userPref.getClass().getMethod("set"+array[0].substring(0, 1).toUpperCase().concat(array[0].substring(1)), String.class);
					m.invoke(userPref, array[1]);
				} catch (Exception e) {
					logger.error("UserPreference create",e);
				}
			}
		}
		return userPref;
	}
}
