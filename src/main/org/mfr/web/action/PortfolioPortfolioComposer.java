package org.mfr.web.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mfr.data.Category;
import org.mfr.data.ImageDataModel;
import org.mfr.data.Photo;
import org.mfr.data.PhotoCategory;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.data.SiteGalleriesDao;
import org.mfr.manager.ImageDataManager;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Window;

public class PortfolioPortfolioComposer extends SelectorComposer {
	private static final Logger logger=LoggerFactory.getLogger(PortfolioPortfolioComposer.class);
	@Autowired
	private SiteGalleriesDao siteGalleriesDao;
	@Wire	
	private Div albums1;
	@Autowired
	private PhotoCategoryDao photoCategoryDao;
	@Wire
	private Div main;
	
	final org.mfr.data.Site site=org.mfr.web.action.GlobalVariableResolver.getSite();
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		main=(Div)comp;
		refreshImages();
	}

	public void refreshImages(){
		List<Category> albumsList1=siteGalleriesDao.findSiteCategoriesForType(site.getId(), 4);
		try {
			if(albums1.getFirstChild()!=null){
				albums1.getChildren().clear();
			}			
			
			if(albumsList1.size()>0){

				for (final Category category : albumsList1) {
					//final List<PhotoCategory> albumCategories1=photoCategoryDao.findByCategoryId(category.getId());
					final Photo photo=photoCategoryDao.getCoverPhoto(category.getId());
					if(photo==null){ // empty album
						continue;
					}
					final ImageDataModel idm=ImageDataManager.extractImageData(photo, false,false);
					HttpHelper.setImageAccessForPublic(idm);
					Component [] comps=main.getTemplate("albums1Template").create(albums1,null,new org.zkoss.xel.VariableResolver(){
						public Object resolveVariable(String variable){
							if("each".equals(variable)){
								return idm;
							}else if("category".equals(variable)){
								return category;
							}else{
								return null;
							}
						}
					},null);
				}

			}
			
		} catch (IOException e) {
			logger.error("refreshImages", e);
		}
		
	}	
	
	public void createAlbumSelectorWindow(int type, boolean single){
		
		Map arg=new HashMap();
		arg.put("site",site);
		arg.put("type",type);
		arg.put("single",single);
	 	Component comp=Executions.createComponents("comp/albumselector.zul", null, arg);
	 	Window albumselector=(Window)comp.getFellow("albumselector");
	 	albumselector.addEventListener("onClose", new org.zkoss.zk.ui.event.EventListener(){
	 		   public void onEvent(Event event){
	 			  refreshImages();
	 		   }
	 	});
	 	albumselector.doModal();
	}
	public void createMainImageSelectorWindow(Integer catId){
		
		Map arg=new HashMap();
		arg.put("album",catId);
		arg.put("mainImage",true);
	 	Component comp=Executions.createComponents("comp/albumselector.zul", null, arg);
	 	Window albumselector=(Window)comp.getFellow("albumselector");
	 	albumselector.addEventListener("onClose", new org.zkoss.zk.ui.event.EventListener(){
	 		   public void onEvent(Event event){
	 			  refreshImages();
	 		   }
	 	});
	 	albumselector.doModal();
	}	
	public void removeCategoryFromSite(Integer type, Integer cat){
		siteGalleriesDao.deleteByCategorySiteType(cat, site.getId(),type);
	}	
}
