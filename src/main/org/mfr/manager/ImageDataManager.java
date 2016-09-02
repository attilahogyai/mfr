package org.mfr.manager;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.Constants;
import org.mfr.data.ExifData;
import org.mfr.data.ExifDataDao;
import org.mfr.data.ImageDataModel;
import org.mfr.data.Photo;
import org.mfr.data.PhotoCategory;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.data.PhotoDao;
import org.mfr.data.User;
import org.mfr.data.Useracc;
import org.mfr.data.UseraccDao;
import org.mfr.image.ExifDataEnum;
import org.mfr.image.GalleryImageData;
import org.mfr.image.MyZkImage;
import org.mfr.mybatis.impl.MPhotoCategoryDao;
import org.mfr.mybatis.impl.PhotoCategoryDaoMapper;
import org.mfr.mybatis.types.MCategory;
import org.mfr.mybatis.types.MExifData;
import org.mfr.mybatis.types.MImageDataModel;
import org.mfr.mybatis.types.MPhoto;
import org.mfr.util.JsonUtility;
import org.mfr.util.ZkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Image;
import org.zkoss.zul.Messagebox;

public class ImageDataManager {
	public static final String DEFAULT_CATEGORY_NAME="Album";
	public static final String SELECTED_CATEGORY="selectedcategory";
	@Autowired
	private UseraccDao useraccDao;
	@Autowired
	private PhotoCategoryDao photoCategoryDao;
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private PhotoDao photoDao;
	@Autowired
	private ExifDataDao exifDataDao;
	@Autowired
	private MPhotoCategoryDao photoCategoryDaoMapper;
	
	private int IMAGE_EXIFDATA=1;
	private int IMAGE_CATEGORYDATA=1;
	
	private static final Logger logger=LoggerFactory.getLogger(ImageDataManager.class);
	
	private static final Map<String,File[]>sessionFiles=new HashMap<String,File[]>();
	
	
	
