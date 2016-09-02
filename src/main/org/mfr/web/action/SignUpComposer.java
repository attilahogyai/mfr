package org.mfr.web.action;

import javax.persistence.PersistenceException;

import org.mfr.data.Constants;
import org.mfr.data.Useracc;
import org.mfr.data.UseraccDao;
import org.mfr.manager.UserManager;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Textbox;

public class SignUpComposer extends GenericForwardComposer  {
	private static final Logger logger=LoggerFactory.getLogger(SignUpComposer.class);
	@Autowired
	private UseraccDao useraccDao;
	@Autowired
	private UserManager userManager;
	
	private Textbox loginName;
	private Textbox userName;
	private Textbox signLoginPassword;
	private Textbox signLoginPassword2;
	private Checkbox newsletter;
	public void onClick$signUpButton(){
		if(!loginName.isValid() || !userName.isValid() 
				|| !signLoginPassword.isValid() || !signLoginPassword2.isValid()){
			logger.debug("form is not valid");
			ZkUtil.messageBoxWarning("invalid.form");
			return;
		}
		logger.debug("start onClick$signUpButton()");
		try{
			try{
				if(signLoginPassword2.getValue()==null || signLoginPassword.getValue()==null || !signLoginPassword.getValue().equals(signLoginPassword2.getValue())){
					ZkUtil.messageBoxWarning("password.check.error");
					return;
				}
			}catch(WrongValueException e){
				// handled on client side
				return;
			}
			Useracc user=new Useracc();
			user.setEmail(loginName.getValue());
			user.setLogin(loginName.getValue());
			user.setName(userName.getValue());
			user.setPassword(signLoginPassword.getValue());
			user.setNewsletter(newsletter.isChecked()?1:0);
			user.setStatus(0);

			user.setPrivateCode(new Long(ZkUtil.getRandomLong(1000000)).toString());
			
			userManager.newUserInit(user);
			
			Executions.sendRedirect("activate.zul");
		}catch(PersistenceException e){
			
		}catch(Exception e){
			logger.error("onClick$signUpButton",e);
		}
	}
	

	public UseraccDao getUseraccDao() {
		return useraccDao;
	}
	public void setUseraccDao(UseraccDao useraccDao) {
		this.useraccDao = useraccDao;
	}
}
