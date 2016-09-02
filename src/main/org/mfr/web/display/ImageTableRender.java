package org.mfr.web.display;

import org.mfr.data.ImageDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Vbox;

public class ImageTableRender implements RowRenderer {
	private static final Logger logger=LoggerFactory.getLogger(ImageTableRender.class);
	@Override
	public void render(Row row, Object data, int arg2) throws Exception {
		ImageDataModel rowData = (ImageDataModel) data;
		Image img=new Image();
		img.setContent(rowData.getImageThumb());
		img.setClass("row-image");
		row.appendChild(img);
		
		Vbox vb=new Vbox();
		vb.appendChild(new Label(rowData.getImageName()));
		if(rowData.getExifDataObject()!=null){
			vb.appendChild(new Label(rowData.getExifDataObject().getImageWidth()+" x "+rowData.getExifDataObject().getImageHeight()));
			vb.appendChild(new Label("f"+rowData.getExifDataObject().getFnumber()+"/"+rowData.getExifDataObject().getExposureTime()+" iso:"+rowData.getExifDataObject().getIso()));
			vb.appendChild(new Label(rowData.getExifDataObject().getFocalLength()+"/"+rowData.getExifDataObject().getLensInfo()));
		}
		row.appendChild(vb);
	}
	
}
