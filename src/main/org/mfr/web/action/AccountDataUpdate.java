package org.mfr.web.action;

import org.mfr.data.Useracc;
import org.mfr.data.UseraccDao;
import org.mfr.data.UseraccPrefs;
import org.mfr.data.UseraccPrefsDao;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Textbox;

public class AccountDataUpdate  extends AbstractSelectorComposer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5907111948793836192L;
	private static final Logger logger=LoggerFactory.getLogger(AccountDataUpdate.class);
	@WireVariable
	private UseraccDao useraccDao;
	@WireVariable
	private UseraccPrefsDao useraccPrefsDao;
	@Wire
	private Textbox userName;
	@Wire
	private Textbox emailAddress;	
	@Wire
	private Textbox signLoginPassword;
	@Wire
	private Textbox signLoginPassword2;
	@Wire
	private Checkbox newsletter;
	@Wire
	private Checkbox disableEmail;
	

	
	@Listen("onClick = #updatePasswordButton")
	public void onUpdatePassword(){
		if(!signLoginPassword.isValid() || !signLoginPassword2.isValid()){
			logger.debug("form is not valid");
			ZkUtil.messageBoxWarning("password.check.error");
			return;
		}
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
			Useracc user=HttpHelper.getUser().getUseracc();
			user.setPassword(signLoginPassword.getValue());
			try{
				useraccDao.encodePassword(user);
				useraccDao.merge(user);
				ZkUtil.messageBoxInfo("update.password.success");
			}catch(Exception e){
				Messagebox.show(Labels.getLabel("signup.error",new Object[]{e.getMessage()}));
			}
		}catch(Exception e){
			logger.error("onClick$updatePasswordButton",e);
		}
	}
	
	@Listen("onClick = #updateButton")
	public void onUpdate(){
		if(!checkMandatory(userName,emailAddress)){
			return;
		}
		
		if(!userName.isValid() 
				|| !emailAddress.isValid()){
			logger.debug("form is not valid");
			ZkUtil.messageBoxWarning("invalid.form");
			return;
		}
		try{			
			Useracc user=HttpHelper.getUser().getUseracc();
			user.setEmail(emailAddress.getValue());
			user.setName(userName.getValue());
			user.setNewsletter(newsletter.isChecked()?1:0);
			UseraccPrefs prefs=user.getUseraccPrefs();
			if(prefs==null){
				prefs=new UseraccPrefs();
				prefs.setDisableEmail(disableEmail.isChecked()?1:0);
				user.setUseraccPrefs(prefs);
				prefs.setUseracc(user);
				useraccPrefsDao.persist(prefs);
			}else{
				prefs.setDisableEmail(disableEmail.isChecked()?1:0);
			}
			try{
				useraccDao.merge(user);
				useraccPrefsDao.merge(prefs);
				ZkUtil.messageBoxInfo("update.data.success");
			}catch(Exception e){
				Messagebox.show(Labels.getLabel("update.error",new Object[]{e.getMessage()}));
			}
		}catch(Exception e){
			logger.error("onClick$updateButton",e);
		}
	}
	
	
	public UseraccDao getUseraccDao() {
		return useraccDao;
	}
	public void setUseraccDao(UseraccDao useraccDao) {
		this.useraccDao = useraccDao;
	}

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		Useracc user=HttpHelper.getUser().getUseracc();
		userName.setValue(user.getName());
		emailAddress.setValue(user.getEmail());
		boolean nl=user.getNewsletter()!=null && user.getNewsletter().equals(new Integer(1))?true:false;
		newsletter.setChecked(nl);
		disableEmail.setChecked(user.isDisableEmailOwner());
		
	}	
}
