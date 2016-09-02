package org.mfr.util;

import java.awt.image.ImageFilter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.RequestWrapper;
import org.mfr.data.Category;
import org.mfr.data.ImageDataModel;
import org.mfr.data.Site;
import org.mfr.data.SiteDao;
import org.mfr.data.User;
import org.mfr.data.UserPreference;
import org.mfr.data.Useracc;
import org.mfr.image.NoHtmlOutZulImage;
import org.mfr.manager.PermissionDetail;
import org.mfr.manager.UserManager;
import org.mfr.mybatis.types.MUseracc;
import org.mfr.web.ImgFilter;
import org.mfr.web.action.GlobalVariableResolver;
import org.mfr.web.action.ImageHandlerComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Div;

import bsh.This;

import com.google.appengine.repackaged.com.google.common.base.Joiner;

public class HttpHelper {
	private static final Logger logger = LoggerFactory
			.getLogger(HttpHelper.class);
	
	private static final String PNG = ".PNG";
	private static final String JPG = ".JPG";
	public static final String COOCKIENAME="loginName";
	public static final String PUBLICIMAGELIST="publicImageList";
	public static final String PUBLICIMAGELISTPAGE="publicImageListPage";
	public static final String SITEACCESS="siteaccess";
	
	static Pattern comaSpaces=Pattern.compile(",");
	static Pattern comaSpaces2=Pattern.compile("[ ]{1,},");
	static Pattern manySpaces=Pattern.compile("[ ]{2,}");
	static Pattern openTag=Pattern.compile("<");
	static Pattern closeTag=Pattern.compile(">");
	
	static Pattern smile=Pattern.compile(":\\)|:-\\)");
	static Pattern leer=Pattern.compile(";\\)|;-\\)");
	static Pattern sad=Pattern.compile(":\\(|:-\\(");
	static Pattern link=Pattern.compile("(https?:\\/\\/?[\\da-z\\.-]+\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?)");
	
	private static DateFormat getDateFormat(){
		HttpSession session=getHttpSession();
		DateFormat dateFormat=null;
		if(session!=null){
			dateFormat=(DateFormat)session.getAttribute("dateFormat");
			if(dateFormat==null){
				dateFormat=new SimpleDateFormat("yyyy.MM.dd mm:ss");//.getDateTimeInstance(SimpleDateFormat.MEDIUM,SimpleDateFormat.SHORT);
				session.setAttribute("dateFormat",dateFormat);
			}
		}
		if(dateFormat==null){
			dateFormat=new SimpleDateFormat("yyyy.MM.dd mm:ss");//SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM,SimpleDateFormat.SHORT);
			logger.warn("UNSAFE DATE formatter created !!!!");
		}
		return dateFormat;
	}
	
	
	public static String getUserName(){
		org.mfr.data.User user=(org.mfr.data.User)Executions.getCurrent().getSession().getAttribute(UserManager.USERSESSIONNAME);
		return user.getUserName();		
	}
	public static String getLoginName(){
		org.mfr.data.User user=(org.mfr.data.User)Executions.getCurrent().getSession().getAttribute(UserManager.USERSESSIONNAME);
		return user.getLoginName();
	}
	public static org.mfr.data.User getUser(HttpSession session){
		org.mfr.data.User user=null;
		if(session!=null){
			user=(org.mfr.data.User)session.getAttribute(UserManager.USERSESSIONNAME);
		}

		return user;		
	}
	
	public static boolean isNormalUser(){
		User user=HttpHelper.getUser();
		if(user!=null && user.getPermission()==null){
			return true;
		}
		return false;
	}
	public static boolean hasUploadPermission(){
		User user=HttpHelper.getUser();
		if(user!=null){
			PermissionDetail permissionDetail=user.getPermission();
			if(permissionDetail!=null && permissionDetail.getPermission().getAllowUpload()==1){
				return true;
			}
		}
		return false;
	}
	
	public static org.mfr.data.User getUser(){
		org.mfr.data.User user=null;
		HttpSession session=getHttpSession();
		if(session!=null){
			user=getUser(session);
		}
		return user;		
	}
	public static MUseracc copyToMUser(Useracc user){
		MUseracc museracc=new MUseracc();
		museracc.setId(user.getId().longValue());
		museracc.setLogin(user.getLogin());
		return museracc;		
	}
	public static boolean isSiteGranted(Site site){
		if(site==null) return false;
		if(GlobalVariableResolver.isSiteOwner()){
			return true;
		}
		if(SiteDao.isSitePasswordRequired(site)){
			return isPasswordAccessGranted(site);
		}else{
			return !SiteDao.notIsPublicOrOwner(site);
		}
	}
	
