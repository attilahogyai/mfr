package org.mfr.web.action;

import java.io.File;
import java.util.List;

import org.mfr.data.Photo;
import org.mfr.data.PhotoDao;
import org.mfr.image.ImageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Textbox;

public class AdminComposer extends AbstractSelectorComposer {
	private static final Logger logger=LoggerFactory.getLogger(ImageHandlerComposer.class);
	@WireVariable
	private PhotoDao photoDao;
	@Wire
	private Textbox photoQuery;
	@Wire
	private Textbox command;
	public List<Photo> getPhotoSize(){
		return photoDao.getPhotos(photoQuery.getText());
	}
	@Listen("onClick = #ok")
	public void processCommand(){
		String c=command.getText();
		if(c.equals("size")){
			List<Photo> photos=getPhotoSize();
			for (Photo photo : photos) {
				String path=ImageConfig.ORIGINALCONFIG.getPath(photo.getPath());
				File f=new File(path);
				if(f.exists()){
					logger.debug(f.toString()+" l:"+f.length());
					photo.setSize((int)f.length());
				}else{
					photo.setSize(0);
				}
				photoDao.merge(photo);
			}
		}
	}
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		checkAdmin();
		super.doAfterCompose(comp);
	}
	
}
