package org.mfr.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.RequestWrapper;
import org.mfr.data.PermissionDao;
import org.mfr.data.Site;
import org.mfr.data.SiteDao;
import org.mfr.data.User;
import org.mfr.data.Useracc;
import org.mfr.manager.ImageDataManager;
import org.mfr.manager.PermissionDetail;
import org.mfr.manager.UserManager;
import org.mfr.util.HttpHelper;
import org.mfr.web.action.GalleryComposer;
import org.mfr.web.action.GlobalVariableResolver;
import org.mfr.web.action.LoginComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.appengine.repackaged.com.google.common.base.Log;

public class PrivateContentAccessManager implements Filter {
	
	
	private static final Logger logger=LoggerFactory.getLogger(PrivateContentAccessManager.class);
	private static long count;
	private WebApplicationContext springContext;
	public static List<String> allowedPage;
	public static List<String> portfolioAllowedPage;
	private SiteDao siteDao;
	private UserManager userManager;
	private PermissionDao permissionDao;
	
	
	static{
		String [] pages={"/publicalbums.zul","/license.zul","/signup.zul",
				"/activate.zul","/index.zul","/gallery.zul","/blog.zul",
				"/contact.zul","/openid.zul","/oauthcallback.zul","/foauthcallback.zul",
				"/error500.zul","/error.zul","/adatvedelem.zul", "/embed.zul",
				"/faq.zul","/portfolio_faq.zul","/searchresult.zul","/portfoliolist.zul"};
		allowedPage=Arrays.asList(pages);
		String [] portfoliopages={"/pindex.zul","/pportfolio.zul","/pblog.zul","/pgallery.zul","/pabout.zul","/pcontact.zul","/error500.zul","/privateaccess.zul"};
		portfolioAllowedPage=Arrays.asList(portfoliopages);
		
	}
	 
	@Override
	public void destroy() {

	}

	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest servletR=((HttpServletRequest)request);
		RequestWrapper.httpRequest.set(servletR);
		try{
			String requestUri=servletR.getRequestURI();
			Site site=(Site)request.getAttribute(SiteFilter.PORTFOLIOSITE);;
			
			if(requestUri.length()==1){
				if(!SiteDao.isSitePasswordRequired(site) && !HttpHelper.isSiteGranted(site)){
					request.getRequestDispatcher("/index.zul").forward(request, response);
					return;
				}else{				
					if(SiteDao.isSitePasswordRequired(site) && !HttpHelper.isSiteGranted(site)){
						request.getRequestDispatcher("/privateaccess.zul").forward(request, response);
					}else{
						request.getRequestDispatcher("/pindex.zul").forward(request, response);
					}
					return;
				}
				
			}
			int lastIndex=requestUri.lastIndexOf("/");
			
			requestUri=requestUri.substring(lastIndex);
			logger.debug("pageActUri ["+requestUri+"]");
			request.setAttribute(GlobalVariableResolver.PAGEACTURI, requestUri);
			User user=HttpHelper.getUser(((HttpServletRequest)request).getSession());
			String threadName=Thread.currentThread().getName();
			try{
				
				Thread.currentThread().setName(requestUri+"-"+((HttpServletRequest)request).getSession().getId()+"-"+count++);
				
				List<String> actualAllowedPages=null;
				String redirectUrl=null;
				boolean hasPermission=hasPermission(requestUri);
				if(!SiteDao.isSitePasswordRequired(site) && SiteDao.notIsPublicOrOwner(site)){
					actualAllowedPages=allowedPage;
					redirectUrl=LoginComposer.INDEXPAGE;
				}else{
					if(GlobalVariableResolver.isSiteOwner()){
						redirectUrl=LoginComposer.PINDEXPAGE;
					}else if(SiteDao.isSitePasswordRequired(site) && !HttpHelper.isPasswordAccessGranted(site)){
						redirectUrl="/privateaccess.zul";
					}else{
						redirectUrl=LoginComposer.PINDEXPAGE;
					}
					actualAllowedPages=portfolioAllowedPage;
				}
				if(!hasPermission &&
				  (!actualAllowedPages.contains(requestUri) && user==null) || 
				  (site!=null && !redirectUrl.equals(requestUri) && (!actualAllowedPages.contains(requestUri) || !HttpHelper.isSiteGranted(site))) ){
					request.getRequestDispatcher(redirectUrl).forward(request, response);
				}else{
					filterChain.doFilter(request, response);
				}
			}catch(ServletException e){
				logger.error("doFilter",e);
				throw e;
			}finally{
				Thread.currentThread().setName(threadName);
			}
		}catch(Exception e){
			logger.error("filter",e);
		}finally{
			RequestWrapper.httpRequest.set(null);
		}
		
	}
	@Override
	public void init(FilterConfig config) throws ServletException {
		springContext = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
		siteDao=(SiteDao)springContext.getBean("siteDao");
		userManager=(UserManager)springContext.getBean("userManager");
		permissionDao=(PermissionDao)springContext.getBean("permissionDao");
	}
	
	public boolean hasPermission(String requestUri){
		PermissionDetail permissionDetail=(PermissionDetail)HttpHelper.getHttpSession().getAttribute(GalleryComposer.PERMISSION_DETAIL);
		if(requestUri.equals("/imagehandler.zul") && permissionDetail!=null){
			boolean allowUpload=permissionDetail.getPermission().getAllowUpload()!=null && permissionDetail.getPermission().getAllowUpload().equals(1); 
			if(permissionDetail!=null && allowUpload){
				// check and create user
				Useracc user=userManager.getUserAccForLogin(permissionDetail.getPermission().getTicket());
				if(user==null){
					user=new Useracc();
					user.setLogin(permissionDetail.getPermission().getTicket());
					user.setEmail(permissionDetail.getPermission().getSentTo());
					user.setName("guest");
					permissionDetail.getPermission().setAssignedUseracc(user);
					userManager.newTechUserInit(user);
					permissionDao.merge(permissionDetail.getPermission());
				}
				// login user
				userManager.populateUserSession(user,true);
				HttpHelper.getHttpSession().removeAttribute(GalleryComposer.PERMISSION_DETAIL);
				HttpHelper.getUser().setPermission(permissionDetail);
				return true;
			}	
		}
		return false;
	}
	
}
