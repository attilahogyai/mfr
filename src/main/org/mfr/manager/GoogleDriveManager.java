package org.mfr.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.ExifDataDao;
import org.mfr.data.PhotoCategory;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.data.PhotoDao;
import org.mfr.data.User;
import org.mfr.data.UseraccDataDao;
import org.mfr.manager.oauth.DocumentList;
import org.mfr.manager.oauth.IOAuthService;
import org.mfr.util.HttpHelper;
import org.mfr.web.UploadedFileDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.google.gdata.data.Link;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.util.ServiceException;

public class GoogleDriveManager implements IFilesService {
	public static final int PROVIDERID=1;
	@Autowired
	private PhotoDao photoDao;
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private PhotoCategoryDao photoCategoryDao;
	@Autowired
	private ExifDataDao exifDataDao;
	
	private static final Logger logger = LoggerFactory
			.getLogger(GoogleDriveManager.class);
	private static final String googleDefaultAlbum="Google-main";

	private static final String DRIVEENDPOINT = "https://www.googleapis.com/drive/v2/files";

	private DocumentList documentList = null;
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private IOAuthService googleOAuthService;
	
	@Override
	public Map<String, Object> syncFiles(User user,boolean full) {
		
		DocumentListFeed docList = null;
		try {
			documentList = new DocumentList("myfotoroom.com");
			documentList.getDocsService().setAuthSubToken(user.getGoogleAccessToken());
			try{
				docList = documentList.getDocsListFeed("all");
			}catch(com.google.gdata.util.AuthenticationException autEx){
				logger.warn("syncFiles accessToken has expired refresh");
				String accessToken=googleOAuthService.requestForAccessToken(user.getGoogleRefreshToken());
				user.setGoogleAccessToken(accessToken);
				docList = documentList.getDocsListFeed("all");
				userManager.updateUseraccData(user.getLoginName(), UseraccDataDao.GOOGLE_ACCESS_TOKEN, user.getGoogleAccessToken());
				documentList.getDocsService().setAuthSubToken(user.getGoogleAccessToken());
			}
			Map<Integer,List<UploadedFileDescriptor>>categorySet=new HashMap<Integer,List<UploadedFileDescriptor>>();
			for (DocumentListEntry docEntry : docList.getEntries()) {
				UploadedFileDescriptor desc=handleDocumentEntry(docEntry,user);
				if(desc!=null){
					List<UploadedFileDescriptor> fileList=categorySet.get(desc.getCategory().getId());
					if(fileList==null){
						fileList=new ArrayList<UploadedFileDescriptor>();
						categorySet.put(desc.getCategory().getId(), fileList);
					}
					fileList.add(desc);
				}
			}
			Desktop desktop=Executions.getCurrent().getDesktop();
			for (Integer catId : categorySet.keySet()) {
				UploadHandler.setExifDataDao(getExifDataDao());
				UploadHandler.setPhotoCategoryDao(getPhotoCategoryDao());
				UploadHandler.setPhotoDao(getPhotoDao());
				UploadHandler uploadHandler=UploadHandler.getUploader(null,categorySet.get(catId), categorySet.get(catId).get(0).getCategory(), user.getUseracc(), user.getSessionId(),desktop);

			}
			
		} catch (Exception e) {
			logger.error("listFiles", e);
		}
		return null;
	}

