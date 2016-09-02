package org.mfr.data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mfr.image.ExifDataEnum;
import org.mfr.util.HttpHelper;
import org.mfr.data.ExifData;
import org.zkoss.zul.Image;


public class ImageDataModel{
	private Photo photo;
	private Integer id;
	private String imageRealPath;
	private String imageName;
	private Useracc useracc;
	private Map<ExifDataEnum,String> exifData;
	private Set<ExifDataEnum> exifDataSet;
	private Image zkImageThumb;
	private Image zkImageOri;
	private Image zkImageMedium;
	private Image zkImagePreview;
	private org.zkoss.image.Image imageThumb;
	private org.zkoss.image.Image imageOri;
	private org.zkoss.image.Image imageMedium;
	private org.zkoss.image.Image imagePreview;
	private Set<String> categoryNames=new HashSet<String>();
	private Integer oriSize;
	
	private ExifData exifDataObject;
	
	public boolean isOwner(){
		User user=HttpHelper.getUser();
		if(user!=null){
			return useracc.getId().equals(user.getUseracc().getId());
		}
		return false;	}
	
	public String getImageRealPath() {
		return imageRealPath;
	}
	public void setImageRealPath(String imageRealPath) {
		this.imageRealPath = imageRealPath;
	}
	public Map<ExifDataEnum,String> getExifData() {
		return exifData;
	}
	public void setExifData(Map<ExifDataEnum, String> exifData) {
		this.exifData = exifData;
	}
	public Set<ExifDataEnum> getExifDataSet() {
		return exifDataSet;
	}
	public void setExifDataSet(Set<ExifDataEnum> exifDataSet) {
		this.exifDataSet = exifDataSet;
	}
	public ExifData getExifDataObject() {
		return exifDataObject;
	}
	public void setExifDataObject(ExifData exifDataObject) {
		this.exifDataObject = exifDataObject;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public Image getZkImageThumb() {
		return zkImageThumb;
	}
	public void setZkImageThumb(Image zkImageThumb) {
		this.zkImageThumb = zkImageThumb;
	}
	public Image getZkImageOri() {
		return zkImageOri;
	}
	public void setZkImageOri(Image zkImageOri) {
		this.zkImageOri = zkImageOri;
	}
	public Image getZkImageMedium() {
		return zkImageMedium;
	}
	public void setZkImageMedium(Image zkImageMedium) {
		this.zkImageMedium = zkImageMedium;
	}
	public org.zkoss.image.Image getImageThumb() {
		return imageThumb;
	}
	public void setImageThumb(org.zkoss.image.Image imageThumb) {
		this.imageThumb = imageThumb;
	}
	public org.zkoss.image.Image getImageOri() {
		return imageOri;
	}
	public void setImageOri(org.zkoss.image.Image imageOri) {
		this.imageOri = imageOri;
	}
	public org.zkoss.image.Image getImageMedium() {
		return imageMedium;
	}
	public void setImageMedium(org.zkoss.image.Image imageMedium) {
		this.imageMedium = imageMedium;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Set<String> getCategoryNames() {
		return categoryNames;
	}
	public void setCategoryNames(Set<String> categoryNames) {
		this.categoryNames = categoryNames;
	}
	public Image getZkImagePreview() {
		return zkImagePreview;
	}
	public void setZkImagePreview(Image zkImagePreview) {
		this.zkImagePreview = zkImagePreview;
	}
	public org.zkoss.image.Image getImagePreview() {
		return imagePreview;
	}
	public void setImagePreview(org.zkoss.image.Image imagePreview) {
		this.imagePreview = imagePreview;
	}
	public Useracc getUseracc() {
		return useracc;
	}
	public void setUseracc(Useracc owner) {
		this.useracc = owner;
	}
	public Photo getPhoto() {
		return photo;
	}
	public void setPhoto(Photo photo) {
		this.photo = photo;
	}
	public Integer getOriSize() {
		return oriSize;
	}
	public void setOriSize(Integer oriSize) {
		this.oriSize = oriSize;
	}

}
