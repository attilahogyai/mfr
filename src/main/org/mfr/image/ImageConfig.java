package org.mfr.image;

import org.mfr.data.Constants;

public enum ImageConfig {
	THUMBCONFIG(60, null, 110, Constants.THUMBPATH, ".JPG",false), 
	PREVIEWCONFIG(60, 460,460, Constants.PREVIEWPATH, ".JPG",false), 
	MEDIUMCONFIG(85, 1366, 768,Constants.MEDIUMPATH, ".JPG",false), 
	ORIGINALCONFIG(85, null, null,Constants.ORIGINALPATH, ".JPG",false),
	PNGCONFIG(85, null, null,Constants.PNGPATH, ".PNG",true),
	WORKPNGCONFIG(85, null, null,Constants.WORKPNGPATH, ".PNG",true),
	TMPCONFIG(85, 1366, 768,Constants.TMPPATH, ".JPG",true);
	double quality = 0;
	Integer width = null;
	Integer height = null;
	String storePath = null;
	String ext=null;
	boolean replaceExt;

	ImageConfig(double quantity, Integer width, Integer height, String storePath,String extension,boolean ext) {
		this.width = width;
		this.height = height;
		this.quality = quantity;
		this.storePath = Constants.STOREDIR + storePath;
		this.ext = extension;
		this.replaceExt = ext;
	}

	public String getPath(String path) {
		if(replaceExt){
			path=ImageTools.convertFileExtension(path, ext);
		}
		return storePath + path;
	}
}
