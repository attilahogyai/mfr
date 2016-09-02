package org.mfr.web.action;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.ImageDataModel;
import org.mfr.data.Photo;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.data.PhotoDao;
import org.mfr.manager.ImageDataManager;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.A;
import org.zkoss.zul.Textbox;

import sun.util.logging.resources.logging;

public class SearchComposer extends SelectorComposer<Component> {
	private static final Log log = LogFactory.getLog(SearchComposer.class);
//	public static final String LAST_SEARCH_RESULT_PHOTO = "lastSearchResultPhoto";
//	public static final String LAST_SEARCH_RESULT_CATEGORY = "lastSearchResultCategory";
	
	public static final String LAST_SEARCH_RESULT_PHOTO_COUNT = "lastSearchResultPhotoCOUNT";
	public static final String LAST_SEARCH_RESULT_CATEGORY_COUNT = "lastSearchResultCategoryCOUNT";

	public static final String LAST_SEARCH_STRING = "searchString";
	@Wire
	private A search;
	@Wire
	private Textbox searchText;
	@WireVariable
	private PhotoDao photoDao;
	@WireVariable
	private CategoryDao categoryDao;
	@WireVariable
	private PhotoCategoryDao photoCategoryDao;
	
	private Object[] searchString;
	
	ImageDataModel [] imageData=null;
	List<Object[]> categoriesData=null;
	boolean emptyData=false;
	private static final double ITEMPERPAGE=200;
	
	private Long[] imagePageCount;
	private Long[] albumPageCount;
	private int lastPhotoPage;
	private int lastAlbumPage;
	
	private long countPhotos;
	private long countAlbums;
	
	@Override
	public void doBeforeComposeChildren(Component comp) throws Exception {
		if("maincontainer".equals(comp.getId())){
//			imageData=(ImageDataModel [])HttpHelper.getHttpSession().getAttribute(LAST_SEARCH_RESULT_PHOTO);
//			categoriesData=(List<Object[]>)HttpHelper.getHttpSession().getAttribute(LAST_SEARCH_RESULT_CATEGORY);
			searchString = new Object[]{(String)HttpHelper.getHttpSession().getAttribute(LAST_SEARCH_STRING)};
			countPhotos=(Integer)HttpHelper.getHttpSession().getAttribute(LAST_SEARCH_RESULT_PHOTO_COUNT);
			countAlbums=(Integer)HttpHelper.getHttpSession().getAttribute(LAST_SEARCH_RESULT_CATEGORY_COUNT);
			
			String lastPage=HttpHelper.getHttpRequest().getParameter("p");
			if(lastPage!=null){
				if(lastPage.equals("next")){
					if(lastPhotoPage*ITEMPERPAGE<countPhotos){
						lastPhotoPage++;
					}
				}else{
					try{
						lastPhotoPage=Integer.parseInt(lastPage);
					}catch(Exception e){}
				}
			}
			
			
			performPhotoSearch();
			performCategorySearch();
			
			if(countPhotos>ITEMPERPAGE){
				double r=countPhotos/ITEMPERPAGE;
				imagePageCount = new Long[(int)Math.ceil(r)];
			}
			if(countAlbums>ITEMPERPAGE){
				double r=countAlbums/ITEMPERPAGE;
				albumPageCount = new Long[(int)Math.ceil(r)];
			}
			
			comp.getPage().setTitle(Labels.getLabel("search.result",searchString));
			if(imageData!=null){
				emptyData=imageData.length==0;
			}
			
			super.doBeforeComposeChildren(comp);
		}
	}
	public Photo getCoverPhoto(Category cat){
		Photo photo=photoCategoryDao.getCoverPhoto(cat.getId());
		return photo;
	}
	@Listen("onClick=#i1;onClick=#i2;onClick=#i3;onClick=#i4;onClick=#i5;onClick=#iNext")
	public void selectImagePage(Event event){
		String label=((A)event.getTarget()).getLabel();
		Integer page=Integer.parseInt(label);
		this.lastPhotoPage=new Double(page*ITEMPERPAGE).intValue();
		performPhotoSearch();
				
	}
	@Listen("onClick=#a1;onClick=#a2;onClick=#a3;onClick=#a4;onClick=#a5;onClick=#aNext")
	public void selectAlbumPage(Event event){
		String label=((A)event.getTarget()).getLabel();
		Integer page=Integer.parseInt(label);
		
	}
	@Listen("onClick=#search;onOK=#searchText")
	public void setCounters(){
		String t=searchText.getValue();
		if(t==null || t.length()<4){
			ZkUtil.messageBoxInfo("minimum.length.error",new Object[]{t});
		}else{
			HttpHelper.getHttpSession().setAttribute(LAST_SEARCH_STRING, t);
			int catResult=categoryDao.findCategoryCount(t);
			HttpHelper.getHttpSession().setAttribute(LAST_SEARCH_RESULT_CATEGORY_COUNT, catResult);
			boolean result=false;
			if(catResult>0){
				result=true;
			}
			int photoCount=photoDao.findPhotoCount(t);
			HttpHelper.getHttpSession().setAttribute(LAST_SEARCH_RESULT_PHOTO_COUNT, photoCount);
			if(photoCount>0){
				result=true;
			}
			if(!result){
				ZkUtil.messageBoxInfo("no.search.result",new Object[]{t});
			}else{
				Executions.sendRedirect("_searchresult");
			}
		}
		
		//performSearch(t);
	}
	public void performPhotoSearch() {
		List<Photo> photo=photoDao.findPhoto((String)searchString[0],new Double(lastPhotoPage*ITEMPERPAGE).intValue(), (int)ITEMPERPAGE);
		if(photo!=null && photo.size()>0){
			try {
				imageData=ImageDataManager.extractImageModelList(photo, true,false);
			} catch (IOException e) {
				log.error("performPhotoSearch",e);
			}
		}
	}
	public void performCategorySearch() {
		if(searchString!=null && searchString[0]!=null){
			categoriesData=categoryDao.findCategory((String)searchString[0]);
		}
	}
//	public void performSearch(String t) {
//		if(t==null || t.length()<4){
//			ZkUtil.messageBoxInfo("minimum.length.error",new Object[]{t});
//		}else{
//			HttpHelper.getHttpSession().setAttribute(LAST_SEARCH_STRING, t);
//			
//			List<Object[]> categories=categoryDao.findCategory(t);
//			boolean result=false;
//			if(categories!=null && categories.size()>0){
//				HttpHelper.getHttpSession().setAttribute(LAST_SEARCH_RESULT_PHOTO_COUNT, categories);
//				result=true;
//			}
//			List<Photo> photo=photoDao.findPhoto(t);
//			if(photo!=null && photo.size()>0){
//				result=true;
//				try {
//					ImageDataModel []images=ImageDataManager.extractImageModelList(photo, false);
//					HttpHelper.getHttpSession().setAttribute(LAST_SEARCH_RESULT_PHOTO, images);
//					
//				} catch (IOException e) {
//				}
//			}
//			if(!result){
//				ZkUtil.messageBoxInfo("no.search.result",new Object[]{t});
//			}
//		}
//	}