	public boolean checkForUploadedImages(String id){
		String uploadedDir=Constants.TEMPDIR+"/"+id+"/";
		File destDir=new File(uploadedDir);
		if(destDir.exists()){
			int l=destDir.listFiles().length;
			if(l>0){
				ZkUtil.messageBoxInfo("processing.files",new Object[]{l});
				return true;
			}
		}
		return false;
	}
	public UploadHandler processUploadedImages(User user,String id) throws Exception{
		Integer selectedCatId=(Integer)Executions.getCurrent().getSession().getAttribute(SELECTED_CATEGORY);
		return processUploadedImages(user,id,selectedCatId);
	}
	public UploadHandler processUploadedImages(User user,String id,Integer selectedCatId) throws Exception{
		Category selectedCatEntity=null;
		Useracc useracc=user.getUseracc();
		if(selectedCatId==null){
			List<Category> categories=categoryDao.findByUseracc(useracc);
			logger.debug("user already has categories");			
			for (Category category : categories) {
				if(category.getName().equals(DEFAULT_CATEGORY_NAME)){
					selectedCatEntity=category;
					break;
				}
			}
		}else{
			selectedCatEntity=categoryDao.findById(selectedCatId);
		}
		String uploadedDir=Constants.TEMPDIR+"/"+id+"/";
		try {
			Desktop desktop=Executions.getCurrent().getDesktop();
			File destDir=new File(uploadedDir);
			UploadHandler.setExifDataDao(getExifDataDao());
			UploadHandler.setPhotoCategoryDao(getPhotoCategoryDao());
			UploadHandler.setPhotoDao(getPhotoDao());
			UploadHandler uploadHandler=UploadHandler.getUploader(destDir,null, selectedCatEntity, useracc, id,desktop);
			return uploadHandler;
		} catch (Exception e) {
			logger.error("processUploadedImages",e);
			throw e;
		}
	}
	private ExifData fillExifData(Map<ExifDataEnum,String> exifData){
		ExifData exifDataObj=new ExifData();
		exifDataObj.setDateTimeOriginal(exifData.get(ExifDataEnum.DateTimeOriginal));
		exifDataObj.setExposureCompenstation(exifData.get(ExifDataEnum.ExposureCompensation));
		exifDataObj.setExposureProgram(exifData.get(ExifDataEnum.ExposureProgram));
		exifDataObj.setExposureTime(exifData.get(ExifDataEnum.ExposureTime));
		exifDataObj.setFocalLength(exifData.get(ExifDataEnum.FocalLength));
		exifDataObj.setFnumber(exifData.get(ExifDataEnum.FNumber)!=null?Double.parseDouble(exifData.get(ExifDataEnum.FNumber)):null);
		exifDataObj.setImageHeight(exifData.get(ExifDataEnum.ImageHeight)!=null?Integer.parseInt(exifData.get(ExifDataEnum.ImageHeight)):null);
		exifDataObj.setImageWidth(exifData.get(ExifDataEnum.ImageWidth)!=null?Integer.parseInt(exifData.get(ExifDataEnum.ImageWidth)):null);
		exifDataObj.setIso(exifData.get(ExifDataEnum.Iso)!=null?Integer.parseInt(exifData.get(ExifDataEnum.Iso)):null);
		exifDataObj.setLensInfo(exifData.get(ExifDataEnum.LensInfo));
		exifDataObj.setLensMake(exifData.get(ExifDataEnum.LensMake));
		exifDataObj.setLensModel(exifData.get(ExifDataEnum.LensModel));
		exifDataObj.setOrientationDesc(exifData.get(ExifDataEnum.Orientation));
		return exifDataObj;
	}
	public ImageDataModel[] listAlbumContentbyId(Category category, boolean exifData,boolean categoryData) throws Exception {
		return listAlbumContentbyId(category, exifData, categoryData, true);
	}
	public ImageDataModel[] listAlbumContentbyId(Category category, boolean exifData,boolean categoryData,boolean userData) throws Exception {
		ImageDataModel [] imageData=null;
		try{
			List<Photo> photoList=photoCategoryDao.findPhotosByCategoryId(category);
			imageData = extractImageModelList(photoList, exifData, categoryData, userData);
		}catch(Exception e){
			logger.error("listAlbumContent",e);
		}
		return imageData;
	}
	public MImageDataModel[] listAlbumContentbyId(MCategory category, boolean exifData,boolean categoryData,boolean userData) throws Exception {
		MImageDataModel [] imageData=null;
		try{
			List<MPhoto> photoList=photoCategoryDaoMapper.findPhotosByCategoryId(category.getId());
			imageData = extractMImageModelList(photoList, exifData, categoryData, userData);
		}catch(Exception e){
			logger.error("listAlbumContent",e);
		}
		return imageData;
	}

