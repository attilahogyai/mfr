package org.mfr.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mfr.data.PhotoDao;
import org.mfr.data.Site;
import org.mfr.data.SiteDao;
import org.mfr.data.User;
import org.mfr.manager.GoogleDriveManager;
import org.mfr.manager.UserManager;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.mfr.web.PrivateContentAccessManager;
import org.mfr.web.SiteFilter;
import org.zkoss.spring.DelegatingVariableResolver;
import org.zkoss.xel.XelException;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class GlobalVariableResolver extends DelegatingVariableResolver {
	public static final String LANG_HU="HU";
	public static final String LANG_EN="EN";
	public static final String LANG_RO="RO";
	
	private static final String IMG_ICON2_MOBILE = "img/icon2/mobile/";
	private static final String IMG_ICON2 = "img/icon2/";
	final static String USER = "user";
	final static String LOGGEDIN = "loggedIn";
	final static String LANGUAGE = "language";
	final static String URI = "uri";
	final static String SITE = "site";
	final static String SITEARRAY = "siteArray";
	public final static String PAGEACTURI = "pageActUri";
	public final static String _PAGEACTURI = "_pageActUri";
	public final static String LANG_ = "lang_";
	final static String ISGOOGLEUSER = "isGoogleUser";
	final static String ISPHONE = "isPhone";
	final static String ISTABLET = "isTablet";
	final static String ISDESKTOP = "isDesktop";
	final static String DOMAIN = "domain";
	final static String SITEOWNER = "siteowner";
	final static String SITEDESC = "siteDesc";
	final static String ICONURL = "iconurl";
	final static String ISEDITABLE = "isEditable";
	
	final static String POPUPWIDTH = "popupwidth";
	final static String PLATFCSS = "platfcss";
	
	final static String HTTPHELPER = "httpHelper";
	final static String HP = "hp";
	final static String HASPORTFOLIO = "hasPortfolio";
	
	public final static String mainImageUrl="/img/main.JPG";
	
	
	final static HttpHelper httpHelper=new HttpHelper();
	
	@Override
	public Object resolveVariable(String varName) throws XelException {
		Session session = Sessions.getCurrent();
		if (varName.equals(USER)) {
			return HttpHelper.getUser();
		} else if (varName.equals(LOGGEDIN)) {
			return HttpHelper.isNormalUser();
		} else if (varName.equals(LANGUAGE)) {
			return getLanguage();
		} else if (varName.equals(URI)) {
			String url = ((HttpServletRequest) Executions.getCurrent()
					.getNativeRequest()).getRequestURI();
			return url;
		} else if (varName.equals(PAGEACTURI)) {
			return getActualUri();
		} else if (varName.equals(_PAGEACTURI)) {
			return getActualUri_();
		} else if (varName.equals(LANG_)) {
			return getLang_();
		} else if (varName.equals(ISGOOGLEUSER)) {
			return false;
			/**
			User user = HttpHelper.getUser();
			if(!user.isAdmin()){
				return false;
			}
			return user.getProvider().equals(GoogleDriveManager.PROVIDERID);
			**/
		} else if (varName.equals(SITE)) {
			return getSite();
		} else if (varName.equals(SITEARRAY)) {
			return new String[] { getSite().getName() };
		} else if (varName.equals(ISPHONE)) {
			return isPhone();
		} else if (varName.equals(ISTABLET)) {
			return isTablet();
		} else if(varName.equals(ISDESKTOP)){
			return isDesktop();
		} else if(varName.equals(PLATFCSS)){
			if(isDesktop()){
				return "";
			}else {
				return "-mob";
			}
		} else if (varName.equals(DOMAIN)) {
			return getDomain();
		} else if(varName.equals(POPUPWIDTH)){
			if(isDesktop()){
				return "750";
			}else if(isTablet()){
				return "520";
			}else {
				return "320";
			}
		} else if (varName.equals(ICONURL)) {
			if (isPhone() || isTablet()) {
				return IMG_ICON2_MOBILE;
			} else {
				return IMG_ICON2;
			}
		} else if (varName.equals(SITEOWNER)) {
			return isSiteOwner();
		} else if (varName.equals(SITEDESC)) {
			Site site = getSite();
			String language = getLanguage();
			if (language.equals("HU")) {
				return site.getDescription();
			} else {
				return site.getDescriptionEn();
			}
		} else if (varName.equals(ISEDITABLE)){
			return isEditable();
		}else if (varName.equals(HTTPHELPER) || varName.equals(HP)){
			return httpHelper;
		}else{
			return super.resolveVariable(varName);
		}
	}

	public static String getLang_() {
		return getLanguage().toLowerCase();
	}

	public static String getActualUri() {
		String uri = (String) ((HttpServletRequest) Executions.getCurrent()
				.getNativeRequest()).getAttribute(PAGEACTURI);
		return uri;
	}
	public static String getActualUri_() {
		String uri = (String) ((HttpServletRequest) Executions.getCurrent()
				.getNativeRequest()).getAttribute(_PAGEACTURI);
		return uri;
	}

	
	public static boolean isSiteOwner(){
		Site site = getSite();
		if (site == null)
			return false;
		User user = HttpHelper.getUser();
		if (user == null)
			return false;
		return user.getUserId().equals(site.getOwner().getId());
	}
	public static boolean isEditable(){
		User user = HttpHelper.getUser();
		if(user==null){
			return false;
		}
		return !user.isRealView() && isSiteOwner();
	}
	
	public static final Site getSite() {
		if(HttpHelper.getHttpRequest()!=null){
			return (Site) HttpHelper.getHttpRequest().getAttribute(SiteFilter.PORTFOLIOSITE);
		}
		return null;
	}

	public static final String getDomain() {
		return System.getProperty("domain", "myfotoroom.com");
	}

	public static final String getLanguage() {
		HttpSession session = HttpHelper.getHttpSession();
		return ZkUtil.getCurrentLanguage(session);
	}

	public static final boolean isPhone() {
		org.mfr.util.UAgentInfo info = org.mfr.util.UAgentInfo.getUAgentInfo();
		boolean isPhone = info.detectMobileLong() || info.detectTierTablet();
		return isPhone;
//		return true;
	}
	public static final boolean isSmallPhone() {
		org.mfr.util.UAgentInfo info = org.mfr.util.UAgentInfo.getUAgentInfo();
		boolean isPhone = info.detectMobileLong();
		return isPhone;
	}
	public static final boolean isTablet() {
		org.mfr.util.UAgentInfo info = org.mfr.util.UAgentInfo
				.getUAgentInfo();
		boolean isTablet = (info.detectTierTablet() || info.detectIpad());
		return isTablet;
	}
	public static final boolean isDesktop(){
		return !isPhone() && !isTablet();
		//return false;
	}
	public static boolean isHungarian(){
		return "HU".equals(getLanguage());
	}
}