	public ImageDataModel[] getImageData() {
		return imageData;
	}

	public void setImageData(ImageDataModel[] imageData) {
		this.imageData = imageData;
	}
	public boolean isEmptyData() {
		return emptyData;
	}

	public void setEmptyData(boolean emptyData) {
		this.emptyData = emptyData;
	}


	public Textbox getSearchText() {
		return searchText;
	}


	public void setSearchText(Textbox searchText) {
		this.searchText = searchText;
	}


	public Object[] getSearchString() {
		return searchString;
	}


	public void setSearchString(Object[] searchString) {
		this.searchString = searchString;
	}
	public List<Object[]> getCategoriesData() {
		return categoriesData;
	}
	public void setCategoriesData(List<Object[]> categoriesData) {
		this.categoriesData = categoriesData;
	}
	public Long[] getImagePageCount() {
		return imagePageCount;
	}
	public void setImagePageCount(Long[] imagePageCount) {
		this.imagePageCount = imagePageCount;
	}
	public Long[] getAlbumPageCount() {
		return albumPageCount;
	}
	public void setAlbumPageCount(Long[] albumPageCount) {
		this.albumPageCount = albumPageCount;
	}
	public int getLastPhotoPage() {
		return lastPhotoPage;
	}
	public void setLastPhotoPage(int lastPhotoPage) {
		this.lastPhotoPage = lastPhotoPage;
	}
	public int getLastAlbumPage() {
		return lastAlbumPage;
	}
	public void setLastAlbumPage(int lastAlbumPage) {
		this.lastAlbumPage = lastAlbumPage;
	}
	public long getCountPhotos() {
		return countPhotos;
	}
	public void setCountPhotos(long countPhotos) {
		this.countPhotos = countPhotos;
	}



}
