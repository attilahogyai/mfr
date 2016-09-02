package org.mfr.web.action;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mfr.data.User;
import org.mfr.data.UserPreference;
import org.mfr.data.UseraccDao;
import org.mfr.data.Useracc;
import org.mfr.manager.UserManager;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.message.MessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Textbox;

public class LoginComposer extends GenericForwardComposer<Component>  {
	private static final long serialVersionUID = 1710170874132967585L;
	private static final Logger logger=LoggerFactory.getLogger(LoginComposer.class);
	
	
	public static final String INDEXPAGE="/index.zul";
	public static final String PINDEXPAGE="/pindex.zul";
	@Autowired
	private UseraccDao useraccDao;
	@Autowired
	private UserManager userManager;
	private Component window;
	@Wire
	private Textbox loginName;
	@Wire
	private Textbox loginPassword;
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		window=comp;
		UserPreference userPref=HttpHelper.getUserPrefFromCookie();
		if(userPref.getLoginName()!=null){
			try{
				loginName.setValue(userPref.getLoginName());
			}catch(WrongValueException e){}
		}

	}
	public void onClick$loginButton(){
		
		// http://pecalista.orbanz.hu/mailman/options/pecalist?language=hu&email=attila.hogyai%40gmail.com&password=mano.war&login=Bejelentkez%E9s
		String loginS=loginName.getValue();
    	String passwordS=loginPassword.getValue();
    	Useracc user=useraccDao.findByLoginAndPassword(loginS,passwordS);
    	if(user==null){
    		ZkUtil.messageBoxInfo("login.error");
    	}else{
    		userManager.populateUserSession(user,false);
    	}
	}
	public void onClick$googleLogin(){
		try {
			userManager.oAuthLogin("google",(HttpServletRequest)Executions.getCurrent().getNativeRequest(),(HttpServletResponse)Executions.getCurrent().getNativeResponse());
			//userManager.openIDLogin((HttpServletRequest)Executions.getCurrent().getNativeRequest(),(HttpServletResponse)Executions.getCurrent().getNativeResponse());
		} catch (Exception e) {
			logger.error("onClick$googleLogin", e);
		}
	}
	public void onClick$facebookLogin(){
		try {
			userManager.oAuthLogin("facebook",(HttpServletRequest)Executions.getCurrent().getNativeRequest(),(HttpServletResponse)Executions.getCurrent().getNativeResponse());
			//userManager.openIDLogin((HttpServletRequest)Executions.getCurrent().getNativeRequest(),(HttpServletResponse)Executions.getCurrent().getNativeResponse());
		} catch (Exception e) {
			logger.error("onClick$googleLogin", e);
		}
	}
	
	public UseraccDao getUseraccDao() {
		return useraccDao;
	}
	public void setUseraccDao(UseraccDao useraccDao) {
		this.useraccDao = useraccDao;
	}
	
}