	public static boolean isPasswordAccessGranted(Site site){
		if(SiteDao.isSitePasswordRequired(site)){
			HttpSession session=getHttpSession();
			Set<String> siteAcess=(Set<String>)session.getAttribute(SITEACCESS);
			if(siteAcess==null){
				return false;
			}
			String url=site.getUrl();
			return siteAcess.contains(url);
		}
		return false;
	}
	public static void setAccess(Site site){
		HttpSession session=getHttpSession();
		
		Set<String> siteAcess=(Set<String>)session.getAttribute(SITEACCESS);
		if(siteAcess==null){
			siteAcess=new HashSet<String>();
			siteAcess.add(site.getUrl());
			session.setAttribute(SITEACCESS, siteAcess);
		}
		siteAcess.add(site.getUrl());
	}
	public static HttpSession getHttpSession(){
		HttpServletRequest request=getHttpRequest();
		if(request!=null){
			return request.getSession();
		}
		return null;
	}
	public static HttpServletRequest getHttpRequest(){
		HttpServletRequest request=(HttpServletRequest)RequestWrapper.httpRequest.get();
		if(request==null){
			request=(HttpServletRequest)Executions.getCurrent().getNativeRequest();
		}
		return request;
	}
	public static HttpServletResponse getHttpResponse(){
		Execution exec=Executions.getCurrent();
		if(exec!=null){
			return (HttpServletResponse)exec.getNativeResponse();
		}
		return null;
	}
	public static void storeCookie(UserPreference up,HttpServletResponse response){
		if(response!=null){
			Cookie c=new Cookie(COOCKIENAME, up.serialize());
			c.setMaxAge(60*60*24*360);
			c.setPath("/");
			response.addCookie(c);
		}else{
			logger.warn("store cookie is not possible response is NULL");
		}
	}
	public static void storeCookie(UserPreference up){
		storeCookie(up, getHttpResponse());
	}
	
	
	public static UserPreference getUserPref(){
		if(getUser()!=null){
			return getUser().getUserPrefs();
			
		}
		return getUserPrefFromCookie();
	}
	public static UserPreference getUserPref(HttpServletRequest request){
		if(getUser(request.getSession())!=null){
			return getUser(request.getSession()).getUserPrefs();
			
		}
		return getUserPrefFromCookie(request);
	}

	
	public static UserPreference getUserPrefFromCookie(HttpServletRequest request){
		UserPreference userPref=new UserPreference();
		if(request!=null && request.getCookies()!=null){
			Cookie []c=request.getCookies();
			for (Cookie cookie : c) {
				if(cookie.getName().equals(COOCKIENAME)){
					userPref=UserPreference.create(cookie.getValue());
				}
			}
		}
		return userPref;
	}	
	public static UserPreference getUserPrefFromCookie(){
		HttpServletRequest request=getHttpRequest();
		return getUserPrefFromCookie(request);
	}
	
	
	public static String encodeParameters(Map<String, String> params) {
	    Map<String, String> escapedParams = new LinkedHashMap();
	    for (Map.Entry<String, String> entry : params.entrySet()) {
	      try {
	        escapedParams.put(URLEncoder.encode(entry.getKey(), "UTF-8"),
	                          URLEncoder.encode(entry.getValue(), "UTF-8"));
	      } catch (UnsupportedEncodingException e) {
	        // this should not happen
	        throw new RuntimeException("platform does not support UTF-8", e);
	      }
	    }
	    return Joiner.on("&").withKeyValueSeparator("=").join(escapedParams);
	}	
	public static String replacetoAsciiChars(String string){
		String result=string.replaceAll("[óõö]", "o");
		result=result.replaceAll("[ ]", "");
		result=result.replaceAll("[üûú]", "u");
		result=result.replaceAll("[í]", "i");
		result=result.replaceAll("[á]", "a");
		result=result.replaceAll("[é]", "e");
		result=result.replaceAll("[ÓÕÖ]", "O");
		result=result.replaceAll("[ÜÛÚ]", "U");
		result=result.replaceAll("[Í]", "I");
		result=result.replaceAll("[Á]", "A");
		result=result.replaceAll("[É]", "E");
		result=result.replaceAll("[\\s]", "_");
		result=result.replaceAll("[/\\\\]", "_");
		return result;
	}
	public static String getExt(String path){
		if(path!=null && path.lastIndexOf(".")>-1){
			String ext=path.substring(path.lastIndexOf("."));
			return ext;
		}
		return "";
	}
	public static boolean isFileImageExt(String path){
		return isImageExt(getExt(path));
	}
	public static boolean isImageExt(String ext){
		if(ext!=null && ext.length()>0){
			return ext.toUpperCase().endsWith(JPG) || ext.toUpperCase().endsWith(PNG);
		}
		return false;
	}
	
	
	public static class PublicImageDesc implements Serializable{
		private static final long serialVersionUID = 7134330843652322888L;
		public Integer id=0;
		public String name=null;
		public String path=null;
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
	}
	
