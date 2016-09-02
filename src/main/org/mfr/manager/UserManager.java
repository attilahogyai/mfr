package org.mfr.manager;


import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.exception.ConstraintViolationException;
import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.Constants;
import org.mfr.data.Site;
import org.mfr.data.SiteDao;
import org.mfr.data.User;
import org.mfr.data.UserPreference;
import org.mfr.data.Useracc;
import org.mfr.data.UseraccDao;
import org.mfr.data.UseraccData;
import org.mfr.data.UseraccDataDao;
import org.mfr.manager.oauth.IOAuthService;
import org.mfr.util.HttpHelper;
import org.mfr.util.MailHelper;
import org.mfr.util.ObjectHelper;
import org.mfr.util.ZkUtil;
import org.mfr.web.action.GlobalVariableResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Executions;

import com.google.appengine.repackaged.com.google.common.base.Log;


 

public class UserManager {
	private static final Logger logger = LoggerFactory
			.getLogger(UserManager.class);
	public static final String USERSESSIONNAME="USERSESSION";
	public static final String DEFAULTLOGINPAGE="_index";
	
	public static final String GOOGLE="GOOGLE";
	public static final String FACEBOOK="FACEBOOK";
	
	@Autowired
	private Map<String,IOAuthService> oAuthServiceMap=null;

	
	@Autowired
	private UseraccDao userAccDao;
	@Autowired
	private UseraccDataDao userAccDataDao;
	@Autowired
	private CategoryDao categoryDao;
	
	@Autowired
	private SiteDao siteDao;
	
	
	public UseraccDao getUserAccDao() {
		return userAccDao;
	}

	public void setUserAccDao(UseraccDao userAccDao) {
		this.userAccDao = userAccDao;
	}

	public boolean sendNewPassword(String loginname) {
		Useracc user = userAccDao.getUserAccForLogin(loginname);
		if (user != null) {
			String password = ZkUtil.getAlphaNumeric(8);
			user.setPassword(password);
			userAccDao.encodePassword(user);
			userAccDao.persist(user);
			final List reg = new ArrayList();
			reg.add(user.getEmail());
			final Map arguments = new HashMap();
			arguments.put("user", user);
			arguments.put("password", password);
			try {
				MailHelper.sendMail(reg, "email/passwordrem.vm",
						"info@myfotoroom.com",
						Labels.getLabel("password.remainder.email.subject"),
						true, "text/html", arguments);
				return true;
			} catch (Exception e) {
				ZkUtil.showProcessError(e);
			}
		}
		return false;
	}
	
	public void populateUserSession(Useracc user,boolean externalLogin){
		//Session session=Sessions.getCurrent();
		HttpSession session=HttpHelper.getHttpSession();
		UserPreference up=HttpHelper.getUserPrefFromCookie();
		up.setLoginName(user.getLogin());
		User a=new User();
		a.setUserPrefs(up);
		a.setUseracc(user);
		a.setProvider(user.getProvider());
		a.setSessionId(session.getId());
		UseraccData googleAccessToken=userAccDataDao.findUseraccData(a.getLoginName(), UseraccDataDao.GOOGLE_ACCESS_TOKEN);
		if(googleAccessToken!=null){
			a.setGoogleAccessToken(googleAccessToken.getValue());
		}
		UseraccData facebookAccessToken=userAccDataDao.findUseraccData(a.getLoginName(), UseraccDataDao.FACEBOOK_ACCESS_TOKEN);
		if(facebookAccessToken!=null){
			a.setFacebookAccessToken(facebookAccessToken.getValue());
		}
		user.setLastLogin(new Date());

		List<Site> sites=siteDao.getSitesForUseracc(user.getId());
		a.setSite(sites);
		userAccDao.merge(user);
		if(!externalLogin){
			HttpHelper.storeCookie(up);
		}
		session.setAttribute(USERSESSIONNAME,a );
		if(Executions.getCurrent()!=null){
			String backLink=(String)Executions.getCurrent().getDesktop().getAttribute("backLink");
			if(backLink!=null){
				Executions.sendRedirect(backLink);
			}else{
				Executions.sendRedirect(DEFAULTLOGINPAGE);
			}
		}
	}
	
