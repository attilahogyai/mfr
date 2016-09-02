package org.mfr.web.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mfr.data.Css;
import org.mfr.data.CssDao;
import org.mfr.util.HttpHelper;
import org.zkoss.spring.SpringUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Window;

public class BasePageInitiator extends SelectorComposer<Component> {

	public static Map<Integer,String> getCssUrls(CssDao cssDao,String target,String page,List<Css> userCssList){
		List<Css> cssList=cssDao.findDefaultCssForTargetAndPage(target,page);
		
		Map<Integer,String> urlList=new HashMap<Integer,String>();
		for (Css css : cssList) {
			urlList.put(css.getGroup(), css.getUrl());
		}
		for (Css css : userCssList) {
			urlList.put(css.getGroup(), css.getUrl());
		}
		return urlList;
	}
	protected void showLogin(){
		Map arg=new HashMap();
		arg.put("backLink",Executions.getCurrent().getDesktop().getAttribute("zkoss.spring.DESKTOP_URL"));
		Executions.getCurrent().getDesktop().setAttribute("backLink", Executions.getCurrent().getDesktop().getAttribute("zkoss.spring.DESKTOP_URL"));
		Component comp=Executions.createComponents("/comp/login_popup.zul", null, arg);
		((Window)comp).doModal();
	}
	
}
