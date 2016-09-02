package org.mfr.web.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mfr.data.Category;
import org.mfr.data.ImageDataModel;
import org.mfr.data.PhotoCategory;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.data.SiteDao;
import org.mfr.data.SiteGalleries;
import org.mfr.data.SiteGalleriesDao;
import org.mfr.manager.ImageDataManager;
import org.mfr.util.ZkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkforge.ckez.CKeditor;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Window;

public class PortfolioAboutComposer extends SelectorComposer {
	private static final Logger logger=LoggerFactory.getLogger(PortfolioAboutComposer.class);
	@Wire
	private CKeditor abouttext;
	@Wire
	private Div main;
	@Autowired
	private SiteDao siteDao;
	@Autowired
	private SiteGalleriesDao siteGalleriesDao;
	@Autowired
	private PhotoCategoryDao photoCategoryDao;	
	@Wire
	private Image aboutImage;
	
	final org.mfr.data.Site site=org.mfr.web.action.GlobalVariableResolver.getSite();
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		main=(Div)comp;
		refreshImages();
	}

	public void refreshImages(){

		List<SiteGalleries> aboutCats=siteGalleriesDao.findSiteGalleriesForType(site.getId(), 3);
		if(aboutCats!=null && aboutCats.size()>0){
			try {
				ImageDataModel imageData=ImageDataManager.extractImageData(aboutCats.get(0).getPhoto(), false,false);
				aboutImage.setContent(imageData.getImagePreview());
			} catch (IOException e) {
				logger.error("refreshImages", e);
			}
		}
		
	}	
	public void saveAboutText(){
		String language=GlobalVariableResolver.getLanguage();
		if(language.toUpperCase().equals("HU")){
			site.setAbout(abouttext.getValue());
		}else{
			site.setAboutEn(abouttext.getValue());
		}
		siteDao.merge(site);
		ZkUtil.messageBoxInfo("text.saved");
	}
	public void createAlbumSelectorWindow(int type, boolean single){
		
		Map arg=new HashMap();
		arg.put("site",site);
		arg.put("type",type);
		arg.put("single",single);
		arg.put("selectImage",true);
	 	Component comp=Executions.createComponents("comp/albumselector.zul", null, arg);
	 	Window albumselector=(Window)comp.getFellow("albumselector");
	 	albumselector.addEventListener("onClose", new org.zkoss.zk.ui.event.EventListener(){
	 		   public void onEvent(Event event){
	 			  refreshImages();
	 		   }
	 	});
	 	albumselector.doModal();
	}	

}
