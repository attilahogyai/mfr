package org.mfr.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.RequestWrapper;
import org.mfr.data.Site;
import org.mfr.data.SiteDao;
import org.mfr.data.User;
import org.mfr.util.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SiteFilter implements Filter{
	private WebApplicationContext springContext;
	private SiteDao siteDao;
	public static final String PORTFOLIOSITE="PORTFOLIO_SITE";
	private static final Logger logger=LoggerFactory.getLogger(SiteFilter.class);
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest servletR=((HttpServletRequest)request);
		RequestWrapper.httpRequest.set(servletR);
		try{
			String requestUri=servletR.getRequestURI();
			Site site=(Site)request.getAttribute(PORTFOLIOSITE);
			if(site==null && (requestUri.endsWith(".zul") || requestUri.endsWith("/") || requestUri.endsWith("/zkau") || requestUri.startsWith("/dc/"))){
				String serverName=request.getServerName();
				String portfolioName=null;
				if(!serverName.startsWith("myfotoroom.com") &&
						!serverName.startsWith("31.24.16.57") &&						
						!serverName.startsWith("dszg0234.dorsum.intra") &&
						!serverName.startsWith("localmfr") &&
						!requestUri.endsWith("/error500.zul")){
					if(serverName.indexOf('.')>-1){
						portfolioName=serverName.substring(0, serverName.indexOf('.'));
						site=siteDao.getSiteForUrl(portfolioName);
						if(site==null){
							logger.info("site ["+portfolioName+"] not found");
							if(request.getAttribute("requesterror")==null){
								request.setAttribute("requesterror", true);
								request.getRequestDispatcher("/error500.zul").forward(request, response);
							}
							return;
						}
						request.setAttribute(PORTFOLIOSITE, site);
		
					}
				}
			}
			if(site!=null){
				logger.info("site ["+site.getUrl()+"]");
			}
			filterChain.doFilter(request, response);
		}finally {
			RequestWrapper.httpRequest.set(null);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		
		springContext = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
		siteDao=(SiteDao)springContext.getBean("siteDao");
		
	}

}
