package org.mfr.manager.oauth;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mfr.data.User;

public interface IOAuthService {
	public Integer getProviderId();
	public String getProviderName();
	
	public void oAuthLoginRequest(HttpServletRequest request,HttpServletResponse response);
	public String[] requestForRefreshAndAccessToken(String authToken) throws Exception;
	public String requestForAccessToken(String refreshToken) throws Exception;
	public String requestEmail(String accessToken) throws Exception;
	public Map<String,String> requestUserData(String accessToken) throws Exception;
	public void openIDLoginRequest(HttpServletRequest request,HttpServletResponse response) ;
	public void verifyOpenIDLogin(HttpServletRequest request);
}