	public static ImageDataModel[] extractImageModelList(List<Photo> categoryList, boolean exifData,boolean categoryData)
			throws IOException {
		return extractImageModelList(categoryList, exifData, categoryData, true);
	}
	public static ImageDataModel[] extractImageModelList(List<Photo> categoryList, boolean exifData,boolean categoryData, boolean userData)
			throws IOException {
		ImageDataModel[] imageData=new ImageDataModel[categoryList.size()];
		for (int i = 0; i < categoryList.size(); i++) {
			imageData[i]=extractImageData(categoryList.get(i), exifData, categoryData, userData);
			
		}
		return imageData;
	}
	public static MImageDataModel[] extractMImageModelList(List<MPhoto> categoryList, boolean exifData,boolean categoryData, boolean userData)
			throws IOException {
		MImageDataModel[] imageData=new MImageDataModel[categoryList.size()];
		for (int i = 0; i < categoryList.size(); i++) {
			imageData[i]=extractImageData(categoryList.get(i), exifData, categoryData, userData);
			
		}
		return imageData;
	}	
	public static ImageDataModel extractImageData(Photo photo,
			boolean exifData,boolean categoryData)
			throws IOException {
		return extractImageData(photo, exifData, categoryData, true);
	}	
	public static ImageDataModel extractImageData(Photo photo,
			boolean exifData,boolean categoryData, boolean userData)
			throws IOException {
		String path=photo.getPath();
		String name=photo.getName();
//		Set<ExifData> exifDatas=photo.getExifData();
//		if(exifDatas!=null && exifDatas.size()>0){
//			data=exifDatas.iterator().next();
//		}
		ExifData data=null;
		if(exifData){
			data=photo.getExifData();
		}
		ImageDataModel imageData=buildImageDataModel(path,photo,name,data);		
		if(categoryData){
			
			Set <String> categoryNames=getCategoryNames(photo.getPhotoCategories());
			imageData.setCategoryNames(categoryNames);
		}
		if(userData){
			imageData.setUseracc(photo.getUseracc());			
		}else{
			photo.setUseracc(null);
		}
		return imageData; 
	}
	public static MImageDataModel extractImageData(MPhoto photo,
			boolean exifData,boolean categoryData, boolean userData)
			throws IOException {
		String path=photo.getPath();
		String name=photo.getName();
//		Set<ExifData> exifDatas=photo.getExifData();
//		if(exifDatas!=null && exifDatas.size()>0){
//			data=exifDatas.iterator().next();
//		}
		MExifData data=null;
		if(exifData){
			data=photo.getExifData();
		}
		MImageDataModel imageData=buildImageDataModel(path,photo,name,data);		
		if(categoryData){
			
			Set <String> categoryNames=getCategoryNames(photo.getPhotoCategories());
			imageData.setCategoryNames(categoryNames);
		}
		if(userData){
			imageData.setUseracc(photo.getUseracc());			
		}else{
			photo.setUseracc(null);
		}
		return imageData; 
	}	
	@Deprecated
	public List<Category> getPublicAlbums() {
		List<Category> categoryList=categoryDao.findPublicAlbums(-1);
		return categoryList;
	}
	public ImageDataModel [] getRandomRecommendedImages(int maxRandom) {
		List<PhotoCategory> categoryList=photoCategoryDao.findRecommendedRandomImages(maxRandom);
		ImageDataModel [] imageData=null;
		try{
			List<Photo> photos=new ArrayList<Photo>(categoryList.size());
			for (PhotoCategory photoCategory : categoryList) {
				photos.add(photoCategory.getPhoto());
			}
			imageData = extractImageModelList(photos,false,false);
		}catch(Exception e){
			logger.error("listAlbumContent",e);
		}
		return imageData;
	}
	
