package org.mfr.web.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.ImageDataModel;
import org.mfr.data.Photo;
import org.mfr.data.PhotoCategory;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.data.PhotoDao;
import org.mfr.data.SiteDao;
import org.mfr.data.SiteGalleries;
import org.mfr.data.SiteGalleriesDao;
import org.mfr.manager.ImageDataManager;
import org.mfr.util.HttpHelper;
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
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;


public class PortfolioIndexComposer extends SelectorComposer {
	private static final Logger logger=LoggerFactory.getLogger(PortfolioIndexComposer.class);
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private SiteGalleriesDao siteGalleriesDao;
	@Autowired
	private PhotoCategoryDao photoCategoryDao;	
	@Autowired
	private SiteDao siteDao;
	@Autowired
	private PhotoDao photoDao;
	
	@Wire
	private Div mainImage;
	@Wire	
	private Div albums1;
	
	@Wire
	private Div main;
	
	
	
	@Wire
	private CKeditor welcometext;
	
	final org.mfr.data.Site site=org.mfr.web.action.GlobalVariableResolver.getSite();
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		main=(Div)comp;
		refreshImages();
	}
	public void refreshImages(){

		try {
			// random images from categories
/*			
			List<Category> mainCats=siteGalleriesDao.findSiteCategoriesForType(site.getId(), 1);
			if(mainCats.size()>0){
				List<PhotoCategory> mainCategory=photoCategoryDao.getByCategoryList(mainCats);
				int max=mainCategory.size();
				int act=ZkUtil.getRandomInt(max);
				ImageDataModel imageData=ImageDataManager.extractImageData(mainCategory.get(act).getPhoto(), false);
				mainImage.setContent(imageData.getImagePreview());
			}
*/
			List<SiteGalleries> aboutCats=siteGalleriesDao.findSiteGalleriesForType(site.getId(), 1);
			Photo mainPhoto=null;
			if(aboutCats!=null && aboutCats.size()>0 && aboutCats.get(0).getPhoto()!=null){
				mainPhoto=aboutCats.get(0).getPhoto();
			}else{
				mainPhoto=photoDao.getMainPhoto(site.getOwner().getId());
			}
			if(mainPhoto!=null){
				ImageDataModel imageData=ImageDataManager.extractImageData(mainPhoto, false,false);
				if(mainImage!=null){
					mainImage.setStyle("background-image: url(/mimg/medium/id/"+imageData.getId()+");");
					HttpHelper.setImageAccessForPublic(imageData);
				}
			}

			
//			if(albums1.getFirstChild()!=null){
//				albums1.getFirstChild().detach();
//			}
			List<Category> albumsList1=siteGalleriesDao.findSiteCategoriesForType(site.getId(), 2);
			
			while(albums1.getFirstChild()!=null) albums1.getFirstChild().detach();
			if(albumsList1.size()>0){
//				Div subDiv=new Div();
//				albums1.appendChild(subDiv);

				for (final Category category : albumsList1) {
					final Photo photo=photoCategoryDao.getCoverPhoto(category.getId());
					ImageDataModel id=null;
					if(photo!=null){
						id=ImageDataManager.extractImageData(photo, false,false);
					}
					final ImageDataModel idm=id;
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
	public void createAlbumSelectorWindow(int type, boolean single,boolean selectImage){
		
		Map arg=new HashMap();
		arg.put("site",site);
		arg.put("type",type);
		arg.put("single",single);
		arg.put("selectImage",selectImage);
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
	
	public void saveWelcomeText(){
		String language=GlobalVariableResolver.getLanguage();
		if(language.toUpperCase().equals("HU")){
			site.setDescription(welcometext.getValue());
		}else{
			site.setDescriptionEn(welcometext.getValue());
		}
		siteDao.merge(site);
		ZkUtil.messageBoxInfo("text.saved");
	}
	
}
