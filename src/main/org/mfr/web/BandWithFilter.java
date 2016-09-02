package org.mfr.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.RequestWrapper;
import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.Constants;
import org.mfr.data.ImageDataModel;
import org.mfr.data.UserPreference;
import org.mfr.manager.ImageDataManager;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.mfr.web.action.GlobalVariableResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class BandWithFilter implements Filter {
	private static final Logger logger=LoggerFactory.getLogger(BandWithFilter.class);
	private WebApplicationContext springContext;
	public static final String LANGUAGE="l";
	private Pattern filterOut=Pattern.compile(".css$|.js$|.ico$|.wpd$|");
	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest servletR=((HttpServletRequest)request);
		RequestWrapper.httpRequest.set(servletR);
		String uri=servletR.getRequestURI();
		filterOut=Pattern.compile(".css$|.js$|.ico$|.wpd$|/img/|.json$");
		Matcher matcher=filterOut.matcher(uri);
		if(!matcher.find()){
			String language=servletR.getParameter(LANGUAGE);
			if(language==null){
				language=(String)servletR.getAttribute(LANGUAGE);
			}
			if(uri.startsWith("/en/") || uri.startsWith("/hu/") || uri.startsWith("/ro/")){
				language=uri.substring(1,3);
				uri=uri.substring(3);
				servletR.setAttribute(LANGUAGE,language);
			}		
			
			if(language==null && HttpHelper.getUser()==null){
				UserPreference up=HttpHelper.getUserPrefFromCookie((HttpServletRequest)request);
				if(up.getLanguage()!=null){
					ZkUtil.setLanguage(up.getLanguage(), ((HttpServletRequest)request).getSession());
				}else{
					ZkUtil.setLanguage("hu", ((HttpServletRequest)request).getSession());
				}
			}else if(language!=null){
				ZkUtil.setLanguage(language, ((HttpServletRequest)request).getSession());
				UserPreference up=HttpHelper.getUserPref(((HttpServletRequest)request));
				up.setLanguage(language);
				HttpHelper.storeCookie(up,(HttpServletResponse)response);
			}
			
			
			if(uri.startsWith("/_")){   // /album/id/nos_eze_az_album_100000
				logger.debug("doFilter ["+uri+"]");
				
				String pageString=uri.substring(uri.lastIndexOf("/_")+2);
	//			if(pageString.startsWith("en_") || pageString.startsWith("hu_") || pageString.startsWith("ro_")){
	//				String lang=pageString.substring(0,2);
	//				servletR.setAttribute(LanguageFilter.LANGUAGE, lang);
	//				pageString=pageString.substring(3);
	//			}
				request.setAttribute(GlobalVariableResolver._PAGEACTURI, "_"+pageString);
				String page=null;
				int s=pageString.indexOf("_");
				if(s>-1){
					page=pageString.substring(0,s);
					String variables=pageString.substring(s);
					String [] vars=variables.split("_");
					for (int i = 0; i < vars.length-2; i=i+2) {
						servletR.setAttribute(vars[i], vars[i+1]);
					}
				}else{
					page=pageString;
				}
				if(page.indexOf(".zul")==-1){
					page+=".zul";
				}
				if(!page.startsWith("/")){
					page="/"+page;
				}
				logger.debug("page ["+page+"]");	
				request.getRequestDispatcher(page).forward(request, response);
				
			}else if(uri.startsWith("/download/album/")){
				logger.debug("doFilter ["+uri+"]");
				Integer albumId=new Integer(uri.substring("/download/album/".length()));
				String fsize=request.getParameter("s");
				String filepath=Constants.MEDIUMPATH;
				if(fsize.equals(ImgFilter.MEDIUM)){
					filepath=Constants.MEDIUMPATH;
				}else if(fsize.equals(ImgFilter.PREV)){
					filepath=Constants.PREVIEWPATH;
				}else if(fsize.equals(ImgFilter.THUMB)){
					filepath=Constants.THUMBPATH;
				}
				try {
					ImageDataManager imageDataManager=(ImageDataManager)springContext.getBean("imageDataManager");
					CategoryDao categoryDao=(CategoryDao)springContext.getBean("categoryDao");
					Category category=categoryDao.findById(albumId);
					if(category==null){
						throw new ServletException("requested resource not found");
					}
					ImageDataModel []imageData=imageDataManager.listAlbumContentbyId(category,false,false);
					if(category.getAllowDownload()==1){
						String outFilename = HttpHelper.replacetoAsciiChars(category.getName())+".zip";
						ZipOutputStream out = new ZipOutputStream( response.getOutputStream() );  
						//Set header to HttpServletResponse response  
						response.reset();  
						response.setContentType("application/zip");  
						((HttpServletResponse)response).addHeader("Content-Disposition","inline; filename="+outFilename);
						
						for (int i = 0; i < imageData.length; i++) {
							String imageName=imageData[i].getImageName();
							String realpath=imageData[i].getImageRealPath();
							imageName=HttpHelper.replacetoAsciiChars(imageName);
							int oriExt=imageName.lastIndexOf(".");
							String ext=null;
							if(oriExt>-1){
								ext=imageName.substring(oriExt).toUpperCase();
								if(!(HttpHelper.isImageExt(ext))){
									ext=null;
								}
							}
							int lastindx=realpath.lastIndexOf(".");
							if(lastindx>-1 && ext==null){
								ext=realpath.substring(lastindx);
								imageName+=ext;
							}
							addEntry(out, imageName,1);
							
							String imagePath=Constants.STOREDIR+filepath+imageData[i].getImageRealPath();
							File f=new File(imagePath);
							if(f.exists()){
								FileInputStream fis=new FileInputStream(new File(imagePath));
								byte []bytearray=new byte[100000];
								int size=0;
								while ( ( size = fis.read( bytearray ) ) != -1 ) {  
								     out.write( bytearray, 0 , size );  
								}  
								fis.close();
							}else{
								logger.error("file not found ["+f.getAbsolutePath()+"]");
							}
						         
						}
					    out.flush();  
					    out.close();  
					    response.getOutputStream().flush();
					}
				} catch (Exception e) {
					logger.error("doFilter",e);
				}
				
				
			}else if(uri.startsWith("/download/")){
				logger.debug("doFilter ["+uri+"]");
				uri=uri.replace("/download/", "/");
				int lastslash=uri.lastIndexOf('/');
				String filename=uri.substring(lastslash+1);
				filename=filename.replaceAll("\\(","");
				filename=filename.replaceAll("\\)","");
				((HttpServletResponse)response).addHeader("Content-disposition", "attachment; filename="+filename);
				RequestDispatcher disp=servletR.getRequestDispatcher(uri);
				disp.forward(servletR, response);
			}else{
				try {
					if(request.getAttribute(GlobalVariableResolver._PAGEACTURI)==null){
						request.setAttribute(GlobalVariableResolver._PAGEACTURI, uri);
					}
					chain.doFilter(request, response);
				} catch (Exception e) {
					logger.error("unhandled exception catched by ThreadNameFilter", e);
					throw new ServletException(e);
				}
			}
		}else{
			chain.doFilter(request, response);
		}
	}

	public void addEntry(ZipOutputStream out, String imageName,int i)
			throws IOException {
		try{
			ZipEntry ze=new ZipEntry( imageName );
			out.putNextEntry( ze );
		}catch(ZipException e){
			if(e.getMessage().indexOf("duplicate entry")>-1){
				int point=imageName.indexOf(".");
				i++;
				if(point>-1){
					imageName=imageName.substring(0,point)+(i)+imageName.substring(point);
				}
				if(i<50){
					addEntry(out, imageName,i);
				}
			}else{
				throw e;
			}
		}
		
	}

	public void init(FilterConfig config) throws ServletException {
		    springContext = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
	}
}
