package org.mfr.web;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.tools.view.VelocityView;
import org.apache.velocity.tools.view.VelocityViewServlet;
import org.mfr.data.Css;
import org.mfr.data.CssDao;
import org.mfr.data.Site;
import org.mfr.data.SiteCssDao;
import org.mfr.util.ZkUtil;
import org.mfr.web.action.BasePageInitiator;
import org.mfr.web.action.css.CssSelectorComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.velocity.VelocityEngineFactory;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.spring.SpringUtil;

import sun.net.www.MimeTable;


public class DynaServlet extends VelocityViewServlet {
	private static final Logger logger=LoggerFactory.getLogger("velocity");
	private static final long serialVersionUID = 3157888868435339186L;
	public static final String CSSLIST="cssList";
	
	
	@Override
	protected void setContentType(HttpServletRequest request,
			HttpServletResponse response) {
		String uri=request.getRequestURI();
		if(uri.endsWith(".css")){
			response.setContentType("text/css");
		}else{
			super.setContentType(request, response);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("doGet called");
		super.doGet(request, response);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		logger.info("init called");
		super.init(config);
	}

	@Override
	protected Context createContext(HttpServletRequest request,
			HttpServletResponse response) {
		logger.debug("createContext called");
		return super.createContext(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("doPost called");
		super.doPost(request, response);
	}

	@Override
	protected void doRequest(HttpServletRequest arg0, HttpServletResponse arg1)
			throws IOException {
		logger.debug("doRequest called");
		super.doRequest(arg0, arg1);
	}

	@Override
	protected void error(HttpServletRequest arg0, HttpServletResponse arg1,
			Throwable arg2) {
		logger.debug("error called");
		super.error(arg0, arg1, arg2);
	}

	@Override
	protected void fillContext(Context context, HttpServletRequest request) {
		logger.debug("fillContext called");
		
		Map<Integer,String> cssList=(Map<Integer,String>)request.getSession().getAttribute(CSSLIST);
		Site site=(Site)request.getAttribute(SiteFilter.PORTFOLIOSITE);
		if(site!=null){
			ApplicationContext scontext=(ApplicationContext)WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
			CssDao cssDao=(CssDao)scontext.getBean("cssDao");
			SiteCssDao siteCssDao=(SiteCssDao)scontext.getBean("siteCssDao");
			List<Css> siteCssList=siteCssDao.findCssBySite(site.getId());
			Map<Integer,String> cssList2=BasePageInitiator.getCssUrls(cssDao,"style_"+site.getStyle(),"site",siteCssList);
			if(cssList==null){
				cssList=new HashMap<Integer,String>();
			}
			cssList.putAll(cssList2);
		}
		context.put("cssList", cssList);
		super.fillContext(context, request);
	}

	@Override
	protected String findInitParameter(ServletConfig arg0, String arg1) {
		logger.debug("findInitParameter called with "+arg1);
		return super.findInitParameter(arg0, arg1);
	}

	@Override
	protected Log getLog() {
		logger.debug("getLog called");
		return super.getLog();
	}

	@Override
	protected Template getTemplate(HttpServletRequest request,
			HttpServletResponse response) {
		try{
			logger.debug("getTemplate called");
			return super.getTemplate(request, response);
		}catch(Exception e){
			logger.error("getTemplate", e);
		}
		return null;
	}

	@Override
	protected Template getTemplate(String name) {
		logger.debug("getTemplate called with "+name);
		return super.getTemplate(name);
	}

	@Override
	protected String getVelocityProperty(String name, String alternate) {
		logger.debug("getVelocityProperty "+name+"="+alternate);
		return super.getVelocityProperty(name, alternate);
	}

	@Override
	protected VelocityView getVelocityView() {
		logger.debug("getVelocityView called ");
		return super.getVelocityView();
	}

	@Override
	protected Template handleRequest(HttpServletRequest request,
			HttpServletResponse response, Context ctx) {
		try{
			logger.debug("handleRequest called ");
			return super.handleRequest(request, response, ctx);
		}catch(Exception e){
			logger.error("service",e);
		}			
		return null;
	}

	@Override
	protected void manageResourceNotFound(HttpServletRequest request,
			HttpServletResponse response, ResourceNotFoundException e)
			throws IOException {
		logger.debug("manageResourceNotFound called ");
		super.manageResourceNotFound(request, response, e);
	}

	@Override
	protected void mergeTemplate(Template arg0, Context arg1,
			HttpServletResponse arg2) throws IOException {
		try{
			logger.debug("mergeTemplate called ");
			super.mergeTemplate(arg0, arg1, arg2);
		}catch(Exception e){
			logger.error("service",e);
		}
	}


	@Override
	protected void setVelocityView(VelocityView view) {
		logger.debug("set velocity view called "+view);
		super.setVelocityView(view);
	}


	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		try{
			super.service(arg0, arg1);
		}catch(Exception e){
			logger.error("service",e);
		}
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		try{
			super.service(arg0, arg1);
		}catch(Exception e){
			logger.error("service",e);
		}
	}


	@Override
	public void log(String msg) {
		logger.debug(msg);
	}

	@Override
	public void log(String message, Throwable t) {
		logger.debug(message,t);
	}

}
