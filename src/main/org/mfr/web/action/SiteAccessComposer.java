package org.mfr.web.action;

import javax.servlet.RequestDispatcher;

import org.mfr.data.Site;
import org.mfr.util.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

public class SiteAccessComposer extends SelectorComposer<Component> {
	private static final long serialVersionUID = -3328559060535245407L;
	private static final Logger logger=LoggerFactory.getLogger(SiteAccessComposer.class);
	@Wire
	private Textbox password;
	@Wire
	private Label dispayMessage;
	
	private Site site;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		site=GlobalVariableResolver.getSite();
	}
	@Listen("onClick = #setPassword")
	public void setPassword(){
		String passwords=password.getValue();
		if(password!=null){
			if(site.getPassword().equals(passwords)){
				
				HttpHelper.setAccess(site);
				String url=HttpHelper.getHttpRequest().getRequestURL().toString();
				url="/";
				RequestDispatcher dispatcher=HttpHelper.getHttpRequest().getRequestDispatcher(url);
				try {
					Executions.sendRedirect(url);
				} catch (Exception e) {
					logger.error("setPassword", e);
				}
			}else{
				dispayMessage.setValue(Labels.getLabel("private.access.wrong.password"));
			}
		}
	}
}
