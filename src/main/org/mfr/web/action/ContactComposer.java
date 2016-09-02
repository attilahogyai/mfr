package org.mfr.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;

import org.mfr.data.User;
import org.mfr.util.HttpHelper;
import org.mfr.util.MailHelper;
import org.mfr.util.VelocityHelper;
import org.mfr.util.ZkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;

public class ContactComposer  extends SelectorComposer  {
	private static final Logger logger=LoggerFactory.getLogger(ContactComposer.class);
	@Wire
	private Listbox subject;
	@Wire
	private Textbox contactEmail;
	@Wire
	private Textbox message;
	@Listen("onClick = #sendmessage")
	public void sendMessage(){
		if(subject.getSelectedItem().getLabel()==null || subject.getSelectedItem().getLabel().length()==0 ||
				message.getValue()==null || message.getValue().length()==0){
			ZkUtil.messageBoxWarning("fill.in");
			return;
		}
		
		VelocityHelper.init(((HttpServletRequest)Executions.getCurrent().getNativeRequest()).getServletContext());
		final List reg=new ArrayList();
		reg.add("info@myfotoroom.com");
		
		final Map arguments=new HashMap();
		User user=HttpHelper.getUser();
		arguments.put("subject", subject.getSelectedItem().getLabel());
		arguments.put("message", message.getValue());
		arguments.put("user", user);
		arguments.put("contactEmail", contactEmail.getValue());
		
		final String subjectString=subject.getSelectedItem().getLabel();
		Thread thread=new Thread(){
			public void run(){
				try {
					MailHelper.sendMail(reg,"email/contact.vm","info@myfotoroom.com",subjectString,true,"text/html",arguments);
				} catch (Exception e) {
					logger.error("sendMail",e);
				}
			}
		};
		thread.start();
		ZkUtil.messageBoxWarning("message.sent");
	}

}