	public UploadedFileDescriptor handleDocumentEntry(DocumentListEntry doc,User user) {
		if(doc.getType()!=null && doc.getType().equals("file") && HttpHelper.isImageExt(doc.getFilename())){
			logger.debug("this is an image sync that ["+doc.getFilename()+"]");
		
			String albumName="";
			for (Link link : doc.getParentLinks()) {
				albumName+=link.getTitle()+"/";
			}
			
			if(albumName.equals("")){
				albumName=googleDefaultAlbum;
			}else{
				albumName=albumName.substring(0, albumName.length()-1);
			}
			Category cat=categoryDao.findCategoryByUserAndPath(user.getUserId(), albumName);
			if(cat==null){  // if the category doesnt exist erlier than we can sure that the photo doesnt exist
				// create google category
				cat=new Category();
				cat.setOwner(user.getEmail());
				cat.setUseracc(user.getUseracc());
				cat.setDescription(albumName+" on Google drive.");
				cat.setName(albumName);
				cat.setPath(albumName);
				cat.setProvider(PROVIDERID);
				categoryDao.persist(cat);
				return setupFileDescriptor(doc, albumName,cat);
			}
			
			/// find image in category
			PhotoCategory photoCat=photoCategoryDao.findByCategoryAndPhotoProviderPath(cat.getId(), doc.getFilename());
			if(photoCat==null){
				return setupFileDescriptor(doc, albumName,cat);
			}
		}
		return null;
	}

	private UploadedFileDescriptor setupFileDescriptor(DocumentListEntry doc,
			String albumName, Category cat) {
		MediaContent mediaContent=(MediaContent)doc.getContent();
		MediaSource source=null;
		try {
			source = documentList.getDocsService().getMedia(mediaContent);
		} catch (IOException e1) {
			logger.error("setupFileDescriptor IOException",e1);
		} catch (ServiceException e1) {
			logger.error("setupFileDescriptor ServiceException",e1);
		}
		//MediaSource source=mediaContent.getMediaSource();
		try{
			
			UploadedFileDescriptor ufd=new UploadedFileDescriptor(doc.getFilename(), source.getInputStream());
			ufd.setFolderName(albumName);
			ufd.setRemoteUri(mediaContent.getUri());
			ufd.setCategory(cat);
			ufd.setProvider(1);
			StringBuffer output = new StringBuffer();
			output.append(" -- " + doc.getTitle().getPlainText() + " ");
			if (!doc.getParentLinks().isEmpty()) {
				for (Link link : doc.getParentLinks()) {
					output.append("[" + link.getTitle() + "] ");
				}
			}
			output.append(doc.getResourceId());
			logger.debug(output.toString());
			return ufd;
		}catch(Exception e){
			logger.error("unable to get input stream for ["+doc.getFilename()+"]");
			return null;
		}
	}

	public void handleSyncConfirm() {
		final User user=HttpHelper.getUser();
		Messagebox.show(Labels.getLabel("google.drive.sync.confirm"), Labels.getLabel("google.drive.sync.title"), 
				new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.OK},
				new String[] {Labels.getLabel("synchronize"),Labels.getLabel("fullsynchronize")},
				Messagebox.INFORMATION,Messagebox.Button.YES, new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
				if(event.getTarget() instanceof Button){
					Button button=(Button)event.getTarget();
					String action=button.getAction();
					boolean full=false;
					if(button.getId().equals("OK")){
						full=true;
					}
					if(!button.getId().equals("CANCEL")){
						final boolean sync=full;
						Thread t=new Thread(){
							public void run(){
								syncFiles(user,sync);
							}
						};
						t.start();
					}else{
						logger.debug("syncronization canceled");
					}
				}
			}
		});
	}
	
	public PhotoDao getPhotoDao() {
		return photoDao;
	}

	public void setPhotoDao(PhotoDao photoDao) {
		this.photoDao = photoDao;
	}

	public CategoryDao getCategoryDao() {
		return categoryDao;
	}

	public void setCategoryDao(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}

	public PhotoCategoryDao getPhotoCategoryDao() {
		return photoCategoryDao;
	}

	public void setPhotoCategoryDao(PhotoCategoryDao photoCategoryDao) {
		this.photoCategoryDao = photoCategoryDao;
	}

	public ExifDataDao getExifDataDao() {
		return exifDataDao;
	}

	public void setExifDataDao(ExifDataDao exifDataDao) {
		this.exifDataDao = exifDataDao;
	}

	public IOAuthService getGoogleOAuthService() {
		return googleOAuthService;
	}

	public void setGoogleOAuthService(IOAuthService googleOAuthService) {
		this.googleOAuthService = googleOAuthService;
	}



}
