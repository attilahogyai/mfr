package org.mfr.web.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mfr.data.Category;
import org.mfr.manager.CategoryManager;
import org.mfr.util.CallbackEvent;
import org.mfr.util.EventData;
import org.mfr.util.HttpHelper;
import org.mfr.util.MailHelper;
import org.mfr.util.ZkUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AlbumDetailComposer extends SelectorComposer {
	private static final Log logger = LogFactory
			.getLog(AlbumDetailComposer.class);
	@WireVariable
	private CategoryManager categoryManager;

	@Wire
	private Window newAlbum;

	@Wire
	private Textbox subject;

	@Wire
	private Checkbox disablePublic;
	@Wire
	private Checkbox recommend;
	@Wire
	private Textbox name;
	@Wire
	private Textbox desc;
	@Wire
	private Checkbox blog;
	@Wire
	private Checkbox ispublic;
	@Wire
	private Checkbox allowDownload;
	@Wire
	private Checkbox showComment;
	
	@Wire
	private Label titleText;
	@Wire
	private Button deleteButton;
	@Wire
	private Listbox sortModeSelector;
	@Wire
	private Listbox sortDirSelector;
	

	private boolean update = false;
	
	private Category category = null;	

	private Integer[] sortModes=new Integer[]{new Integer(0),new Integer(1),new Integer(2)};
	private Integer[] sortDir=new Integer[]{new Integer(0),new Integer(1)};
	
	public ComponentInfo doBeforeCompose(Page page, Component parent,
			ComponentInfo compInfo) {
		
		return super.doBeforeCompose(page, parent, compInfo);
	}
	@Listen("onClick = #saveButton")
	public void update() {
		try {
			if (name.isValid()) {
				
				final Integer sort=(Integer)sortModeSelector.getSelectedItem().getValue();
				final Integer sortDir=(Integer)sortDirSelector.getSelectedItem().getValue();
				final boolean recommendV=recommend!=null?recommend.isChecked():false;
				if (update) {
					categoryManager.updateCategory(category, name.getValue(), desc.getValue(), 
							disablePublic.isChecked(), recommendV, blog.isChecked(), allowDownload.isChecked(), showComment.isChecked(),category.getUseracc(), sort, sortDir);
					categoryManager.mergeCategory(category);
					Messagebox.show(Labels.getLabel("album.saved"),
							Labels.getLabel("information"), Messagebox.OK,
							Messagebox.INFORMATION);
					if (disablePublic.isChecked()) {
						if(category.getIspublic()==0 || category.getIspublic()==null){
							logger.debug(category.getId()+" marked public");
							Map args = new HashMap();
							args.put("user", HttpHelper.getUser());
							args.put("category", category);
							List<String> reg = new ArrayList();
							reg.add("info@myfotoroom.com");
							MailHelper.sendMail(reg, "email/markedpublic.vm",
									HttpHelper.getUser().getEmail(),
									"public request", true, "text/plain", args);
						}
					}
					if (recommend!=null && recommend.isChecked()) {
						List<String> reg = new ArrayList();
						Map args = new HashMap();
						args.put("user", HttpHelper.getUser());
						args.put("category", category);
						reg.add("info@myfotoroom.com");
						MailHelper.sendMail(reg, "email/recommendrequest.vm",
								HttpHelper.getUser().getEmail(),
								"mainpage request", true, "text/plain", args);
					}
					
					
				} else {
					categoryManager.createCategory(name.getValue(),
							desc.getValue(), disablePublic.isChecked(),
							recommendV,blog.isChecked(),allowDownload.isChecked(),showComment.isChecked(),sort,sortDir, new CallbackEvent() {
								@Override
								public void onEvent(EventData event) {
									try{
										category = (Category)event.getData();
										if(disablePublic.isChecked()){
											logger.debug(name.getValue()+" marked public by user:"+HttpHelper.getUser().getUseracc().getId());
											Map args = new HashMap();
											args.put("user", HttpHelper.getUser());
											args.put("category", category);
											List<String> reg = new ArrayList();
											reg.add("info@myfotoroom.com");
											MailHelper.sendMail(reg, "email/markedpublic.vm",
													HttpHelper.getUser().getEmail(),
													"public request", true, "text/plain", args);
										}
										if (recommend!=null && recommend.isChecked()) {
											List<String> reg = new ArrayList();
											Map args = new HashMap();
											args.put("user", HttpHelper.getUser());
											args.put("category", category);
											reg.add("info@myfotoroom.com");
											MailHelper.sendMail(reg, "email/recommendrequest.vm",
													HttpHelper.getUser().getEmail(),
													"mainpage request", true, "text/plain", args);
										}

										Messagebox.show(Labels.getLabel("album.saved"),
												Labels.getLabel("information"), Messagebox.OK,
												Messagebox.INFORMATION);
										name.setRawValue(null);
										desc.setValue(null);
										closeWindow();
									}catch(Exception e){
										logger.error("create category send maill exception",e);
									}
									
								}
							});
				}
			}
		} catch (Exception e) {
			org.mfr.util.ZkUtil.showProcessError(e);
		}
	}

	@Listen("onClick = #deleteButton")
	public void delete() {
		try {
			categoryManager.deleteCategory(category);
			Messagebox.show(Labels.getLabel("album.deleted"),
					Labels.getLabel("information"), Messagebox.OK,
					Messagebox.INFORMATION);
			Component comp=(Component)Executions.getCurrent().getSession().getAttribute(AlbumComposer.ACTUAL_POPUP);
			Events.postEvent("onClose",comp,null);			
		} catch (Exception e) {
			logger.error("delete", e);
			ZkUtil.tryTranslateException(e);
		}
	}

	void closeWindow() {
		Component comp=(Component)Executions.getCurrent().getSession().getAttribute(AlbumComposer.ACTUAL_POPUP);
		Events.postEvent("onClose",comp,null);			
	}

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		category =(Category) Executions.getCurrent().getAttribute("category");
		int sortIdx=0;
		int sortDirIdx=0;
		if (category != null) {
			update = true;
			if(titleText!=null){
				titleText.setValue(Labels.getLabel("album.update.header"));
			}
			name.setText(category.getName());
			desc.setText(category.getDescription());
			if(disablePublic!=null){
				disablePublic.setChecked(category.getIspublic() == 2);
			}
			if(recommend!=null){
				recommend.setChecked(category.getRecommend() == 1
						|| category.getRecommend() == 2);
			}
			blog.setChecked(category.getBlog()!=null && category.getBlog()==1);
			allowDownload.setChecked(category.getAllowDownload()!=null && category.getAllowDownload()==1);
			showComment.setChecked(category.getShowComment()!=null && category.getShowComment()==1);
			
			if(category.getSort()!=null){
				sortIdx=Arrays.binarySearch(sortModes, category.getSort().intValue());
				sortIdx=sortIdx<0?0:sortIdx;
			}
			if(category.getSortDir()!=null){
				sortDirIdx=Arrays.binarySearch(sortModes, category.getSortDir().intValue());
				sortDirIdx=sortDirIdx<0?0:sortDirIdx;
			}
		} else {
			deleteButton.setVisible(false);
			if(titleText!=null){
				titleText.setValue(Labels.getLabel("album.create.header"));
			}
		}
		sortModeSelector.setSelectedIndex(sortIdx);
		sortDirSelector.setSelectedIndex(sortDirIdx);

	}
	public Integer[] getSortModes() {
		return sortModes;
	}
	public void setSortModes(Integer[] sortModes) {
		this.sortModes = sortModes;
	}
	public Integer[] getSortDir() {
		return sortDir;
	}
	public void setSortDir(Integer[] sortDir) {
		this.sortDir = sortDir;
	}

}