	public static ImageDataModel buildImageDataModel(String destPath,Photo photo, String name, ExifData exifData) throws IOException {
		ImageDataModel datamodelArray=new ImageDataModel();
		datamodelArray.setImageRealPath(destPath);
		datamodelArray.setExifDataObject(exifData);
		if(photo!=null){
			datamodelArray.setPhoto(photo);
			datamodelArray.setId(photo.getId());
		}
		datamodelArray.setImageName(name);
//		Page p=Executions.getCurrent().getDesktop().getPage("galeria");
//		Image thumbImage=new Image();
//		thumbImage.setPage(p);
		datamodelArray.setImageThumb(new MyZkImage(Constants.STOREDIR+Constants.THUMBPATH+destPath));
//		thumbImage.setContent(datamodelArray.getImageThumb());
//		thumbImage.setPage(null);
		
//		Image mediumImage=new Image();
//		mediumImage.setPage(p);
//		mediumImage.setContent(new MyZkImage(Constants.STOREDIR+Constants.MEDIUMPATH+destPath));
		datamodelArray.setImageMedium(new MyZkImage(Constants.STOREDIR+Constants.MEDIUMPATH+destPath));
//		mediumImage.setPage(null);
//		Image oriImage=new Image();
//		oriImage.setPage(p);
//		oriImage.setContent(new MyZkImage(Constants.STOREDIR+Constants.ORIGINALPATH+destPath));
		datamodelArray.setImageOri(new MyZkImage(Constants.STOREDIR+Constants.ORIGINALPATH+destPath));
		File f=new File(Constants.STOREDIR+Constants.ORIGINALPATH+destPath);
		datamodelArray.setOriSize((int)f.length());
		f=null;
//		oriImage.setPage(null);
		datamodelArray.setImagePreview(new MyZkImage(Constants.STOREDIR+Constants.PREVIEWPATH+destPath));
//		datamodelArray.setZkImageThumb(thumbImage);
//		datamodelArray.setZkImageMedium(mediumImage);
//		datamodelArray.setZkImageOri(oriImage);
//		datamodelArray.setImageName(name);
		return datamodelArray;
	}
	public static MImageDataModel buildImageDataModel(String destPath,MPhoto photo, String name, MExifData exifData) throws IOException {
		MImageDataModel datamodelArray=new MImageDataModel();
		datamodelArray.setImageRealPath(destPath);
		datamodelArray.setExifDataObject(exifData);
		if(photo!=null){
			datamodelArray.setPhoto(photo);
			datamodelArray.setId(photo.getId());
		}
		datamodelArray.setImageName(name);
//		Page p=Executions.getCurrent().getDesktop().getPage("galeria");
//		Image thumbImage=new Image();
//		thumbImage.setPage(p);
		datamodelArray.setImageThumb(new MyZkImage(Constants.STOREDIR+Constants.THUMBPATH+destPath));
//		thumbImage.setContent(datamodelArray.getImageThumb());
//		thumbImage.setPage(null);
		
//		Image mediumImage=new Image();
//		mediumImage.setPage(p);
//		mediumImage.setContent(new MyZkImage(Constants.STOREDIR+Constants.MEDIUMPATH+destPath));
		datamodelArray.setImageMedium(new MyZkImage(Constants.STOREDIR+Constants.MEDIUMPATH+destPath));
//		mediumImage.setPage(null);
//		Image oriImage=new Image();
//		oriImage.setPage(p);
//		oriImage.setContent(new MyZkImage(Constants.STOREDIR+Constants.ORIGINALPATH+destPath));
		datamodelArray.setImageOri(new MyZkImage(Constants.STOREDIR+Constants.ORIGINALPATH+destPath));
		File f=new File(Constants.STOREDIR+Constants.ORIGINALPATH+destPath);
		datamodelArray.setOriSize((int)f.length());
		f=null;
//		oriImage.setPage(null);
		datamodelArray.setImagePreview(new MyZkImage(Constants.STOREDIR+Constants.PREVIEWPATH+destPath));
//		datamodelArray.setZkImageThumb(thumbImage);
//		datamodelArray.setZkImageMedium(mediumImage);
//		datamodelArray.setZkImageOri(oriImage);
//		datamodelArray.setImageName(name);
		return datamodelArray;
	}	
	public int addToAlbum(Map<String,Object> images,Integer album){
		int count=0;
		Category category=categoryDao.findById(album);
		for (String imageId : images.keySet()) {
			PhotoCategory photoCategory=photoCategoryDao.findByCategoryAndPhoto(album, Integer.parseInt(imageId));
			if(photoCategory==null){
				Photo photo=photoDao.findById(Integer.parseInt(imageId));
				PhotoCategory pc=new PhotoCategory();
				pc.setPhoto(photo);
				pc.setCategory(category);
				photoCategoryDao.persist(pc);
				count++;
			}
		}
		return count;
	}
	public int moveToAlbum(Map<String,Object> images,Integer fromAlbum,Integer toAlbum){
		int count=0;
		if(!fromAlbum.equals(toAlbum)){
			Category toCategory=categoryDao.findById(toAlbum);
			for (String imageId : images.keySet()) {
				PhotoCategory photoCategory=photoCategoryDao.findByCategoryAndPhoto(toAlbum, Integer.parseInt(imageId));
				if(photoCategory==null){
					PhotoCategory fromCategory2=photoCategoryDao.findByCategoryAndPhoto(fromAlbum, Integer.parseInt(imageId));
					fromCategory2.setCategory(toCategory);
					photoCategoryDao.persist(fromCategory2);
					count++;
				}else{
					photoCategoryDao.remove(photoCategory);
					count++;
				}
			}
		}
		return count;
	}	
	public ImageDataModel getFullImageDataById(Integer photoId){
		List<PhotoCategory> photoCategory=photoCategoryDao.findByPhoto(photoId);
		Photo photo=photoCategory.get(0).getPhoto();
		ImageDataModel imageDataModel=null;
		try {
			imageDataModel = buildImageDataModel(photo.getPath(),photo,photo.getName(),null);
		} catch (IOException e) {
			logger.error("getFullImageDataById", e);
		}
		Set<String> categories=getCategoryNames(photoCategory);
		imageDataModel.setCategoryNames(categories);
		return imageDataModel;
		
	}
	private static Set<String> getCategoryNames(Iterable<PhotoCategory> photoCategory){
		Set<String> categories=new HashSet<String> ();
		for (PhotoCategory photoCategory2 : photoCategory) {
			categories.add(photoCategory2.getCategory().getDisplayName());
		}
		return categories;
	}
	public void deleteImages(Map<String,Object> images){
		Set<String> removed=new HashSet();
		for (String imageId : images.keySet()) {
			List<PhotoCategory> photoCategory=photoCategoryDao.findByPhoto(Integer.parseInt(imageId));
			if(photoCategory.size()>0){
				for (PhotoCategory photoCategory2 : photoCategory) {
					logger.debug("delete photo cat:"+photoCategory2.getId()+" name:"+photoCategory2.getCategory().getName());
					//categories.add(photoCategory2.getCategory().getName());
					photoCategoryDao.remove(photoCategory2);
				}
			}
			Photo ph=photoDao.findById(Integer.parseInt(imageId));
			exifDataDao.remove(ph.getExifData());
//			Set<ExifData> exifs=ph.getExifDatas();
//			for (ExifData exifData : exifs) {
//				exifDataDao.remove(exifData);
//			}
			photoDao.remove(ph);
			removed.add(imageId);
			File med=new File(Constants.STOREDIR+Constants.MEDIUMPATH+ph.getPath());
			if(!med.delete()){
				logger.error(" !!! can not delete "+med.getAbsolutePath());
			}
			File ori=new File(Constants.STOREDIR+Constants.ORIGINALPATH+ph.getPath());
			if(!ori.delete()){
				logger.error(" !!! can not delete "+ori.getAbsolutePath());
			}			
			File thumb=new File(Constants.STOREDIR+Constants.THUMBPATH+ph.getPath());
			if(!thumb.delete()){
				logger.error(" !!! can not delete "+thumb.getAbsolutePath());
			}			
		}
	}
	
