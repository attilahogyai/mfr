package org.mfr.web;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.mfr.data.Site;
import org.mfr.data.SiteDao;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.mfr.web.action.GlobalVariableResolver;
import org.mfr.web.action.portfolio.PortfolioVariableResolver;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.sys.PageCtrl;
import org.zkoss.zk.ui.util.Initiator;

public class PageInitiator implements Initiator {
	private static final String version="v=2.3";
	private static String [] pageids={"login","header","bottom"};
	static{
		Arrays.sort(pageids);
	}
	
	@Override
	public void doInit(Page page, Map<String, Object> args) throws Exception {
		if(((PageCtrl)page).getBeforeHeadTags()!=null && ((PageCtrl)page).getBeforeHeadTags().indexOf("noarchive")==-1){
			String laguage=ZkUtil.getCurrentLanguage((HttpSession)Sessions.getCurrent().getNativeSession());
			((PageCtrl)page).addBeforeHeadTags("<meta name=\"googlebot\" content=\"noarchive\"/>");
			((PageCtrl)page).addBeforeHeadTags("<meta name=\"robots\" content=\"noarchive,noodp\"/>");
			((PageCtrl)page).addBeforeHeadTags("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\">");
			((PageCtrl)page).addBeforeHeadTags("<META NAME=\"Distribution\" CONTENT=\"Global\"/>");
			((PageCtrl)page).addBeforeHeadTags("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />");
			((PageCtrl)page).addBeforeHeadTags("<meta name=\"apple-mobile-web-app-capable\" content=\"yes\" />");
			((PageCtrl)page).addBeforeHeadTags("<meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\" />");
			// <!-- 57 x 57 Nokia icon -->
			((PageCtrl)page).addBeforeHeadTags("<link rel=\"shortcut icon\" href=\"img/launcher/icon57.png\" />");
			// <!-- 114 x 114 iPhone 4 icon -->
			((PageCtrl)page).addBeforeHeadTags("<link rel=\"apple-touch-icon\" href=\"img/launcher/icon114.png\" />");
			
			
			Site site=GlobalVariableResolver.getSite();
			
			if(site!=null && HttpHelper.isSiteGranted(site)){
				if(((PageCtrl)page).getAfterHeadTags().indexOf("mfr.")==-1){
					((PageCtrl)page).addAfterHeadTags("<link rel=\"stylesheet\" type=\"text/css\" href=\"/dc/css/mfr.css?"+version+"\"/>");
				}
			}else{
				if(((PageCtrl)page).getAfterHeadTags().indexOf("pst")==-1){
					((PageCtrl)page).addAfterHeadTags("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/pst27.css?"+version+"\"/>");
				}
			}
			
			org.mfr.util.UAgentInfo info=org.mfr.util.UAgentInfo.getUAgentInfo();
			if(info.detectMobileQuick()){ // because of chrome hide link bar error
				((PageCtrl)page).addBeforeHeadTags("<script type=\"text/javascript\" src=\"js/mfr-script.js?"+version+"\"></script>");
			}
			boolean testenv=Boolean.parseBoolean(System.getProperty("testenv","false"));
			if(!testenv){
				((PageCtrl)page).addBeforeHeadTags("<script type=\"text/javascript\">"+
				  "var _gaq = _gaq || [];"+
				  "_gaq.push(['_setAccount', 'UA-30128241-1']);"+
				  "_gaq.push(['_setDomainName','myfotoroom.com']);"+
				  "_gaq.push(['_trackPageview']);"+
				  
				  "(function() {"+
				  "  var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;"+
				  "  ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';"+
				  "  var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);"+
				  "})();"+
				  "</script>");
			}
			((PageCtrl)page).addBeforeHeadTags("<link rel=\"shortcut icon\" href=\"/favicon.ico?"+version+"\" type=\"image/x-icon\" />");
		}
	}

}
