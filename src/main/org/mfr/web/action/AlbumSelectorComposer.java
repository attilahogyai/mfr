package org.mfr.web.action;

import java.io.IOException;
import java.util.List;

import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.ImageDataModel;
import org.mfr.data.Photo;
import org.mfr.data.PhotoCategory;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.data.PhotoDao;
import org.mfr.data.Site;
import org.mfr.data.SiteGalleries;
import org.mfr.data.SiteGalleriesDao;
import org.mfr.data.Useracc;
import org.mfr.manager.ImageDataManager;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AlbumSelectorComposer extends SelectorComposer<Window> {
	private static final Logger logger=LoggerFactory.getLogger(AlbumSelectorComposer.class);
	@Autowired
	private CategoryDao categoryDao;
	
	@Autowired
	private PhotoDao photoDao;
	
	@Autowired
	private SiteGalleriesDao siteGalleriesDao;
	@Autowired
	private PhotoCategoryDao photoCategoryDao;	
	
	@Wire
 	private Grid albumGrid;
	@Wire
	private Textbox name1;
	@Wire
	private Label titleText;
	
	private Site site;
	private Integer type;
	private boolean single;
	private boolean selectImage;
	private boolean mainImageSelection;
	
	private Component mainComp;
	
	private String currentAlbum;
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		if(HttpHelper.getUser()==null){
			return;
		}
		super.doAfterCompose(comp);
		mainComp = comp;
		site = (Site) Executions.getCurrent().getArg().get("site");
		if(Executions.getCurrent().getArg().get("type")!=null){
			type = (Integer) Executions.getCurrent().getArg().get("type");
		}
		if(Executions.getCurrent().getArg().get("single")!=null){
			single = (Boolean) Executions.getCurrent().getArg().get("single");
		}
		if(Executions.getCurrent().getArg().get("mainImage")!=null){
			mainImageSelection = (Boolean) Executions.getCurrent().getArg().get("mainImage");
			Integer album= (Integer) Executions.getCurrent().getArg().get("album");
			selectAlbum(album.toString());
		}
		if(Executions.getCurrent().getArg().get("selectImage")!=null){
			selectImage = (Boolean) Executions.getCurrent().getArg().get("selectImage");
		}
		if(selectImage){
			titleText.setValue(Labels.getLabel("image.selector"));
		}else if(mainImageSelection){
			titleText.setValue(Labels.getLabel("cover.image.selector"));
		}else{
			titleText.setValue(Labels.getLabel("album.selector"));
		}
		
		refreshContent();
	}
	public void refreshContent(){
		Useracc useracc=HttpHelper.getUser().getUseracc();
		if(currentAlbum==null){
			List<Category> categories=(List<Category>)categoryDao.findByUseracc(useracc);
			albumGrid.setModel(new ListModelList(categories));
			albumGrid.setRowRenderer(new AlbumTableRender());
		}else{
			List<PhotoCategory> photos=(List<PhotoCategory>)photoCategoryDao.findByCategoryId(Integer.parseInt(currentAlbum));
			if(!mainImageSelection){
				photos.add(0,new PhotoCategory());
			}
			albumGrid.setModel(new ListModelList(photos));
			albumGrid.setRowRenderer(new PhotoTableRender());
		}
	}
	public boolean updateSelection(String id){
		return updateSelection(Integer.parseInt(id));
	}
	public boolean updateSelection(Integer id){
		Photo photo=null;
		Category cat=null;
		if(selectImage || mainImageSelection){
			logger.debug("select image ["+id+"]");
			photo=photoDao.findById(id);
		}else{
			logger.debug("select category ["+id+"]");
			cat=categoryDao.findById(id);
			Long counT=photoCategoryDao.countByCategoryId(id);
			if(counT==0){
				ZkUtil.messageBoxWarning("empty.category.selection.not.allowed");
				return false;
			}
			
		}
		if(mainImageSelection){
			photoCategoryDao.updateCoverImage(photo.getId(), Integer.parseInt(currentAlbum));
			Events.postEvent("onClose",mainComp,null);
			return true;
		}else{
			logger.debug("update site setup");
			List<SiteGalleries> typeList=(List<SiteGalleries>)siteGalleriesDao.findSiteGalleriesForType(site.getId(), type);
			if(single && typeList!=null && typeList.size()>0){
				if(selectImage){
					typeList.get(0).setPhoto(photo);
				}else{
					typeList.get(0).setCategory(cat);
				}
				siteGalleriesDao.merge(typeList.get(0));
				Events.postEvent("onClose",mainComp,null);
				return true;
			}else{
				boolean already=false;
				for (SiteGalleries siteGalleries : typeList) {
					if(selectImage){
						if(siteGalleries.getPhoto().getId().equals(id)){
							ZkUtil.messageBoxWarning("already.on.the.list");
							already=true;
							break;
						}
					}else{
						if(siteGalleries.getCategory().getId().equals(id)){
							ZkUtil.messageBoxWarning("already.on.the.list");
							already=true;
							break;
						}
					}
				}
				if(!already){
					SiteGalleries siteGal=new SiteGalleries();
					if(selectImage){
						siteGal.setPhoto(photo);
					}else{
						siteGal.setCategory(cat);
					}
					siteGal.setSite(site);
					siteGal.setType(type);
					siteGalleriesDao.persist(siteGal);
					Events.postEvent("onClose",mainComp,null);
					return true;
				}
				
			}
		}
		return false;
	}
	public void selectAlbum(String id){
		currentAlbum=id;
		refreshContent();
	}
	public class AlbumTableRender implements RowRenderer {

		@Override
		public void render(Row row,final Object rowData, int index) throws Exception {
			Page page=Executions.getCurrent().getDesktop().getFirstPage();
			Component albumselector=page.getFellow("albumselector");
			
			Component [] comps=albumselector.getTemplate("rowTemplate").create(row,null,new org.zkoss.xel.VariableResolver(){
				public Object resolveVariable(String variable){
					if("each".equals(variable)){
						return rowData;
					}else if("selectImage".equals(variable)){
						return selectImage;
					}
					return null;
				}
			},null);
			
		}
		
	}
	public class PhotoTableRender implements RowRenderer {

		@Override
		public void render(Row row,final Object rowData,final int index) throws Exception {
			Page page=Executions.getCurrent().getDesktop().getFirstPage();
			Component albumselector=page.getFellow("albumselector");
			Template template=null;
			if(mainImageSelection){
				template=albumselector.getTemplate("mainImageRowTemplate");
			}else{
				template=albumselector.getTemplate("imageRowTemplate");
			}
			Component [] comps=template.create(row,null,new org.zkoss.xel.VariableResolver(){
				public Object resolveVariable(String variable){
					if("each".equals(variable)){
						return rowData;
					}else if("photo".equals(variable)){
						ImageDataModel imageData=null;
						try {
							imageData=ImageDataManager.extractImageData(((PhotoCategory)rowData).getPhoto(), false,false);
						} catch (IOException e) {
							ZkUtil.logProcessError(e);
						}
						return imageData;
					}else if("index".equals(variable)){
						return new Integer(index);
					}
					return null;
				}
			},null);
			
		}
		
	}
	
	public Grid getAlbumGrid() {
		return albumGrid;
	}
	public void setAlbumGrid(Grid albumGrid) {
		this.albumGrid = albumGrid;
	}
	public boolean isSelectImage() {
		return selectImage;
	}
	public void setSelectImage(boolean selectImage) {
		this.selectImage = selectImage;
	}
	
}