	public Map<String,Object> removeFromAlbum(Map<String,Object> images,Integer album){
		Map<String,Object> unsuccess=new HashMap();
		Set<String> removed=new HashSet();
		int count=0;
		for (String imageId : images.keySet()) {
			List<PhotoCategory> photoCategory=photoCategoryDao.findByPhoto(Integer.parseInt(imageId));
			if(photoCategory.size()>1){
				for (PhotoCategory photoCategory2 : photoCategory) {
					if(photoCategory2.getCategory().getId().equals(album)){
						photoCategoryDao.remove(photoCategory2);
						removed.add(imageId);
						count++;
						break;
					}
				}
			}else{
				unsuccess.put(imageId,true);
			}
		}
		return unsuccess;
	}
	
	
	
	public UseraccDao getUseraccDao() {
		return useraccDao;
	}
	public void setUseraccDao(UseraccDao useraccDao) {
		this.useraccDao = useraccDao;
	}
	public PhotoCategoryDao getPhotoCategoryDao() {
		return photoCategoryDao;
	}
	public void setPhotoCategoryDao(PhotoCategoryDao photoCategoryDao) {
		this.photoCategoryDao = photoCategoryDao;
	}
	public CategoryDao getCategoryDao() {
		return categoryDao;
	}
	public void setCategoryDao(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}
	public PhotoDao getPhotoDao() {
		return photoDao;
	}
	public void setPhotoDao(PhotoDao photoDao) {
		this.photoDao = photoDao;
	}

	public ExifDataDao getExifDataDao() {
		return exifDataDao;
	}

	public void setExifDataDao(ExifDataDao exifDataDao) {
		this.exifDataDao = exifDataDao;
	}
	
	
}
