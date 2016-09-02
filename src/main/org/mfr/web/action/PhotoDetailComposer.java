package org.mfr.web.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mfr.data.ImageDataModel;
import org.mfr.data.Photo;
import org.mfr.data.PhotoDao;
import org.mfr.image.ImageConfig;
import org.mfr.image.ImageFile;
import org.mfr.image.ImageTools;
import org.mfr.image.MyZkImage;
import org.mfr.util.ZkUtil;
import org.zkforge.ckez.CKeditor;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.A;
import org.zkoss.zul.Image;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class PhotoDetailComposer extends SelectorComposer<Window> {
	private static final Log logger = LogFactory.getLog(PhotoDetailComposer.class);
	@Wire
	private Window photoDetail;
	@Wire
	private CKeditor desc;
	@Wire
	private Textbox name;
	@WireVariable
	private PhotoDao photoDao;
	@Wire
	private A normalize;
	

	ImageDataModel imageData = (ImageDataModel) Executions.getCurrent().getArg()
			.get("imageData");
	Map exifData=null;
	@Wire
	Image image;
	/**
	 * 
	 */
	private static final long serialVersionUID = -5986723455522530000L;

	
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		Photo photo=imageData.getPhoto();
		if (photo != null) {
			String nameT=photo.getName();
			name.setValue(nameT);
			desc.setVisible(false);
			desc.setValue(photo.getDescription()!=null?photo.getDescription():"");
			ImageFile f=new ImageFile(ImageConfig.TMPCONFIG.getPath(photo.getPath()));
			if(!f.exists()){
				normalize.setLabel(Labels.getLabel("normalize"));
				image.setContent(imageData.getImageMedium());
			}else{
				image.setContent(new MyZkImage(ImageConfig.TMPCONFIG.getPath(photo.getPath())));
				normalize.setLabel(Labels.getLabel("undo"));
			}
			f.close();
			f=null;
		}
	}	

	@Listen("onClick = #photoSaveButton")
	public void update() {
		try {
			if (name.isValid()) {
				imageData.getPhoto().setName(name.getValue());
				logger.debug("desc2:"+desc);
				logger.debug("desc:"+desc.getValue());
				imageData.getPhoto().setDescription(desc.getValue());
				photoDao.merge(imageData.getPhoto());
				name.setRawValue(null);
				desc.setValue(null);
				Messagebox.show(Labels.getLabel("photoDetail.saved"),
						Labels.getLabel("information"), Messagebox.OK,
						Messagebox.INFORMATION);
				ImageFile f=new ImageFile(ImageConfig.TMPCONFIG.getPath(imageData.getPhoto().getPath()));
				if(f.exists()){
					ImageTools.finishChanges(null,imageData.getPhoto().getPath());
				}
				f.close();
				f=null;
			}
			closeWindow();
		} catch (Exception e) {
			org.mfr.util.ZkUtil.showProcessError(e);
		}
	}
	public void normalizeImage(){
		try {
			ImageFile tempF=new ImageFile(ImageConfig.TMPCONFIG.getPath(imageData.getPhoto().getPath()));
			if(!tempF.exists()){
				ImageTools.normalizeImage(ImageConfig.WORKPNGCONFIG,imageData.getPhoto().getPath());
				ImageTools.resizeImagesFromWORKPNG(ImageConfig.TMPCONFIG, imageData.getPhoto().getPath());
				image.setContent(new MyZkImage(ImageConfig.TMPCONFIG.getPath(imageData.getPhoto().getPath())));
				normalize.setLabel(Labels.getLabel("undo"));
			}else{
				tempF.setWritable(true);
				boolean result=tempF.delete();
				if(result){
					image.setContent(imageData.getImageMedium());
					normalize.setLabel(Labels.getLabel("normalize"));
				}
			}
			tempF.close();
			tempF=null;
		} catch (Exception e) {
			ZkUtil.messageBoxError("image.process.error");
		}
	}
	void closeWindow() {
		Events.postEvent("onClose", photoDetail, null);
	}

	public PhotoDao getPhotoDao() {
		return photoDao;
	}

	public void setPhotoDao(PhotoDao photoDao) {
		this.photoDao = photoDao;
	}

	public Map getExifData() {
		return exifData;
	}

	public void setExifData(Map exifData) {
		this.exifData = exifData;
	}

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent,
			ComponentInfo compInfo) {
		Photo photo=imageData.getPhoto();
		exifData=ImageTools.getExifData(photo.getExifData());
		return super.doBeforeCompose(page, parent, compInfo);
	}	
	
	
}
