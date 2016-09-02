package org.mfr.web.display;

import org.mfr.data.ImageDataModel;
import org.mfr.image.HtmlOutZulImage;
import org.mfr.image.NoHtmlOutZulImage;
import org.mfr.manager.ImageDataManager;
import org.zkoss.spring.SpringUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;

public class IndexHelper {
	public static void renderRandomImages(Div galleryHidden){
		ImageDataModel[] imageData = getRandomImages(6);
		try{		
			for(int i=0;i<imageData.length;i++){
				HtmlOutZulImage imageMedium=new HtmlOutZulImage();
				galleryHidden.appendChild(imageMedium);
				imageMedium.setContent(imageData[i].getImagePreview());
				
			}
		}catch(Exception e){
			Messagebox.show(Labels.getLabel("image.process.error")+"["+e.getMessage()+"]");
		} 

	}
	public static ImageDataModel[] getRandomImages(int count) {
		ImageDataManager imageDataManager=(ImageDataManager)SpringUtil.getBean("imageDataManager");
		ImageDataModel [] imageData=null;
		
		imageData=imageDataManager.getRandomRecommendedImages(count);
		return imageData;
	}
	
}
