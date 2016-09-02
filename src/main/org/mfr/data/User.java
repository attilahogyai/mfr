package org.mfr.data;

import java.io.Serializable;
import java.util.List;

import org.mfr.manager.PermissionDetail;

public class User implements Serializable{
	private Useracc useracc;
	private UserPreference userPrefs;
	private String sessionId;
	private Integer provider;
	private List<Site> site;
	private boolean admin;
	
	
	private String googleAccessToken=null;
	private String googleRefreshToken=null;
	private String facebookAccessToken=null;
	private boolean realView=false;
	private long usedSpace=0;
	
	private PermissionDetail permission;
	
	private boolean hasPortfolio;
	
	public String getUserName() {
		if(useracc==null) return null;
		return useracc.getName();
	}
	public String getLoginName() {
		if(useracc==null) return null;
		return useracc.getLogin();
	}
	public String getEmail() {
		if(useracc==null) return null;
		return useracc.getEmail();
	}
	public Useracc getUseracc() {
		return useracc;
	}
	public void setUseracc(Useracc useracc) {
		this.useracc = useracc;
	}
	public Integer getUserId() {
		if(useracc==null) return null;
		return useracc.getId();
	}
	public String toString(){
		StringBuffer sb=new StringBuffer("loginname[").append(getLoginName()).append("]").
				append(" email[").append(getEmail()).append("]").append("username[").append(getUserName()).append("]")
				.append("userid[").append(getUserId()).append("]");
		if(useracc!=null){
			sb.append(" useraccid["+useracc.getId()+"]");
		}
		return sb.toString();
		
	}
	public UserPreference getUserPrefs() {
		return userPrefs;
	}
	public void setUserPrefs(UserPreference userPrefs) {
		this.userPrefs = userPrefs;
	}
	public String getGoogleAccessToken() {
		return googleAccessToken;
	}
	public void setGoogleAccessToken(String googleAccessToken) {
		this.googleAccessToken = googleAccessToken;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public Integer getProvider() {
		return provider;
	}
	public void setProvider(Integer provider) {
		this.provider = provider;
	}
	public String getGoogleRefreshToken() {
		return googleRefreshToken;
	}
	public void setGoogleRefreshToken(String googleRefreshToken) {
		this.googleRefreshToken = googleRefreshToken;
	}
	public List<Site> getSite() {
		return site;
	}
	public void setSite(List<Site> site) {
		this.site = site;
	}
	
	public boolean getHasPortfolio(){
		return site!=null && site.size()>0;
	}
	public boolean isRealView() {
		return realView;
	}
	public void setRealView(boolean realView) {
		this.realView = realView;
	}
	public boolean isAdmin() {
		if(useracc==null) return false;
		return useracc.getEmail().equals("attila.hogyai@gmail.com") || useracc.getEmail().equals("hatika@gmail.com");
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	public long getUsedSpace() {
		return usedSpace;
	}
	public void setUsedSpace(long usedSpace) {
		this.usedSpace = usedSpace;
	}
	public PermissionDetail getPermission() {
		return permission;
	}
	public void setPermission(PermissionDetail permission) {
		this.permission = permission;
	}
	public String getFacebookAccessToken() {
		return facebookAccessToken;
	}
	public void setFacebookAccessToken(String facebookAccessToken) {
		this.facebookAccessToken = facebookAccessToken;
	}
	
}