	public static boolean isLoggedUser(Useracc user){
		if(HttpHelper.getUser()!=null && user!=null){
			return HttpHelper.getUser().getUseracc().getId().equals(user.getId());
		}
		return false;
	}
	
	public static void setImageAccessForPublic(ImageDataModel imageDataModel){
		if(imageDataModel!=null){
			setImageAccessForPublic(imageDataModel.getId(),imageDataModel.getImageName(),imageDataModel.getImageRealPath());
		}
	}

	
	public static void setZkImageOri(ImageDataModel imageDataModel,Div div){
		org.mfr.image.NoHtmlOutZulImage imageOri=new org.mfr.image.NoHtmlOutZulImage();
		div.appendChild(imageOri);
		if(logger.isTraceEnabled()){
			logger.trace("attachto:"+div.getId());
		}
		//imageOri.setContent(imageDataModel.getImageOri());
		imageOri.setContent(imageDataModel.getImageMedium());
		imageDataModel.setZkImageOri(imageOri);
		if(logger.isTraceEnabled()){
			logger.trace("attachto:"+((org.mfr.image.NoHtmlOutZulImage)imageDataModel.getZkImageOri()).getUrl());
		}
	}
	
	public static void setImageAccessForPublic(Integer id,String name,String path){
		String uri=GlobalVariableResolver.getActualUri_();
		PublicImageDesc pid=new PublicImageDesc();
		pid.id=id;
		pid.name=name;
		pid.path=path;
		String oldUri=(String)HttpHelper.getHttpSession().getAttribute(PUBLICIMAGELISTPAGE);
		if(oldUri!=null && !oldUri.equals(uri)){
			HttpHelper.getHttpSession().removeAttribute(PUBLICIMAGELIST);
		}
		HttpHelper.getHttpSession().setAttribute(PUBLICIMAGELISTPAGE, uri);
		Map<Integer,PublicImageDesc> imageMap=(Map<Integer,PublicImageDesc>)HttpHelper.getHttpSession().getAttribute(PUBLICIMAGELIST);
		if(imageMap==null){
			imageMap=new HashMap<Integer,PublicImageDesc>(200);
			HttpHelper.getHttpSession().setAttribute(PUBLICIMAGELIST, imageMap);
		}
		imageMap.put(id,pid);
	}
	public static String getIM(Integer id){
		return getIUrl(id,ImgFilter.MEDIUM);
	}
	public static String getIO(Integer id){
		return getIUrl(id,ImgFilter.ORI);
	}
	public static String getIP(Integer id){
		return getIUrl(id,ImgFilter.PREV);
	}
	public static String getIT(Integer id){
		return getIUrl(id,ImgFilter.THUMB);
	}
	public static String getIUrl(Integer id,String path){
		PublicImageDesc pid=isImageAccessForPublic(id);
		if(pid!=null){
			StringBuilder builder=new StringBuilder("/mimg/");
			builder.append(path).append("/id/").append(id).append("/");
			String n=HttpHelper.replacetoAsciiChars(pid.name);
			builder.append(n);
			if(HttpHelper.getExt(n).equals("")){
				builder.append(HttpHelper.getExt(pid.path));
			}
			return builder.toString();
		}
		return "";
	}
	public static PublicImageDesc isImageAccessForPublic(Integer id){
		Map<Integer,PublicImageDesc> imageList=(Map<Integer,PublicImageDesc>)HttpHelper.getHttpSession().getAttribute(PUBLICIMAGELIST);
		if(imageList==null) return null;
		return imageList.get(id);
	}
	public static ImageHandlerComposer getImageHandler(){
		ImageHandlerComposer ihc=(ImageHandlerComposer)HttpHelper.getHttpSession().getAttribute(ImageHandlerComposer.IMAGEHANDLERCOMPOSER);
		return ihc;
	}
	public static String getDisplayFormat(Long size){
		if(size==null){
			size=0L;
		}
		DecimalFormat myFormatter = new DecimalFormat("##,###.#");
		double s=size/1024;
		String unit="Kb";
		if(s>1024){
			s=s/1024;
			unit="Mb";
			if(s>1024){
				s=s/1024;
				unit="Gb";
			}
		}
		return myFormatter.format(s)+unit;
	}
	public static String formatDate(Date date){
		if(date!=null){
			DateFormat dateFormat=getDateFormat();
			return dateFormat.format(date);
		}else{
			return null;
		}
	}

