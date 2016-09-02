package org.mfr.web.action.portfolio;

import org.mfr.util.HttpHelper;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;

public class PortfolioHeaderComposer  extends SelectorComposer<Component> {
	private static final long serialVersionUID = 2752273445255364341L;
	private static String [] menu=new String[]{"/_pindex","/_pportfolio","/_pabout","/_pcontact"};
	private static String [] menuNames=new String[]{"mainpage","pgallery","about","contact"};
	
	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent,
			ComponentInfo compInfo) {
		Session session=parent.getDesktop().getSession();
		boolean loggedID=session.getAttribute(org.mfr.manager.UserManager.USERSESSIONNAME)!=null;
		org.mfr.data.Site site = org.mfr.web.action.GlobalVariableResolver.getSite();
		boolean aboutEnabled=site.getEnableAbout()!=null && site.getEnableAbout().equals(1);
		boolean contactEnabled=site.getEnableContact()!=null && site.getEnableContact().equals(1);
		Boolean [] access=new Boolean[]{true,true,aboutEnabled,contactEnabled};
		String path=HttpHelper.getHttpRequest().getRequestURI();
		path=path.replaceAll("/","/_");
		path=path.replaceAll(".zul","");
		
		page.setAttribute("loggedID", loggedID);
		page.setAttribute("site", site);
		page.setAttribute("menu", menu);
		page.setAttribute("menuNames", menuNames);
		page.setAttribute("access", access);
		page.setAttribute("path", path);
		return super.doBeforeCompose(page, parent, compInfo);
	}
	
	@Listen("onClick=#view")
    public void switchView() {
		HttpHelper.getUser().setRealView(!HttpHelper.getUser().isRealView());
		Executions.sendRedirect("_pindex");
    }
	public String getViewButtonText(){
		if(HttpHelper.getUser().isRealView()){
			return Labels.getLabel("switchtoeditview");
		}else{
			return Labels.getLabel("switchtorealview");
			
		}
	}
}
