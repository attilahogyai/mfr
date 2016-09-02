package org.mfr.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mfr.data.User;
import org.mfr.data.UserPreference;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguageFilter implements Filter {
	private static final Logger logger=LoggerFactory.getLogger(LanguageFilter.class);
	public static final String LANGUAGE="l";
	public void destroy() {

	}
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		logger.debug("start doFilter");
		HttpServletRequest servletR=((HttpServletRequest)request);
		String uri=servletR.getRequestURI();
		String language=servletR.getParameter(LANGUAGE);
		if(language==null){
			language=(String)servletR.getAttribute(LANGUAGE);
		}
		if(uri.startsWith("/en/") || uri.startsWith("/hu/") || uri.startsWith("/ro/")){
			language=uri.substring(1,2);
		}		
		if(language==null && HttpHelper.getUser()==null){
			UserPreference up=HttpHelper.getUserPrefFromCookie((HttpServletRequest)request);
			if(up.getLanguage()!=null){
				ZkUtil.setLanguage(up.getLanguage(), ((HttpServletRequest)request).getSession());
			}
		}else if(language!=null){
			ZkUtil.setLanguage(language, ((HttpServletRequest)request).getSession());
			UserPreference up=HttpHelper.getUserPref(((HttpServletRequest)request));
			up.setLanguage(language);
			HttpHelper.storeCookie(up,(HttpServletResponse)response);
		}

		try {
			chain.doFilter(request, response);
		} catch (Exception e) {
			logger.error("unhandled exception catched by ThreadNameFilter", e);
			ZkUtil.logProcessError(e);
			if(request.getAttribute("requesterror")==null){
				request.setAttribute("requesterror", true);
				request.getRequestDispatcher("/error500.zul").forward(request, response);
			}
		}
		logger.debug("end doFilter");
	}

	public void init(FilterConfig arg0) throws ServletException {

	}
}