	public static String getAlbumFullLink(String startUri,Category album){
		StringBuffer sb=new StringBuffer(HttpHelper.getRequestDomain());
		sb.append(getAlbumUrl(startUri,album.getName())).append("?albumid=").append(album.getId());
		return sb.toString();
	}
	public static String getAlbumUrl(String album){
		return getAlbumUrl(null,album);
	}
	public static String getAlbumUrl(String startUri,String album){
		if(startUri==null){
			startUri="_gallery_album_";
		}if(startUri.endsWith(".zul")){
			return startUri;
		}else{
			return startUri+replacetoAsciiChars(album);
		}
	}
	public static boolean isGoogleCrawler(){
		org.mfr.util.UAgentInfo info=org.mfr.util.UAgentInfo.getUAgentInfo();
		String agent=info.getUAgentInfo().getUserAgent();
		logger.debug("agent["+agent+"]");
		boolean v=agent!=null && agent.toUpperCase().contains("GOOGLEBOT");
		if(v){
			logger.warn("GOOGLEBOT crawler");	
		}
		return v;
	}
	public static String textBeautifier(String text){
		text=openTag.matcher(text).replaceAll("&lt;");
		text=closeTag.matcher(text).replaceAll("&gt;");
		text=comaSpaces.matcher(text).replaceAll(", ");
		text=comaSpaces2.matcher(text).replaceAll(", ");
		text=manySpaces.matcher(text).replaceAll(" ");
		return text; 
	}
	public static String textHtmlFormatter(String text){
		text=smile.matcher(text).replaceAll("<img src='img/icon2/feel/smile.png' style='vertical-align:middle;width:15xp;height:15px;'/>");
		text=sad.matcher(text).replaceAll("<img src='img/icon2/feel/sad.png' style='vertical-align:middle;width:15xp;height:15px;'/>");
		text=leer.matcher(text).replaceAll("<img src='img/icon2/feel/leer.png' style='vertical-align:middle;width:15xp;height:15px;'/>");
		text=link.matcher(text).replaceAll("<a href='$1' target='blank'>$1</a>");
		
		return text; 
	}
	public static String getRequestDomain(){
		StringBuffer sb=new StringBuffer(HttpHelper.getHttpRequest().getScheme()).append("://").append(HttpHelper.getHttpRequest().getServerName()); 
		return sb.toString();
	}
	
	public static Map<String,String> getUrlParameters(String url){
		Map<String,String> values=new HashMap<String,String>();
		String []variables=url.split("&");
		for (String v : variables) {
			String [] urlv=v.split("=");
			if(urlv.length==2){
				values.put(urlv[0], urlv[1]);
			}else{
				logger.debug("parse error :"+v);
			}
		}
		return values;
	}
	
	public static final void main(String args[]){
//		String t=textBeautifier("  dd   sss ,ssds  ,   sdsd <wer> <erwe> ").trim();
//		System.out.println(t);
		getUrlParameters("access_token=CAAJgq0LMJoEBAENG0ZAiEHFWhWzTHRDYZBDvnysGQqDHgHikPWURDV4CIuffPm1DhbNGOSkV8WXor2LYxCtdHNdEdvF9H7WN0uyQpSZAOTmP7oPKnu5fIJ1xYoRDNKLaceUJkqfCfl2HAHaz6HGDtZAwVrZBspwPDQXThTniqZBOyd4IGfe7Vo&expires=518169");
	}
	
	
	
}
