package org.mfr.web.action.portfolio;

import java.util.HashMap;
import java.util.Map;

import org.mfr.util.HttpHelper;
import org.zkoss.xel.VariableResolver;
import org.zkoss.xel.XelException;

public class PortfolioVariableResolver implements VariableResolver {
	public static final String HEADER_PAGE = "headerPage";
	public static final String CSS = "css";
	public static final String PORTFOLIO_PAGE = "portfolioPage";
	public static final String GALLERY_PAGE = "galleryPage";
	public static final String INDEX_PAGE = "indexPage";
	public static final String BLOG_PAGE = "blogPage";
	public static final String BOTTOM_PAGE = "bottomPage";
	private static final long serialVersionUID = -8031635105723847694L;
	static class SiteConfig{
		String headerPage="template/0/pheader.zul";;
		String bottomPage="template/0/pbottom.zul";
		String indexPage;
		String portfolioPage;
		String galleryPage;
		String blogPage;
		String css;
		String blog;
		
	}
	static Map<String,SiteConfig> config=new HashMap<String,SiteConfig>();
	static{
		
		SiteConfig config0=new SiteConfig();
		config0.indexPage="template/0/pindex.zul";
		config0.portfolioPage="template/0/pportfolio.zul";
		config0.galleryPage="template/0/pgallery.zul";
		config0.blogPage="template/0/pblog.zul";
		config0.css="0/";
		config.put("0", config0);
		
		SiteConfig config1=new SiteConfig();
		config1.indexPage="template/0/pindex.zul";
		config1.portfolioPage="template/0/pportfolio.zul";
		config1.blogPage="template/0/pblog.zul";
		config1.css="1/";
		config.put("1", config1);
		
		SiteConfig config2=new SiteConfig();
		config2.headerPage="template/2/pheader.zul";
		config2.indexPage="template/2/pindex.zul";
		config2.portfolioPage="template/2/pportfolio.zul";
		config2.css="2/";
		config.put("2", config2);
		
		SiteConfig config3=new SiteConfig();
		config3.headerPage="template/3/pheader.zul";
		config3.indexPage="template/0/pportfolio.zul";
		config3.galleryPage="template/0/pgallery.zul";
		config3.blogPage="template/0/pblog.zul";
		config3.css="3/";
		config.put("3", config3);
		
		
	}
	
	@Override
	public Object resolveVariable(String varName) throws XelException {
		if("description".equals(varName)){
			return (String)HttpHelper.getHttpSession().getAttribute("description");
		}else{
			return resolveTemplate(varName);
		}
		
	}
	public static final String resolveTemplate(String varName){
		org.mfr.data.Site site=org.mfr.web.action.GlobalVariableResolver.getSite();
		SiteConfig sc=config.get(site.getStyle().toString());
		if(INDEX_PAGE.equals(varName)){
			return sc.indexPage;
		}else if(PORTFOLIO_PAGE.equals(varName)){
			return sc.portfolioPage;
		}else if(CSS.equals(varName)){
			return sc.css;
		}else if(HEADER_PAGE.equals(varName)){
			return sc.headerPage;
		}else if(GALLERY_PAGE.equals(varName)){
			return sc.galleryPage;
		}else if(BLOG_PAGE.equals(varName)){
			return sc.blogPage;
			
		}
		return null;
	}
	public static final String resolveIndex(){
		return resolveTemplate(INDEX_PAGE);
	}
	public static final String resolvePortfolio(){
		return resolveTemplate(PORTFOLIO_PAGE);
	}
	public static final String resolveGallery(){
		return resolveTemplate(GALLERY_PAGE);
	}
	
	public static final String resolveCssDir(){
		return resolveTemplate(CSS);
	}
	public static final String resolveHeader(){
		return resolveTemplate(HEADER_PAGE);
	}
	public static final String resolveBlog(){
		return resolveTemplate(BLOG_PAGE);
	}
}
