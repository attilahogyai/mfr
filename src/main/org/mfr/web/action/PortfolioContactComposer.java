package org.mfr.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mfr.data.SiteDao;
import org.mfr.data.User;
import org.mfr.util.HttpHelper;
import org.mfr.util.MailHelper;
import org.mfr.util.VelocityHelper;
import org.mfr.util.ZkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkforge.ckez.CKeditor;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;

public class PortfolioContactComposer extends SelectorComposer {
	private static final Logger logger=LoggerFactory.getLogger(PortfolioContactComposer.class);
	final org.mfr.data.Site site=org.mfr.web.action.GlobalVariableResolver.getSite();
	@Wire
	private Listbox subject;
	@Wire
	private Textbox contactEmail;
	@Autowired
	private SiteDao siteDao;
	@Wire
	private Textbox message;
	
	@Wire
	private CKeditor contacttext;
	
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
	}
	@Listen("onClick = #sendmessage")
	public void sendMessage(){
		if(subject.getSelectedItem().getLabel()==null || subject.getSelectedItem().getLabel().length()==0 ||
				message.getValue()==null || message.getValue().length()==0){
			ZkUtil.messageBoxWarning("fill.in");
			return;
		}
		
		VelocityHelper.init(((HttpServletRequest)Executions.getCurrent().getNativeRequest()).getServletContext());
		
		final Map arguments=new HashMap();
		User user=HttpHelper.getUser();
		arguments.put("subject", subject.getSelectedItem().getLabel());
		arguments.put("message", message.getValue());
		arguments.put("user", user);
		arguments.put("contactEmail", contactEmail.getValue());
		
		String adminEmail=site.getAdminEmail();
		if(adminEmail==null){
			adminEmail=site.getOwner().getEmail();
		}
		final String subjectString=subject.getSelectedItem().getLabel();
		final List reg=new ArrayList();
		reg.add(adminEmail);
		
		Thread thread=new Thread(){
			public void run(){
				try {
					MailHelper.sendMail(reg,"email/portfolio_contact.vm",contactEmail.getValue()+" <"+site.getName()+".myfotoroom.com - Kapcsolat>",subjectString,true,"text/html",arguments);
				} catch (Exception e) {
					logger.error("sendMail",e);
				}
			}
		};
		thread.start();
		ZkUtil.messageBoxWarning("portfolio.message.sent");
	}
	
	public void saveContactText(){
		String language=GlobalVariableResolver.getLanguage();
		if(language.toUpperCase().equals("HU")){
			site.setContact(contacttext.getValue());
		}else{
			site.setContactEn(contacttext.getValue());
		}
		siteDao.merge(site);
		ZkUtil.messageBoxInfo("text.saved");
	}

}
