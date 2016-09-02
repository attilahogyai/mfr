package org.mfr.manager;

import java.io.File;
import java.util.Iterator;

import org.mfr.data.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class BackgroundResizeHandler extends Thread{
	private static final Logger logger=LoggerFactory.getLogger(BackgroundResizeHandler.class);
	@Override
	public void run() {
		File f=new File(Constants.STOREDIR+"/update");
		if(f.isFile()){
			logger.info("start resize images");
			File[] files=new File(Constants.STOREDIR+Constants.ORIGINALPATH).listFiles();
			for (int i = 0; i < files.length; i++) {
				File dir = files[i];
				if(dir.isDirectory()){
					File[] sessionDirs=dir.listFiles();
					for (int j = 0; j < sessionDirs.length; j++) {
						File[] fileToresize=sessionDirs[j].listFiles();
						logger.info("start bg resize for dir ["+sessionDirs[j]+"] files count["+fileToresize.length+"]");					
						String[] fileNamesToresize=new String[fileToresize.length];
						for (int k = 0; k < fileNamesToresize.length; k++) {
							fileNamesToresize[k]=fileToresize[k].getAbsolutePath();
						}
						try {
							UploadHandler.resizeImages(dir.getName()+"/"+sessionDirs[j].getName(), fileNamesToresize);
						} catch (Exception e) {
							logger.error("error bg resize in dir["+dir+"]",e);
						}
					}
				}
			}
			if(!f.delete()){
				logger.error("update file DELETE ERROR");
			}
		}

	}
	
}
