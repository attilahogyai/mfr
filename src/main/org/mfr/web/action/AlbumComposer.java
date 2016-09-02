package org.mfr.web.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.data.Site;
import org.mfr.data.SiteDao;
import org.mfr.data.User;
import org.mfr.util.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

public class AlbumComposer  extends SelectorComposer<Component> {
	public static final String ACTUAL_POPUP = "actualPopup";

	private static final Logger logger=LoggerFactory.getLogger(AlbumComposer.class);
	
	private static final long serialVersionUID = -7114418271440049968L;
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private PhotoCategoryDao photoCategoryDao;
	@Autowired
	private SiteDao siteDao;
	
	private Component mainComp; 
	
	@Wire
	private Listbox siteSelector;
	
	
	
	boolean isPhone;
	boolean isTablet;
	
	@Wire
	private Grid albumGrid;
	
	private Integer siteId;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		if(HttpHelper.getUser()==null){
			return;
		}
		super.doAfterCompose(comp);
		mainComp = comp;
		org.mfr.util.UAgentInfo info=org.mfr.util.UAgentInfo.getUAgentInfo();
		isPhone=info.detectMobileLong();
		isTablet=(info.detectTierTablet() || info.detectIpad());
		refresh();
	}
	public class AlbumTableRender implements RowRenderer {
		
		@Override
		public void render(Row row, Object data,final int rowNr) throws Exception {
			try{
				Object[] m=(Object[])data;
				final Category category = (Category) m[0];
				final Integer imageCount = (Integer) m[1];
				Component [] comps=mainComp.getTemplate("albumRow").create(row,null,new org.zkoss.xel.VariableResolver(){
					
					public Object resolveVariable(String variable){
						if("each".equals(variable)){
							return category;
						}else if("index".equals(variable)){
							return new Integer(rowNr);
						}else if("ispublic".equals(variable)){
							int ispublic=category.getIspublic()==null?0:category.getIspublic();
							return ispublic;
						}else if("imageCount".equals(variable)){
							return imageCount;
						}else if("viewed".equals(variable)){
							int viewed=category.getAccessCount()==null?0:category.getAccessCount();
							return viewed;
						}else if("isDefault".equals(variable)){
							return category.getIsDefault();
						}
						return null;
					}
				},null);
				
				
			}catch(Exception e){
				logger.error("ender",e);
			}
			
		}
		
	}	
 	void refresh(){
 		List categoryList=null;
 		try{
 			User user=HttpHelper.getUser();
 			if(siteId==null){
 				categoryList=categoryDao.findByUserId(user.getUserId());
 			}else{
 				categoryList=categoryDao.getSiteCategoriesForUser(user.getUserId(),siteId);
 			}
		 	albumGrid.setModel(new ListModelList(categoryList));
		 	albumGrid.setRowRenderer(new AlbumTableRender());
 		}catch(Exception e){
 			org.mfr.util.ZkUtil.showProcessError(e);
 		}
 		
 	}
 	
	public void createAlbumDetailWindow(Category data){
		Window setupAlbum=createAlbumSetupWindow(data);
	 	Tabbox box=(Tabbox)setupAlbum.getFellow("tbox");
	 	Tab detail=(Tab)setupAlbum.getFellow("albumDetail");
		box.setSelectedTab(detail);
	}
	public Window createAlbumSetupWindow(Category data){
		Map arg=new HashMap();
		arg.put("category",data);
		Executions.getCurrent().setAttribute("category", data);
	 	Component comp=Executions.createComponents("comp/albumsetup.zul", null, arg);
	 	Window setupAlbum=(Window)comp.getFellow("albumSetup");
	 	Executions.getCurrent().getSession().setAttribute(ACTUAL_POPUP, setupAlbum);
	 	setupAlbum.addEventListener("onClose", new org.zkoss.zk.ui.event.EventListener(){
	 		   public void onEvent(Event event){
	 			   refresh();
	 		   }
	 	});
	 	setupAlbum.doModal();
	 	return setupAlbum;
	}
	
	public List<Site> getSites(){
		List<Site> sites=siteDao.getSitesForUseracc(HttpHelper.getUser().getUserId());
		if(sites==null || sites.size()==0 && siteSelector!=null){
			siteSelector.setDisabled(true);
		}
		return sites;
	}
	
	public void albumOnChange(){
		siteId=siteSelector.getSelectedItem().getValue();
		refresh();
	}
	public void createAccessDetailWindow(Category data){
		Window setupAlbum=createAlbumSetupWindow(data);
	 	Tabbox box=(Tabbox)setupAlbum.getFellow("tbox");
	 	Tab share=(Tab)setupAlbum.getFellow("albumShare");
		box.setSelectedTab(share);
	}	
}
