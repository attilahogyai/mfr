package org.mfr.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.FileUtils;
import org.im4java.core.IM4JavaException;
import org.mfr.data.Category;
import org.mfr.data.Constants;
import org.mfr.data.ExifData;
import org.mfr.data.ExifDataDao;
import org.mfr.data.ImageDataModel;
import org.mfr.data.Photo;
import org.mfr.data.PhotoCategory;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.data.PhotoDao;
import org.mfr.data.Useracc;
import org.mfr.image.ImageConfig;
import org.mfr.image.ImageTools;
import org.mfr.web.UploadedFileDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Label;

public class UploadHandler implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(UploadHandler.class);

	public Queue<List<UploadedFileDescriptor>> filesQueue = new ConcurrentLinkedQueue<List<UploadedFileDescriptor>>();

	private int total = 0;

	private static SimpleDateFormat[] sdfArray = new SimpleDateFormat[] { new SimpleDateFormat("yyyy:MM:dd HH:mm:ss"),
			new SimpleDateFormat("yyyy.MM.dd HH:mm:ss"), new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss"),
			new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
			new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"), new SimpleDateFormat("MM.dd.yyyy HH:mm:ss"),
			new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"), new SimpleDateFormat("dd.MM.yyyy HH:mm:ss") };

	private static PhotoDao photoDao;
	private static PhotoCategoryDao photoCategoryDao;
	private static ExifDataDao exifDataDao;

	private File destDir;

	private Useracc useracc;
	private String id;
	private static final Map<String, List<UploadedFileDescriptor>> uploadFilesMap = Collections
			.synchronizedMap(new HashMap<String, List<UploadedFileDescriptor>>());
	private static final Map<String, Integer> counterMap = Collections.synchronizedMap(new HashMap<String, Integer>());
	private static final Map<String, Desktop> desktopMap = Collections.synchronizedMap(new HashMap<String, Desktop>());
	private static final Map<String, UploadHandler> uploadHandlersMap = new HashMap<String, UploadHandler>();

	public static void setDesktop(String id, Desktop desktop,boolean update) {
		if((desktopMap.get(id)!=null && update) || !update){
			if(desktop!=null){
				logger.debug("set desktop start");
				if (!desktop.isServerPushEnabled()) {
					desktop.enableServerPush(true);
				}
				logger.debug("put desktop in map");
				desktopMap.put(id, desktop);
				logger.debug("set desktop end");
			}
		}
	}

	public synchronized static UploadHandler getUploader(File destDir,List<UploadedFileDescriptor>fileDesc, Category defaultCat, Useracc useracc, String id,
			Desktop desktop) {
		UploadHandler uh = uploadHandlersMap.get(id);
		logger.debug("old uploader [" + uh + "]");
		if (uh == null || uh.filesQueue.peek() == null) {
			uh = new UploadHandler(destDir,useracc, id);
			if(fileDesc!=null){
				uh.refreshState(desktop,fileDesc); // fill by file desc
			}else{
				uh.refreshState(desktop,defaultCat);  // fill by directory list
			}
			uploadHandlersMap.put(id, uh);
			logger.debug("create a new one [" + uh + "]");
			new Thread(uh).start();
		} else {
			logger.debug("refresh the state");
			if(fileDesc!=null){
				uh.refreshState(desktop,fileDesc);
			}else{
				uh.refreshState(desktop,defaultCat);
			}
		}
		return uh;
	}

	private UploadHandler(File destDir, Useracc useracc, String id) {
		this.destDir = destDir;
		this.useracc = useracc;
		this.id = id;
	}

	private synchronized void refreshState(Desktop desktop,Category cat) {
		List<UploadedFileDescriptor> uploadFiles = getUploadFiles();
		final List<UploadedFileDescriptor> uploadFilesFinal = uploadFiles;
		File[] fileList = destDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File arg0) {
				UploadedFileDescriptor check=new UploadedFileDescriptor(arg0.getName(),null);
				return !uploadFilesFinal.contains(check);
			}
		});
		if (fileList.length > 0) {
			List<UploadedFileDescriptor> uploadFilesArray=convertToFileDescriptor(fileList,cat);
			refreshState(desktop,uploadFilesArray);
		}
		setDesktop(id, desktop,false);
	}

	private List<UploadedFileDescriptor> getUploadFiles() {
		List<UploadedFileDescriptor> uploadFiles = uploadFilesMap.get(id);
		if (uploadFiles == null) {
			uploadFiles = Collections.synchronizedList(new ArrayList<UploadedFileDescriptor>());
			uploadFilesMap.put(id, uploadFiles);
		}
		return uploadFiles;
	}
	private synchronized void refreshState(Desktop desktop,List<UploadedFileDescriptor> uploadFilesArray) {
		List<UploadedFileDescriptor> uploadFiles = getUploadFiles();
		filesQueue.add(uploadFilesArray);
		logger.debug("current fileList count [" + uploadFilesArray.size()+ "]");
		uploadFiles.addAll(uploadFilesArray);
		setDesktop(id, desktop,false);
	}
	
	private List<UploadedFileDescriptor> convertToFileDescriptor(File[] files,Category cat){
		List<UploadedFileDescriptor> ufda=new ArrayList<UploadedFileDescriptor>(files.length);
		for (int i=0;i<files.length;i++) {
			ufda.add(i,new UploadedFileDescriptor(files[i].getName(),null));
			ufda.get(i).setFilePath(files[i].getAbsolutePath());
			ufda.get(i).setCategory(cat);
		}
		return ufda;
	}

	private void setUploaderLabel(final String inputLabel,final boolean done) {
		logger.debug("setUploaderLabel start");
		Page page = null;
		if (desktopMap.get(id) == null) {
			logger.debug("desktop is null return");
			return;
		}
		logger.debug("get Desktop");
		Desktop dekstop = desktopMap.get(id);
		page = dekstop.getFirstPage();
		logger.debug("Desktop["+dekstop+"] page["+page+"]");
		if (page != null) {
			Component include = page.getFellowIfAny("inc_header");
			if (include != null) {
				final Label uploaderLabel = (Label) include.getFellowIfAny("uploaderLabel");
				
				final A refreshLink = (A) page.getFellowIfAny("refreshButton");
				EventListener el = new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						try{
							String id = event.getName();
							String label = null;
							Integer counter=counterMap.get(id);
							if(inputLabel==null && counter!=null){
								label = Labels.getLabel("upload.process",
										new String[] { Integer.toString(counterMap.get(id)),
												Integer.toString(uploadFilesMap.get(id).size()) });
							}else{
								label=inputLabel;
							}
							if (label==null || label.equals("")) {
								uploaderLabel.setZclass("z-label");
							} else {
								uploaderLabel.setZclass("upload-process");
							}
							uploaderLabel.setValue(label);
							
							if(done && refreshLink!=null){
								Events.postEvent("onClick",refreshLink,null);	
							}
						}catch(Exception e){
							logger.error("setUploaderLabel",e);
						}
					}
				};
				Executions.schedule(dekstop, el, new Event(id));
			} else {
				logger.error("UPLOADERLABEL is null");
			}
		}
	}

	@Override
	public void run() {
		List<ImageDataModel> uploadedImages = new ArrayList<ImageDataModel>();
		try {
			counterMap.put(id, 0);
			int counter = 0;
			do {
				List<UploadedFileDescriptor> fileList = null;
				synchronized (this) {
					fileList = filesQueue.peek();
				}
				for (int i = 0; i < fileList.size(); i++) {
					try {
						ImageDataModel images[] = processUploadedImages(useracc.getLogin() + "/" + id,
								new UploadedFileDescriptor[]{ fileList.get(i)});
						Photo p = new Photo();
						uploadedImages.add(images[0]);
						p.setProvider(fileList.get(i).getProvider());	
						if(p.getProvider()>0){
							p.setProviderUrl(fileList.get(i).getFilePath());
							p.setProviderPath(fileList.get(i).getName());
						}
						p.setName(images[0].getImageName());
						p.setOwner(useracc.getLogin());
						p.setUseracc(useracc);
						p.setImportDate(new Date());
						p.setPath(images[0].getImageRealPath());
						p.setSize(images[0].getOriSize());
						if (images[0].getExifDataObject().getDateTimeOriginal() != null) {
							for (SimpleDateFormat simpledateformat : sdfArray) {
								try {
									p.setTaken(simpledateformat.parse(images[0].getExifDataObject()
											.getDateTimeOriginal()));
									break;
								} catch (Exception e) {
								}
							}
							if (p.getTaken() == null) {
								logger.warn("unable to parse TAKEN TIME");
							}
						}

						photoDao.persist(p);
						PhotoCategory pc = new PhotoCategory();
						pc.setPhoto(p);
						pc.setCategory(fileList.get(i).getCategory());
						photoCategoryDao.persist(pc);
						ExifData exifData = images[0].getExifDataObject();
						exifData.setPhoto(p);
						exifDataDao.merge(exifData);

						Integer count = 0;
						synchronized (counterMap) {
							count = counterMap.get(id);
							count++;
							counterMap.put(id, count);
						}
						setUploaderLabel(null,false);
						Thread.yield();
					} catch (Exception e) {
						logger.error("run", e);
					}
				}
				setUploaderLabel(Labels.getLabel("upload.process.done",
						new String[] { Integer.toString(counterMap.get(id)) }),false);
				Thread.sleep(2000);
				setUploaderLabel("",true);
				
				fileList = filesQueue.poll();
			} while (filesQueue.peek() != null);
		} catch (Exception e) {
			logger.error("UploadHandler error", e);
		} finally {
			synchronized (UploadHandler.class) {
				logger.debug("cleanup maps");
				uploadFilesMap.remove(id);
				counterMap.remove(id);
				Desktop desktop=desktopMap.remove(id);
				if(desktop!=null){
					desktop.enableServerPush(false);
				}
				uploadHandlersMap.remove(id);
			}
		}

	}

	public ImageDataModel[] processUploadedImages(String targetDir, UploadedFileDescriptor[] fileList) throws Exception {
		File target = new File(Constants.STOREDIR + Constants.ORIGINALPATH + targetDir);
		if (!target.exists()) {
			target.mkdirs();
		}
		ImageDataModel datamodelArray[] = new ImageDataModel[fileList.length];
		String filenames[] = new String[datamodelArray.length];
		for (int i = 0; i < fileList.length; i++) {
			File storeLocation = null;
			
			// chnage extension to uppercase
			int extStart=fileList[i].getName().lastIndexOf(".");
			if(extStart>-1){
				String ext=fileList[i].getName().substring(extStart);
				fileList[i].setName(fileList[i].getName().substring(0,extStart)+ext.toUpperCase());
			}
			
			if(fileList[i].getInputStream()!=null){ // write from input stream
				storeLocation = new File(target.getAbsolutePath() + "/" + fileList[i].getName());
				logger.debug("store from input stream " + fileList[i].getName());
				writeFile(fileList[i].getInputStream(),storeLocation);
			}else{
				File actFile=new File(fileList[i].getFilePath());
				String newFilename = ImageTools.calcNewFileName(target.getAbsolutePath() + "/", fileList[i].getName());
				storeLocation = new File(target.getAbsolutePath() + "/" + newFilename);
				
				try{
					FileUtils.moveFile(actFile, storeLocation);
					actFile = storeLocation;
				}catch(IOException e){
					logger.error("ERROR rename " + actFile.getAbsolutePath() + " to " + storeLocation.getAbsolutePath(),e);
					continue;
				}
			}
			filenames[i] = storeLocation.getAbsolutePath();
			fileList[i].setFilePath(storeLocation.getAbsolutePath()); // move file to original file path
			
			fileList[i].setName(storeLocation.getName());
		}
		logger.debug("original files saved [" + filenames.length + "]");
		
		for (int i = 0; i < fileList.length; i++) {
			String destPath = fileList[i].getFilePath();
			logger.debug("process file [" + destPath + "]");
			ExifData exifData = ImageTools.exifTool(destPath);
			
			String orientation=exifData.getOrientationDesc();
			if(orientation!=null){
				if(orientation.indexOf("Horizontal")>-1){
					
				}else if(orientation.indexOf(" 90 ")>-1){
					ImageTools.rotateSingleImage(ImageConfig.ORIGINALCONFIG, 90D, filenames[i]);
				}else if(orientation.indexOf(" 270 ")>-1){
					ImageTools.rotateSingleImage(ImageConfig.ORIGINALCONFIG, 270D, filenames[i]);
				}
			}
			datamodelArray[i] = ImageDataManager.buildImageDataModel(targetDir + "/" + fileList[i].getName(), null,
					fileList[i].getName(), exifData);
		}
		
		resizeImages(targetDir, filenames);

		return datamodelArray;
	}
	
	private void writeFile(InputStream is,File filePath){
		InputStream inStream = null;
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(filePath);
			byte[] buffer=new byte[10000];
			int c;
			while ((c = is.read(buffer)) != -1) {
				outStream.write(buffer,0,c);
				outStream.flush();
			}
			
		}catch (Exception e){
			logger.error("writeFile",e);
		}finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {}
			}
			if (outStream != null) {
				try {
					outStream.flush();
					outStream.close();
				} catch (IOException e) {}
			}
		}
	}
	

	public static void resizeImages(String targetDir, String[] filenames) throws IOException, InterruptedException,
			IM4JavaException {
		File medium = new File(Constants.STOREDIR + Constants.MEDIUMPATH + targetDir);
		medium.mkdirs();
		ImageTools.resizeImages(ImageConfig.MEDIUMCONFIG, medium, true, filenames);

		File preview = new File(Constants.STOREDIR + Constants.PREVIEWPATH + targetDir);
		preview.mkdirs();
		ImageTools.resizeImages(ImageConfig.PREVIEWCONFIG, preview, false,filenames);

		logger.debug("medium files saved [" + filenames.length + "]");
		File thumb = new File(Constants.STOREDIR + Constants.THUMBPATH + targetDir);
		thumb.mkdirs();
		ImageTools.resizeImages(ImageConfig.THUMBCONFIG, thumb, false,filenames);
		logger.debug("thumb files saved [" + filenames.length + "]");
	}

	public PhotoDao getPhotoDao() {
		return photoDao;
	}

	public static void setPhotoDao(PhotoDao photoDao) {
		UploadHandler.photoDao = photoDao;
	}

	public PhotoCategoryDao getPhotoCategoryDao() {
		return photoCategoryDao;
	}

	public static void setPhotoCategoryDao(PhotoCategoryDao photoCategoryDao) {
		UploadHandler.photoCategoryDao = photoCategoryDao;
	}

	public ExifDataDao getExifDataDao() {
		return exifDataDao;
	}

	public static void setExifDataDao(ExifDataDao exifDataDao) {
		UploadHandler.exifDataDao = exifDataDao;
	}

}
