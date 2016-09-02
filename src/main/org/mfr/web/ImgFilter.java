package org.mfr.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mfr.data.Constants;
import org.mfr.data.Photo;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.data.PhotoDao;
import org.mfr.data.SiteDao;
import org.mfr.util.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.util.logging.Log;

public class ImgFilter implements Filter {
	public static final String THUMB = "thumb";
	public static final String MEDIUM = "medium";
	public static final String PREV = "prev";
	public static final String ORI = "ori";
	private WebApplicationContext springContext;
	private static final Logger logger = LoggerFactory
			.getLogger(ImgFilter.class);
	private PhotoDao photoDao;
	private PhotoCategoryDao photoCategoryDao;
	
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		String uri = ((HttpServletRequest) request).getRequestURI();
		try{
		if(logger.isTraceEnabled()){
			logger.trace("doFilter [" + uri + "]");
		}
		if(uri.startsWith("/mimg/")){
			uri=uri.substring(6);
			if(uri.indexOf('/')>-1){
				int slashIdx=uri.indexOf('/');
				String type=uri.substring(0, slashIdx);
				uri=uri.substring(slashIdx+1);
				if(uri.indexOf('/')>-1){
					slashIdx=uri.indexOf('/');
					String property=uri.substring(0, slashIdx);
					if(property.equals("id")){
						int startId = uri.lastIndexOf("id/")+3;
						int endId = uri.indexOf('/',startId);
						if(endId==-1){
							endId=uri.length();
						}
						String id = uri.substring(startId,endId);
						
						HttpHelper.PublicImageDesc pid=HttpHelper.isImageAccessForPublic(new Integer(id));
						String name=null;
						String path=null;
						if(pid==null){
							Photo photo=photoCategoryDao.getPublicVisiblePhotoForId(Integer.parseInt(id));
							if(photo!=null){
								name=photo.getName();
								path=photo.getPath();
							}
						}else{
							name=pid.name;
							path=pid.path;
						}
						
						if(name!=null){
							String typeUrl=null;
							if(type.equals(THUMB)){
								typeUrl=Constants.THUMBPATH;
							}else if(type.equals(ORI)){
								typeUrl=Constants.ORIGINALPATH;
							}else if(type.equals(PREV)){
								typeUrl=Constants.PREVIEWPATH;
							}else if(type.equals(MEDIUM)){
								typeUrl=Constants.MEDIUMPATH;
							}
							File f=new File(Constants.STOREDIR+typeUrl+path);
							if(f.exists()){
								FileInputStream fis=new FileInputStream(f);
								String mimetype = URLConnection.getFileNameMap().getContentTypeFor(name);
								response.setContentType(mimetype);
								response.setContentLength((int)f.length());
								
								byte[] buffer=new byte[1000];
								int c=0;
								OutputStream os=response.getOutputStream();
								try{									
									while((c=fis.read(buffer))>-1){
										os.write(buffer,0,c);
									}
								}finally{
									os.flush();
									os.close();
									fis.close();
								}
							}else{
								logger.warn("FILE NOT FOUND:"+f.getAbsolutePath());
								//throw new ServletException("file not found");
							}
						}
					}
				}
			}
		}else{
			chain.doFilter(request, response);
		}
		}catch(Exception e){
			logger.error("image filter error",e);
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		springContext = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
		photoDao=(PhotoDao)springContext.getBean("photoDao");
		photoCategoryDao=(PhotoCategoryDao)springContext.getBean("photoCategoryDao");
		
	}

}
