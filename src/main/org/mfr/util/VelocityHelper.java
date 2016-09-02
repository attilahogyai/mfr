package org.mfr.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import org.zkoss.zk.ui.Executions;

public class VelocityHelper {
	
	private static VelocityEngine velocityEngine=null;
	private static final Logger log = Logger.getLogger(VelocityHelper.class);
	public static void init(ServletContext context){
		if(velocityEngine==null){
			java.util.Properties props = new java.util.Properties();
			props.put("resource.loader","file");
			props.put("file.resource.loader.description","Velocity FileResource loader");
			props.put("file.resource.loader.class","org.apache.velocity.runtime.resource.loader.FileResourceLoader");
			VelocityConfigurer vc=new VelocityConfigurer();
			vc.setResourceLoaderPath("/WEB-INF/templates/vm/");
			ServletContextResourceLoader rl=new ServletContextResourceLoader(context);
			vc.setResourceLoader(rl);
			vc.setVelocityProperties(props);
			try {
				velocityEngine=vc.createVelocityEngine();
				
			} catch (VelocityException e) {
				log.error("VelocityHelper",e);
			} catch (IOException e) {
				log.error("VelocityHelper",e);
			}
		}
	}
	static String merge(String templateString, Map arguments){
		java.util.Properties props = new java.util.Properties();
		props.put("resource.loader","string");
		props.put("string.resource.loader.description","Velocity StringResource loader");
		props.put("string.resource.loader.class","org.apache.velocity.runtime.resource.loader.StringResourceLoader");
		props.put("string.resource.loader.repository.class","org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl");

		StringResourceRepository repo = StringResourceLoader.getRepository();

		String fakeTemplateName = "/some/imaginary/path/hello.vm";		
		repo.putStringResource(fakeTemplateName, templateString, "ISO-8859-2");

		try {
			Velocity.init(props);
		} catch (Exception e) {
			log.error("",e);
		}

		return mergeTemplate("/some/imaginary/path/hello.vm", arguments);

	}
	static String mergeFile(String templateFile, Map arguments){
		return mergeTemplate(templateFile, arguments);

	}

	private static String mergeTemplate(String templateString, Map arguments) {
		
		java.io.StringWriter sw = null;
		try {			
			Template message = velocityEngine.getTemplate(templateString,"utf-8");
			if (message == null) {
				log.error("template is null!");
			}

			VelocityContext context = new VelocityContext();
			Set argumentKeys = null;
			
			if(arguments!=null)
			{
				argumentKeys = arguments.keySet();
				for (Iterator iterator = argumentKeys.iterator(); iterator.hasNext();) {
					String argumentKey = (String) iterator.next();
					context.put(argumentKey, arguments.get(argumentKey));						
				}			
			}
			
			sw = new java.io.StringWriter();
			message.merge(context, sw);
			
		} catch (Exception e) {
			log.error("",e);
		}				
		
		return sw.toString();
	}
}
