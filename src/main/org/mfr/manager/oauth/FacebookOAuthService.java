package org.mfr.manager.oauth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.mfr.util.HttpHelper;
import org.mfr.web.action.GlobalVariableResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.zkoss.json.parser.JSONParser;
import org.zkoss.zk.ui.Executions;

import com.sun.org.omg.SendingContext.CodeBasePackage.URLHelper;

import sun.net.util.URLUtil;

public class FacebookOAuthService implements IOAuthService {
	private static final Logger logger = LoggerFactory
			.getLogger(FacebookOAuthService.class);
	private RestTemplate restTemplate=new RestTemplate();
	private static final String OAuthAuthURL="https://www.facebook.com/dialog/oauth";
	private static final String oAuthRedirectURL = "http://myfotoroom.com/foauthcallback.zul";
	private static final String clientID = "669238629770881";
	private static final String clientSecret = "1703424682d9885b628228b4ca70d30f";
	
	String OAuthTokenURL = "https://graph.facebook.com/oauth/access_token";
	
	private static List<Integer>tokens=new ArrayList<Integer>();
	private int lastCode=0;
	
	
	@Override
	public void oAuthLoginRequest(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String,String> parameters=new HashMap<String,String>();
		parameters.put("response_type", "code");
		parameters.put("client_id", clientID);
		parameters.put("redirect_uri", oAuthRedirectURL);
		parameters.put("scope", "email");
		int c=lastCode++;
		tokens.add(c);
		parameters.put("state", Integer.toString(c));
		
		String paramter=HttpHelper.encodeParameters(parameters);
		String forwardUrl=OAuthAuthURL+"?"+paramter;
		Executions.sendRedirect(forwardUrl);

	}

	@Override
	public String[] requestForRefreshAndAccessToken(String authToken)
			throws Exception {
		/*
		GET https://graph.facebook.com/oauth/access_token?
		    client_id={app-id}
		   &redirect_uri={redirect-uri}
		   &client_secret={app-secret}
		   &code={code-parameter}
		*/
		HttpHeaders headers=new HttpHeaders();  
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		Map<String,String> parameters=new LinkedHashMap<String,String>();
		parameters.put("client_secret", clientSecret);
		parameters.put("redirect_uri", oAuthRedirectURL);
		parameters.put("client_id", clientID);
		parameters.put("code", authToken);
		
		
		String paramString=HttpHelper.encodeParameters(parameters);

		if(logger.isDebugEnabled())logger.debug("HttpEntity ["+paramString+"] "+headers);
		
		HttpEntity<String> entity = new HttpEntity<String>(paramString,headers);
		
		String response=restTemplate.postForObject(OAuthTokenURL, entity,String.class);
		logger.debug("response["+response+"]");
		
		
		Map<String,String> rootAsMap = HttpHelper.getUrlParameters(response);
		String accessToken=(String)rootAsMap.get("access_token");
		if(accessToken==null){
			logger.debug("access not found in reponse ["+rootAsMap+"]");
		}
		return new String[]{null,accessToken};
	}
	
	@Override
	public String requestForAccessToken(String refreshToken) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String requestEmail(String accessToken) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> requestUserData(String accessToken)
			throws Exception {
		HttpGet get=new HttpGet("https://graph.facebook.com/me?access_token="+URLEncoder.encode(accessToken));
		DefaultHttpClient client=new DefaultHttpClient ();
		HttpResponse response;
		StringBuffer sb=new StringBuffer();
		try {
			response = client.execute(get);
			String line=null;
			BufferedReader reader=new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			while((line=reader.readLine())!=null){
				sb.append(line);
			}
		} catch (Exception e) {
			logger.error("requestUserData", e);
			throw e;
		}
		logger.debug("requestUserData result:"+sb.toString());
		JSONParser jp=new JSONParser(); 
		Map data=(Map)jp.parse(sb.toString());
		return data;

	}

	@Override
	public void openIDLoginRequest(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verifyOpenIDLogin(HttpServletRequest request) {
		// TODO Auto-generated method stub
		
	}
	public Integer getProviderId(){
		return 2;
	}
	public String getProviderName(){
		return "facebook";
	}
}
