package org.mfr.web.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.ImageDataModel;
import org.mfr.data.PermissionDao;
import org.mfr.data.User;
import org.mfr.data.UseraccDao;
import org.mfr.data.UseraccDataDao;
import org.mfr.image.ImageConfig;
import org.mfr.image.ImageTools;
import org.mfr.image.ImageTransform;
import org.mfr.manager.CategoryManager;
import org.mfr.manager.ImageDataManager;
import org.mfr.manager.PermissionDetail;
import org.mfr.util.CallbackEvent;
import org.mfr.util.EventData;
import org.mfr.util.HttpHelper;
import org.mfr.zul.comp.CheckBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.image.AImage;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;


public class ImageHandlerComposer extends SelectorComposer<Div>  {
	private static final String SIMPLE = "simple";
	public static final String IMAGEHANDLERCOMPOSER="ImageHandlerComposer"; 
	private static final Logger logger=LoggerFactory.getLogger(ImageHandlerComposer.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 6645767437687677760L;
	List<CheckBox> checkBoxes=new ArrayList<CheckBox>();
	private List<Category> categoryList=null;
	

	Map<String,Object> selectedImagesMap=new HashMap<String,Object>();
	Map<String,ImageDataModel> imagesModelMap=new HashMap<String,ImageDataModel>();
	Map<String,ImageTransform> transformModelMap=new HashMap<String,ImageTransform>();
	
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private CategoryManager categoryManager;
	
	@Autowired
	private ImageDataManager imageDataManager;
	@Autowired
	private UseraccDao useraccDao;
	@Autowired
	private UseraccDataDao useraccDataDao;
	@Autowired
	private PermissionDao permissionDao;
	
	ImageDataModel [] imageData=null;
 	String [] data2=new String[]{"aaaa","bbbb"};
 	Integer nos=1;
		
 	@Wire
 	private Checkbox switchAll=null;
 	@Wire
 	private Menuitem layoutSimple;
 	@Wire
 	private Menuitem layoutDetailed;
 	@Wire
 	private Div simpleImageContainer; 
 	@Wire
 	private Grid detailedImageGrid;
 	
 	private Map<String,Div> quickEditorDiv=new HashMap<String,Div>();
 	@Wire
 	private Div actionMenu;
 	@Wire
 	private Div mainMenu;
 	@Wire
 	private Window photoAction;
 	@Wire
 	private A shareLink;
 	
 	@Wire
 	private A moveToAlbum;
 	@Wire
 	private A addToAlbum;
 	@Wire
 	private A removeFromAlbum;
 	@Wire
 	private Div imgUploadText;
 	
 	private static final String detailButton="img/icon2/edit.png";
 	
 	private int action;
 	
	public void addImageSelection(String key){
		//selectedImages.add(key);
		selectedImagesMap.put(key,true);
	}
	public void removeImageSelection(String key){
		//selectedImages.remove(key);
		selectedImagesMap.remove(key);
		
	}
    public void init() {
 		User user=HttpHelper.getUser();
 		if(user!=null){
 			logger.debug("instance["+this+"] created for user["+user.getUserName()+"]");
 			categoryList=categoryDao.findByOwnerFetchPhotoCategory(user.getUseracc());
 		}else{
 			Thread.dumpStack();
 			logger.debug("instance["+this+"] created out of session scope what ta fak? Stack dumped in error log.");
 		}
 		HttpHelper.getHttpSession().setAttribute(IMAGEHANDLERCOMPOSER, this);
	}
    
    public void createImageDetailWindow(String imageId){
    	Map arg=new HashMap();
    	ImageDataModel imageData=imagesModelMap.get(imageId);
    	if(imageData.isOwner()){
	    	try {
	    		imageData=ImageDataManager.extractImageData(imageData.getPhoto(), false, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			arg.put("imageData",imageData);
		 	Component comp=Executions.createComponents("comp/photodetail.zul", null, arg);
		 	Window photoDetail=(Window)comp.getFellow("photoDetail");
		 	photoDetail.addEventListener("onClose", new org.zkoss.zk.ui.event.EventListener(){
		 		   public void onEvent(Event event){
	//	 			   refresh();
		 		   }
		 	});
		 	photoDetail.doModal();
    	}else{
    		logger.debug("not owner for["+imageId+"]");
    	}
    }
   public void createQuickEditorWindow(Component main,String imageId){
	   boolean createNew=true;
	   if(quickEditorDiv.size()>0){
		   for (Map.Entry<String, Div> element : quickEditorDiv.entrySet()) {
			   createNew=!element.getKey().equals(imageId);
			   if(element.getValue().getDesktop()!=null){
				   element.getValue().detach();
			   }
			   quickEditorDiv.remove(element.getKey());
			   
		   }
		   
	   }
	   if(createNew){
		   Component [] comps=simpleImageContainer.getTemplate("quickEditor").create(main,null,new org.zkoss.xel.VariableResolver(){
			   public Object resolveVariable(String variable){
				   if("categoryList".equals(variable)){
					   return null;
				   }
				   return null;
			   }
		   },null);
		   for (int i = 0; i < comps.length; i++) {
			   if(comps[i].getId().equals("quickEditorDiv")){
				   quickEditorDiv.put(imageId, (Div)comps[i]);
			   }
		   }
	   }		
   }
   public void rotate(Component parent,String imageId,double degree){
	   Image thumbImage=(Image)parent.getFellowIfAny("img-"+imageId);
	   
	   if(thumbImage!=null){
		   try {
			   ImageDataModel imageData=imagesModelMap.get(imageId);
			   File [] result1=ImageTools.rotateImage(ImageConfig.WORKPNGCONFIG,degree,imageData.getImageRealPath());
			   ImageTools.resizeImagesFromWORKPNG(ImageConfig.THUMBCONFIG, imageData.getPhoto().getPath());
			   thumbImage.setContent(new AImage(ImageConfig.THUMBCONFIG.getPath(imageData.getImageRealPath())));
			   List c=new ArrayList();
			   c.add(ImageConfig.MEDIUMCONFIG);
			   c.add(ImageConfig.PREVIEWCONFIG);
			   ImageTools.finishChanges(c,imageData.getImageRealPath());
		   } catch (Exception e) {
			   logger.error("rotate error", e);
		   }
	   }
   }
   public void doneTransform(String imageId){
	   
   }
	public class ImageTableRender2 implements RowRenderer {
		public void render(Row row, Object data) throws Exception {
			render(row, data, 0);
		}

		@Override
		public void render(Row row, Object data, int index) throws Exception {
			
			
			Div ivb=new Div();
			ivb.setStyle("float:left;");
			ivb.setWidth("");
//			Vbox ivb=new Vbox();
			row.appendChild(ivb);
			
			final ImageDataModel rowData = (ImageDataModel) data;
			
			final String imageId=rowData.getId().toString();
			
			A editLink=new A();
			editLink.setStyle("padding:2px 0px;");
			
			Image editImage=new Image(detailButton);
			editImage.setSclass("hand");
			editImage.setStyle("padding-bottom:2px;");
			editLink.addEventListener("onClick",new org.zkoss.zk.ui.event.EventListener(){
				   public void onEvent(Event event){
					   createImageDetailWindow(imageId);
				   }
			});			
			editLink.appendChild(editImage);
			ivb.appendChild(editLink);
			
			CheckBox checkBox=new CheckBox(ivb);
			checkBoxes.add(checkBox);
			checkBox.addEventListener("onCheck",new org.zkoss.zk.ui.event.EventListener(){
				public void onEvent(Event event){
					CheckBox cb=(CheckBox)event.getTarget();
					if(cb.isSelected()){
						addImageSelection(cb.getValue());
					}else{
						removeImageSelection(cb.getValue());
					}
				}
			});
			checkBox.setValue(rowData.getId().toString());
			if(selectedImagesMap.containsKey(rowData.getId().toString())){
				checkBox.setSelected(true);
			}
			
			
			checkBox.setParent(ivb);
			ivb.appendChild(checkBox);
			

			
			Page page=Executions.getCurrent().getDesktop().getFirstPage();
			Component contentPage=page.getFellow("contentPage");
			

			
			
			
			Component [] comps=contentPage.getTemplate("imageTemplate").create(row,null,new org.zkoss.xel.VariableResolver(){
				public Object resolveVariable(String variable){
					if("each".equals(variable)){
						return rowData;
					}
					return null;
				}
			},null);
			
			
//			Image img=new Image();
//			img.setContent(rowData.getImageThumb());
//			img.setClass("row-image");
//			row.appendChild(img);
			
			
			Vbox vb=new Vbox();
			vb.appendChild(new Label(rowData.getImageName()));
			if(rowData.getExifDataObject()!=null){
				vb.appendChild(new Label(rowData.getExifDataObject().getImageWidth()+" x "+rowData.getExifDataObject().getImageHeight()));
				StringBuffer sb=new StringBuffer();
				if(rowData.getExifDataObject().getIso()!=null){
					sb.append("iso:"+rowData.getExifDataObject().getIso());
				}
				if(rowData.getExifDataObject().getIso()!=null){
					if(sb.length()>0) sb.append(", ");	
					sb.append("f"+rowData.getExifDataObject().getFnumber());
				}
				if(rowData.getExifDataObject().getExposureTime()!=null){
					if(sb.length()>0) sb.append(", "); 
					sb.append(rowData.getExifDataObject().getExposureTime());
				}
				vb.appendChild(new Label(sb.toString()));
				sb=new StringBuffer();
				if(rowData.getExifDataObject().getFocalLength()!=null){
					sb.append(rowData.getExifDataObject().getFocalLength());
				}
				if(rowData.getExifDataObject().getLensModel()!=null){
					if(sb.length()>0) sb.append(", lens:");	
					sb.append(rowData.getExifDataObject().getLensModel());
				}
				vb.appendChild(new Label(sb.toString()));
			}
			row.appendChild(vb);
			if(rowData.getCategoryNames()!=null){
				StringBuffer sb=new StringBuffer();
				for (String category : rowData.getCategoryNames()){
					if(category.equals("default")){
						sb.append(Labels.getLabel("default.album")).append(", ");	
					}else{
						sb.append(category).append(", ");
					}
				}
				sb.delete(sb.length()-2,sb.length());
				row.appendChild(new Label(sb.toString()));
			}else{
				row.appendChild(new Label(""));
			}

			
		}
		
	}	
	
 	public void refreshCategory(){
 		int selectedValue=-1;
 		Page page=Executions.getCurrent().getDesktop().getFirstPage();
 		Listbox category=getCategorySelector();
 		
 		if(category!=null){
 			selectedValue=category.getSelectedItem().getValue();
 		}
 		
 		Div comboDiv = (Div)page.getFellowIfAny("comboDiv");
 		User user=HttpHelper.getUser();
 		
		categoryList=categoryDao.findByOwnerFetchPhotoCategory(user.getUseracc());
		PermissionDetail pd=user.getPermission();
		if(pd!=null){
			List<Category> sharedCategory=permissionDao.getSharedCategory(user.getUseracc());
			categoryList.addAll(sharedCategory);
			Collections.sort(categoryList, new Comparator<Category>() {
				@Override
				public int compare(Category o1, Category o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		
 		final List cl=categoryList;
 		if(comboDiv.getFellowIfAny("category")!=null){
 			comboDiv.removeChild(comboDiv.getFellowIfAny("category"));
 		}
		Component [] comps=comboDiv.getTemplate("comboTemplate").create(comboDiv,null,new org.zkoss.xel.VariableResolver(){
			public Object resolveVariable(String variable){
				if("categoryList".equals(variable)){
					return cl;
				}
				return null;
			}
		},null);
		category = (Listbox)page.getFellow("category");
		if(selectedValue!=-1){
			category.setSelectedIndex(org.mfr.data.CategoryDao.findIndex(category.getItems(),selectedValue));
		}
 	}

 	
 	private void makeCategorySelection(){
 		Page page=Executions.getCurrent().getDesktop().getFirstPage();
 		Session session=Executions.getCurrent().getSession();
 		Listbox category = (Listbox)page.getFellowIfAny("category");
		if(category.getSelectedItem()==null){
			Integer selectedCatId=(Integer)session.getAttribute(org.mfr.manager.ImageDataManager.SELECTED_CATEGORY);
			if(selectedCatId==null){
				category.setSelectedIndex(0);	
			}else{
 				Integer idx=org.mfr.data.CategoryDao.findIndex(category.getItems(),selectedCatId);
 				try{
 					category.setSelectedIndex(idx);
 				}catch(Exception e){
 					logger.debug("error selecting category set default "+e.getMessage());
 					category.setSelectedIndex(0);
 				}
			}
		}
		if(selectedImagesMap.size()>0){
			actionMenu.setVisible(true);
			mainMenu.setVisible(false);
		}else{
			actionMenu.setVisible(false);
			mainMenu.setVisible(true);
		}

		session.setAttribute(org.mfr.manager.ImageDataManager.SELECTED_CATEGORY,category.getSelectedItem().getValue());
	}
	@Listen("onCreate = #detailedImageGrid; onClick= #refreshButton")	
 	public void refresh(){
 		try{
 			Session session=Executions.getCurrent().getSession();
 	 		String id=((HttpSession)session.getNativeSession()).getId();
 	 		Page page=Executions.getCurrent().getDesktop().getFirstPage();
 	 		
 	 		
 	 		checkBoxes=new ArrayList<CheckBox>();
 			switchAll.setChecked(false);
 			refreshCategory();
 			Listbox category = (Listbox)page.getFellowIfAny("category");
 			
 			String albumId=(String)((HttpServletRequest)Executions.getCurrent().getNativeRequest()).getParameter("albumId");
 			if(albumId!=null){
 				Integer idx=org.mfr.data.CategoryDao.findIndex(category.getItems(),Integer.parseInt(albumId));
 				category.setSelectedIndex(idx);
 			}
			makeCategorySelection();
	 		User user=HttpHelper.getUser();

 			if(imageDataManager.checkForUploadedImages(id)){
 				imageDataManager.processUploadedImages(user,id);
 			}
			
 			String layout=SIMPLE;
 			if(user!=null){
	 			org.mfr.data.UseraccData useraccData=useraccDataDao.findUseraccData(user.getLoginName(),org.mfr.data.UseraccDataDao.IMAGE_VIEWER_TYPE);
	 			if(useraccData==null){
	 				useraccData=new org.mfr.data.UseraccData();
	 				org.mfr.data.Useracc useracc=useraccDao.findById(user.getUseracc().getId());
	 				useraccData.setUseracc(useracc);
	 				useraccData.setKey(org.mfr.data.UseraccDataDao.IMAGE_VIEWER_TYPE);
	 				useraccData.setValue(SIMPLE);
	 				useraccDataDao.persist(useraccData);
	 			}
	 			//layout=useraccData.getValue();
	 			
				if(GlobalVariableResolver.isPhone()){
					layout=SIMPLE;
				}else{
	//				if(layout.equals(SIMPLE)){
	//					layoutSimple.setChecked(true);
	//					layoutDetailed.setChecked(false);
	//				}else{
	//					layoutDetailed.setChecked(true);
	//					layoutSimple.setChecked(false);
	//				}
				}
 			}
			Category categoryObject=categoryDao.findById((Integer)category.getSelectedItem().getValue());
			
			if(!categoryObject.isCatOwner()){
				if(shareLink.isVisible()){
					shareLink.setVisible(false);
					moveToAlbum.setVisible(false);
					addToAlbum.setVisible(false);
					imgUploadText.setVisible(false);
				}
			}else if(!shareLink.isVisible()){
				shareLink.setVisible(true);
				moveToAlbum.setVisible(true);
				addToAlbum.setVisible(true);
			}
			
			
			imageData=imageDataManager.listAlbumContentbyId(categoryObject,true,!layout.equals(SIMPLE));
			
			Map newSelectionMap=new HashMap();			
 			for(ImageDataModel idm : imageData){
 				if(selectedImagesMap.containsKey(idm.getId().toString())){
 					newSelectionMap.put(idm.getId().toString(),true);
 				}
 				imagesModelMap.put(idm.getId().toString(), idm);
 					
 			}
 			selectedImagesMap=newSelectionMap;

			if(simpleImageContainer.getFellowIfAny("simpleImageContainerSub")!=null){
				simpleImageContainer.removeChild(simpleImageContainer.getFellow("simpleImageContainerSub"));
			}
			
			Div simpleImageContainerSub=new Div();
			simpleImageContainerSub.setId("simpleImageContainerSub");
			simpleImageContainer.appendChild(simpleImageContainerSub);
			simpleImageContainerSub.setSclass("simple-img-cont row");
			final ImageDataModel[] imageDataArray=imageData;
			for(ImageDataModel idm:imageDataArray){
				org.mfr.image.NoHtmlOutZulImage preview=new org.mfr.image.NoHtmlOutZulImage();
				simpleImageContainerSub.appendChild(preview);
				preview.setContent(idm.getImagePreview());
				idm.setZkImagePreview(preview);
			}
			
 			
 			if(!layout.equals(SIMPLE)){
 				simpleImageContainer.setVisible(false);
	 			if(imageData!=null){
					detailedImageGrid.setModel(new ListModelList(imageData));
					detailedImageGrid.setRowRenderer(new ImageTableRender2());
	 			}
	 			detailedImageGrid.setVisible(true);
 			}else{
 				if(imageData!=null){
 					detailedImageGrid.setVisible(false);
 					
 	 				Component [] comps=simpleImageContainer.getTemplate("simpleTemplate").create(simpleImageContainerSub,null,new org.zkoss.xel.VariableResolver(){
 						public Object resolveVariable(String variable){
 							
 							if("imageDataArray".equals(variable)){
 								return imageDataArray;
 							}
 							return null;
 						}
 					},null);
 	 				

 	 				
 	 				simpleImageContainer.setVisible(true);
 					
 		 		}
 			}
 			
 		}catch(Exception e){
			org.mfr.util.ZkUtil.showProcessError(e);
 		}      
 	}
	public void addToAlbum(String albumId,String albumName){
 		int added=imageDataManager.addToAlbum(selectedImagesMap,Integer.parseInt(albumId));
 		clearSelection();
 		//Messagebox.show(Labels.getLabel("image.to.album.added",new Object[]{added,albumName}),Labels.getLabel("information"),Messagebox.OK,"info");
 		refresh();
 	}
 	public void moveToAlbum(String albumId,String albumName){
 		Listbox category=getCategorySelector();

 		int moved=imageDataManager.moveToAlbum(selectedImagesMap,(Integer)category.getSelectedItem().getValue(),Integer.parseInt(albumId));
 		clearSelection();
 		//Messagebox.show(Labels.getLabel("image.to.album.moved",new Object[]{moved,albumName}),Labels.getLabel("information"),Messagebox.OK,"info");
 		refresh();
 	}
 	@Listen("onClick=#removeFromAlbumMenu") 
 	public void removeFromAlbum(){
 		Page page=Executions.getCurrent().getDesktop().getFirstPage();
 		final Listbox category = (Listbox)page.getFellowIfAny("category");
 		org.mfr.util.ZkUtil.messageBoxConfirm("delete.image.confirm.question",new Object[]{},new EventListener(){
			public void onEvent(Event e){
				Integer data=(Integer)e.getData();
	                switch (data) {
	                	case Messagebox.OK: //OK is clicked
	                		
	                		final Map<String,Object> unsuccess=imageDataManager.removeFromAlbum(selectedImagesMap,(Integer)category.getSelectedItem().getValue());
	                 		if(unsuccess.size()>0){
	                 			org.mfr.util.ZkUtil.messageBoxConfirm("image.from.album.removed.failed",new Object[]{unsuccess.size()},new EventListener(){
	                				public void onEvent(Event e){
	                					Integer data=(Integer)e.getData();
	                 	                switch (data) {
	                 	                	case Messagebox.OK: //OK is clicked
	                	 	   					try{
	                	 		 					imageDataManager.deleteImages(unsuccess);
	                	 		 					clearSelection();
	                	 		 					refresh();
	                	 		 				}catch(Exception e1){
	                	 		 					org.mfr.util.ZkUtil.showProcessError(e1);
	                	 		 				}
	                 	                		break;
	                 	                	case Messagebox.CANCEL: //Cancel is clicked
	                 	                		break;
	                 	                }
	                	            }
	                			});
	                 		}else{
	                 			//Messagebox.show(Labels.getLabel("image.from.album.removed",new Object[]{selectedImagesMap.size(),category.getSelectedItem().getLabel()}),Labels.getLabel("information"),Messagebox.OK,"info");
		                 		clearSelection();
		                 		refresh();
	                 		}	
	                		break;
	                	case Messagebox.CANCEL: //Cancel is clicked
	                		break;
	                }
            }
		});
 	}
 	@Listen("onClick=#switchAll")
 	public void switchSelection(){
 		for(Object checkbox : checkBoxes){
 			CheckBox cb=(CheckBox)checkbox;
 			if(switchAll.isChecked()){
 				cb.setSelected(true);
 				addImageSelection(cb.getValue());
 			}else{
 				cb.setSelected(false);
 				removeImageSelection(cb.getValue());
 			}

 		}
 			
 	}
 	@Listen("onClick=#deleteImageMenu")
 	public void deleteImage(){
 		if(selectedImagesMap.size()>0){
 			Set categories=new HashSet();
 			for(Object imageId : selectedImagesMap.keySet()){
 				ImageDataModel idm=imageDataManager.getFullImageDataById(Integer.parseInt((String)imageId));
 	 			categories.addAll(idm.getCategoryNames());
 			}
 			org.mfr.util.ZkUtil.messageBoxConfirm("delete.image.confirm",new Object[]{selectedImagesMap.size(),categories.size(),categories.toString()},new EventListener(){
				public void onEvent(Event e){
					Integer data=(Integer)e.getData();
 	                switch (data) {
 	                	case Messagebox.OK: //OK is clicked
	 	   					try{
	 		 					imageDataManager.deleteImages(selectedImagesMap);
	 		 					clearSelection();
	 		 				}catch(Exception e1){
	 		 					Messagebox.show(Labels.getLabel("image.process.error")+"["+e1.getMessage()+"]");
	 		 				}
	 		 				refresh();
 	                		break;
 	                	case Messagebox.CANCEL: //Cancel is clicked
 	                		break;
 	                }
	            }
			});
 				
 			}
 		}
 	@Listen("onClick=#layoutSimple;onClick=#layoutDetailed")
 	public void switchLayout(){
 		boolean act=layoutDetailed.isChecked();
 		layoutDetailed.setChecked(!act);
 		layoutSimple.setChecked(act);

 		User user=HttpHelper.getUser();
		org.mfr.data.UseraccData useraccData=useraccDataDao.findUseraccData(user.getLoginName(),org.mfr.data.UseraccDataDao.IMAGE_VIEWER_TYPE);
		useraccData.setValue(layoutDetailed.isChecked()?"detailed":SIMPLE);
		useraccDataDao.merge(useraccData);
 		refresh();
 	}
 	public void switchSelection(Div d){
 		String imgId=d.getId();
 		CheckBox checkbox=(CheckBox)d.getFellowIfAny("check-"+imgId);
 		if(checkbox!=null){
			if(selectedImagesMap.containsKey(imgId)){
				d.setSclass("deselected-img");
				removeImageSelection(imgId);
				checkbox.setSelected(false);
			}else{
				d.setSclass("selected-img");
				addImageSelection(imgId);
				checkbox.setSelected(true);
			}
			makeCategorySelection();
 		}
 	}
	public void move(Component self,Component dragged) {
  		Page page=Executions.getCurrent().getDesktop().getFirstPage();
  		Div simpleImageContainerSub = (Div)page.getFellowIfAny("simpleImageContainerSub");
    	 
         if (self instanceof Image) {
        	 simpleImageContainerSub.insertBefore(dragged.getParent().getParent(), self.getParent().getParent());
         } else {
             self.appendChild(dragged);
         }
     }
     public void categoryOnChange(){
    	 clearSelection();
    	 refresh();
     }
	public Map<String, Object> getSelectedImagesMap() {
		return selectedImagesMap;
	}
	public void setSelectedImagesMap(Map<String, Object> selectedImagesMap) {
		this.selectedImagesMap = selectedImagesMap;
	}
	public void clearSelection(){
		selectedImagesMap=new HashMap<String,Object>();
	}
	public List<Category> getCategoryList() {
		User user=HttpHelper.getUser();
		if(user.getUseracc()==null){
			return null;
		}
		categoryList=categoryDao.findByOwnerFetchPhotoCategory(user.getUseracc());
		return categoryList;
	}
	
	public void setPhotoWindow(int type){
		action=type;
		
		Label label=(Label)photoAction.getFellow("expText");
		label.setValue(Labels.getLabel("move.to.album.text", new Object[]{selectedImagesMap.size()}));
		
		// render album selector with new values
		Textbox name=(Textbox)photoAction.getFellow("name");
		name.setText("");
		Page page=Executions.getCurrent().getDesktop().getFirstPage();
		Listbox selectedAlbum=(Listbox)photoAction.getFellow("moveCategory");
		Listbox category = (Listbox)page.getFellowIfAny("category");
		selectedAlbum.getItems().clear();
		for (int i = 0; i < category.getItemCount(); i++) {
			selectedAlbum.appendChild((Component)category.getItemAtIndex(i).clone());
		}
		photoAction.doModal();
		switch (action){
			case 1:{ // move
				//Component comp=(Window)Path.getComponent("/moveInclude/movePhoto");
				
			}break;
			case 2:{ // copy
				
			}
		}
	}
	public void executePhotoAction(){
		Textbox name=(Textbox)photoAction.getFellow("name");
		Category category=null;
		final int act=action;
		if(name.getValue()!=null && name.getValue().length()>0){
			categoryManager.createCategory(name.getValue(), "", false, false, false,false,true,0,0,new CallbackEvent() {
				@Override
				public void onEvent(EventData event) {
					Category cat=(Category)event.getData();
					handleCategoryAction(cat,act);
				}
			});
		}else{
			Listbox selectedAlbum=(Listbox)photoAction.getFellow("moveCategory");
			Integer id=selectedAlbum.getSelectedItem().getValue();
			if(id.equals(-1)){
				return;
			}
			category=categoryManager.getCategoryDao().findById(id);
		}
		handleCategoryAction(category,act);
	}
	private void handleCategoryAction(Category category,int act){
		if(category!=null){
			switch (act){
				case 1:{ // move
					this.moveToAlbum(category.getId().toString(), category.getName());
				}break;
				case 2:{ // copy
					this.addToAlbum(category.getId().toString(), category.getName());
				}
			}
			photoAction.setVisible(false);
		}
	}
	
	public void createAlbumSetupWindow(){
		Map arg=new HashMap();
		Category data=getSelectedCategory();
		arg.put("category",data);
		Executions.getCurrent().setAttribute("category", data);
	 	Component comp=Executions.createComponents("comp/albumsetup.zul", null, arg);
	 	Window setupAlbum=(Window)comp.getFellow("albumSetup");
	 	setupAlbum.addEventListener("onClose", new org.zkoss.zk.ui.event.EventListener(){
	 		   public void onEvent(Event event){
	 			   refreshCategory();
	 		   }
	 	});
	 	
	 	Executions.getCurrent().getSession().setAttribute(AlbumComposer.ACTUAL_POPUP, setupAlbum);
	 	setupAlbum.doModal();
	 	Tabbox box=(Tabbox)setupAlbum.getFellow("tbox");
	 	Tab share=(Tab)setupAlbum.getFellow("albumShare");
		box.setSelectedTab(share);
	}
	
	public Category getSelectedCategory(){
 		Listbox categories=getCategorySelector();
		Integer albumId=(Integer)categories.getSelectedItem().getValue();
		return categoryDao.findById(albumId);
	}
	protected Listbox getCategorySelector() {
		Page page=Executions.getCurrent().getDesktop().getFirstPage();
 		return (Listbox)page.getFellowIfAny("category");
	}
     
}