	public void oAuthLogin(String provider,HttpServletRequest request,HttpServletResponse response) {
		if(oAuthServiceMap.get(provider)!=null){
			oAuthServiceMap.get(provider).oAuthLoginRequest(request, response);
		}else{
			logger.error("provider ["+provider+"] not found");
		}
	}
	public boolean checkFacebookOAuth(String authCode){
		return checkOAuth("facebook",authCode);
	}
	public boolean checkGoogleOAuth(String authCode){
		return checkOAuth("google",authCode);
	}
	private boolean checkOAuth(String provider,String authCode){
        if(authCode!=null){
        	String accessToken=null;
        	String refreshToken=null;
        	try{
        		String []tokens=this.oAuthServiceMap.get(provider).requestForRefreshAndAccessToken(authCode);
        		refreshToken=tokens[0];
        		accessToken=tokens[1];
        	}catch(Exception e){
        		logger.debug("get access Token error try refresh token",e);
        		// refresh
        	}
        	try {
        		if(accessToken!=null){
        			Map userData=this.oAuthServiceMap.get(provider).requestUserData(accessToken);
        			String email=(String)userData.get("email");
        			String gender=(String)userData.get("gender");
        			String name=(String)userData.get("name");
					Useracc user=userAccDao.getUserAccForLogin(email);
					boolean firstRegistration=false;
					if(user!=null){
						logger.debug("external user exists in DB");
						
					}else{
						logger.debug("user not found in database create record for user");
						user=new Useracc();
						user.setEmail(email);
						user.setName(name);
						user.setProvider(this.oAuthServiceMap.get(provider).getProviderId());
						firstRegistration=true;
					}
					
					// save session // make login
					if(gender!=null){
						user.setGender(gender.equals("mail")?1:2);
					}
					user.setLogin(email);
					if(firstRegistration){
						newUserInit(user);
					}
					updateUseraccData(user,this.oAuthServiceMap.get(provider).getProviderName()+UseraccDataDao.ACCESS_TOKEN_SUFIX,accessToken);
					if(refreshToken!=null){
						updateUseraccData(user,this.oAuthServiceMap.get(provider).getProviderName()+UseraccDataDao.REFRESH_TOKEN_SUFIX,refreshToken);
					}
					populateUserSession(user,true);
					User sessionUser=HttpHelper.getUser();
//					sessionUser.setGoogleAccessToken(accessToken);
//					sessionUser.setGoogleRefreshToken(refreshToken);
					Executions.sendRedirect(DEFAULTLOGINPAGE);
					return true;
        		}
			} catch (Exception e) {
				logger.error("checkAuth",e);
			}
        }
       	return false;
	}

	public void updateUseraccData(String userLogin,String key,String data){
		Useracc user=userAccDao.getUserAccForLogin(userLogin);
		if(user!=null){
			updateUseraccData(user,key,data);
		}
	}
	public void updateUseraccData(Useracc user,String key,String data){
		boolean dataExists=false;
		for (UseraccData userdata : user.getUseraccDatas()) {
			if(userdata.getKey().equals(key)){
				userdata.setValue(data);
				userAccDataDao.persist(userdata);
				dataExists=true;
				break;
			}
		}
		if(!dataExists){
			UseraccData ud=new UseraccData();
			ud.setKey(key);
			ud.setValue(data);
			ud.setUseracc(user);
			userAccDataDao.persist(ud);
		}
		
	}
	public void newTechUserInit(Useracc user){
		try{
			user.setLanguage(GlobalVariableResolver.getLanguage());
			user.setstorageLimit(Constants.STORAGELIMIT);
			userAccDao.persist(user);
		}catch(Exception e){
			if(e.getCause() !=null && e.getCause() instanceof ConstraintViolationException){
				if(e.getCause().getCause() instanceof BatchUpdateException){
					SQLException sqlExc=((BatchUpdateException)e.getCause().getCause()).getNextException();
					if(sqlExc!=null && sqlExc.getMessage()!=null && sqlExc.getMessage().indexOf("uq_loginname")>-1){
						Messagebox.show(Labels.getLabel("signup.loginname.error"));
						return;
					}
				}
			}
			ZkUtil.logProcessError(e);
		}
	}
	public void newUserInit(Useracc user) {
		final List<String> reg1=new ArrayList<String>();
		reg1.add(user.getEmail());
		final List<String> reg2=new ArrayList<String>();
		reg2.add("attila.hogyai@gmail.com");
		
		final Map arguments=new HashMap();
		arguments.put("user", user);
		arguments.put("code", user.getPrivateCode());
		try{
			if(user.getPassword()!=null){ // just for myfotoroom users
				userAccDao.encodePassword(user);
			}
			user.setLanguage(GlobalVariableResolver.getLanguage());
			user.setstorageLimit(Constants.STORAGELIMIT);
			
			userAccDao.persist(user);
			Category defaultCat=new Category();		
			defaultCat.setOwner(user.getLogin());
			defaultCat.setUseracc(user);
			defaultCat.setName(ImageDataManager.DEFAULT_CATEGORY_NAME);
			
			categoryDao.persist(defaultCat);
			try{
				MailHelper.sendMail(reg2,MailHelper.REGISTRATION_EMAIL,"info@myfotoroom.com","Új felhasználó regisztrált",true,"text/html",arguments);
				if(user.getPassword()!=null){ // just for myfotoroom users
					MailHelper.sendMail(reg1,MailHelper.REGISTRATION_EMAIL,"info@myfotoroom.com",Labels.getLabel("signup.email.subject"),true,"text/html",arguments);
				}
			}catch(Exception e){
				logger.error("send mail error:",e);
			}
		}catch(Exception e){
			if(ObjectHelper.getRootException(e).getMessage().indexOf("uq_loginname")>-1){
				ZkUtil.messageBoxError("signup.loginname.error");
				return;
			}
			ZkUtil.logProcessError(e);
		}
	}
	public Useracc getUserAccForLogin(String loginName){
		return userAccDao.getUserAccForLogin(loginName);
	}
	public CategoryDao getCategoryDao() {
		return categoryDao;
	}
	public void setCategoryDao(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}


	public Map<String, IOAuthService> getoAuthServiceMap() {
		return oAuthServiceMap;
	}

	public void setoAuthServiceMap(Map<String, IOAuthService> oAuthServiceMap) {
		this.oAuthServiceMap = oAuthServiceMap;
	}
}
