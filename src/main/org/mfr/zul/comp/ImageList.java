package org.mfr.zul.comp;

import org.mfr.data.ImageDataModel;
import org.zkoss.zk.ui.HtmlMacroComponent;

public class ImageList extends HtmlMacroComponent {
	ImageDataModel [] imageData=null;
	boolean showTitle=false;
	boolean showAuthor=false;
	String className="gallery";
	boolean allowDownload=false;
	@Override
	protected void compose() {
		setDynamicProperty("imageData", imageData);
		setDynamicProperty("showTitle", showTitle);
		setDynamicProperty("className", className);
		setDynamicProperty("showAuthor", showAuthor);
		setDynamicProperty("allowDownload", allowDownload);
		super.compose();
	}	
	public ImageDataModel[] getImageData() {
		return imageData;
	}
	public void setImageData(ImageDataModel[] imageData) {
		this.imageData = imageData;
	}
	public boolean isShowTitle() {
		return showTitle;
	}
	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public boolean isShowAuthor() {
		return showAuthor;
	}
	public void setShowAuthor(boolean showAuthor) {
		this.showAuthor = showAuthor;
	}
	public boolean isAllowDownload() {
		return allowDownload;
	}
	public void setAllowDownload(boolean allowDownload) {
		this.allowDownload = allowDownload;
	}

}
