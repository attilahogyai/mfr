package org.mfr.web.action;

import java.awt.TextField;
import java.util.ArrayList;
import java.util.List;

import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.PhotoCategory;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.data.Site;
import org.mfr.data.SiteDao;
import org.mfr.data.SiteGalleries;
import org.mfr.data.SiteGalleriesDao;
import org.mfr.data.StyleConfig;
import org.mfr.data.StyleConfigDao;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;

public class SiteUpdateComposer extends AbstractSelectorComposer {
	private static final Logger logger=LoggerFactory.getLogger(SiteUpdateComposer.class);
	@WireVariable
	private SiteDao siteDao;
	
	@WireVariable
	private CategoryDao categoryDao;
	
	@WireVariable
	private StyleConfigDao styleConfigDao;
	
	@WireVariable
	private SiteGalleriesDao siteGalleriesDao;

	@WireVariable
	private PhotoCategoryDao photoCategoryDao;
	
	@Wire
	private Button deleteSiteButton;
	@Wire
	private Checkbox sitestate;
	@Wire
	private Checkbox sitelisted;
	
	
	@Wire
	private Checkbox enablecontact;
	
	@Wire
	private Checkbox enableabout;
	
	@Wire
	private Textbox sitename;
	@Wire
	private Textbox siteurl;
	@Wire
	private Textbox siteadminemail;
	@Wire
	private Textbox sitePassword;
	
	@Wire
	private Checkbox sitePasswordCheck;
	
	
	@Wire
	private Radiogroup style;
	
	@Wire
	private Textbox sitedesc;
	

	private Site site;
	
	private static final long serialVersionUID = 824090396088113130L;
	
	private List<StyleConfig> styleConfigList;
	
	@Override
	public void doBeforeComposeChildren(Component comp) throws Exception {
		styleConfigList=styleConfigDao.findAll();
		super.doBeforeComposeChildren(comp);
	}

	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		setupForm();
		
	}	
	
	
	
	@Listen("onClick = #deleteSiteButton")
	public void onDeleteSite(){
		final List<Site> sites=siteDao.getSitesForUseracc(HttpHelper.getUser().getUserId());
		if(sites.size()>0){
			ZkUtil.messageBoxConfirm("delete.site.message",new Object[]{sites.get(0).getName()}, new EventListener<Event>() {
				@Override
				public void onEvent(Event e) throws Exception {
					Integer data=(Integer)e.getData();
 	                switch (data) {
						case Messagebox.OK: {
							siteDao.remove(sites.get(0));
							Integer userId=HttpHelper.getUser().getUseracc().getId();
							List sites=siteDao.getSitesForUseracc(userId);
							HttpHelper.getUser().setSite(sites);
							if(sites.size()==0){
								Executions.sendRedirect("index.zul");
							}
						}
 					}
				}
				
			});
		}
	}
	
	
	public void publishSite(){
		site.setName(sitename.getText());
		site.setUrl(siteurl.getText());
		site.setState(sitestate.isChecked()?1:0);
		site.setListed(sitelisted.isChecked()?1:0);
		site.setDescription(sitedesc.getValue());
		
		site.setOwner(HttpHelper.getUser().getUseracc());
		Integer styleV=0;
		if(style.getSelectedItem()!=null){
			styleV=Integer.parseInt((String)style.getSelectedItem().getValue());
		}
		site.setStyle(styleV);

		if(sitePasswordCheck.isChecked()){
			site.setPassword(sitePassword.getValue());
		}else{
			site.setPassword("");
		}
		if(enableabout!=null){
			site.setEnableAbout(enableabout.isChecked()?1:0);
			site.setEnableContact(enablecontact.isChecked()?1:0);
			if(siteadminemail.isValid()){
				site.setAdminEmail(siteadminemail.getText());
			}
		}
		
	}
	
	@Listen("onClick = #updateSiteButton")
	public void onSiteUpdate(){
		if(!checkMandatory(sitename,siteurl)){
			return;
		}
		if(site!=null){
			// update
			publishSite();
			try{
				siteDao.merge(site);
				ZkUtil.messageBoxInfo("portfolio.save.success",new Object[]{siteurl.getText()});
			}catch(Exception e){
				ZkUtil.tryTranslateException(e);
				logger.warn("onSiteUpdate",e);
			}
		}else{
			// check albums for photos
			List<SiteGalleries> siteGalleries=new ArrayList<SiteGalleries>();
			List<Category> categoryList=categoryDao.findByUseracc(HttpHelper.getUser().getUseracc());
			int max = 0;
			for (Category category : categoryList) {
				List<PhotoCategory> photo=photoCategoryDao.findByCategoryId(category.getId());
				if(photo!=null && photo.size()>0){
					SiteGalleries sg=new SiteGalleries();
					sg.setType(SiteGalleriesDao.GALLERY);
					sg.setCategory(category);
					siteGalleries.add(sg);
					max++;
					if(max>10){
						break;
					}
				}
			}
			if(max==0){
				ZkUtil.messageBoxInfo("portfolio.alert.create.album");
			}else{
				// insert
				site=new Site();
				try{
					publishSite();
					siteDao.persist(site);
				}catch(Exception e){
					logger.warn("onSiteUpdate",e);
					ZkUtil.tryTranslateException(e);
					return;
				}				
				for (SiteGalleries gallery : siteGalleries) {
					// create site
					gallery.setSite(site);
					siteGalleriesDao.persist(gallery);
				}
				List sites=siteDao.getSitesForUseracc(HttpHelper.getUser().getUseracc().getId());
				HttpHelper.getUser().setSite(sites);
				ZkUtil.messageBoxInfo("portfolio.save.success",new Object[]{siteurl.getText()});
			}
			
		}
		setupForm();
		
	}
	public boolean isSiteExists(){
		List<Site> sites=siteDao.getSitesForUseracc(HttpHelper.getUser().getUserId());
		return sites.size()>0; 
	}
	@Listen("onClick = #sitePasswordCheck, #sitestate")
	public void onSitePasswordCheck(Event event){
		if(!sitestate.isChecked()){
			sitelisted.setDisabled(true);
			sitelisted.setChecked(false);
			sitePasswordCheck.setDisabled(true);
			sitePasswordCheck.setChecked(false);
		}else{
			sitelisted.setDisabled(false);
			sitePasswordCheck.setDisabled(false);
		}
		
		if(sitePasswordCheck.isChecked()){
			sitePassword.setDisabled(false);
		}else{ // first loading
			
			if((site!=null && site.getPassword()!=null && site.getPassword().length()>0)){
				if(event==null){
					sitePassword.setValue(site.getPassword());
					sitePasswordCheck.setChecked(true);
				}else{
					sitePassword.setDisabled(true);
				}
			}else{
				sitePasswordCheck.setChecked(false);
				sitePassword.setValue("");
				sitePassword.setDisabled(true);
			}
			
		}
		
		
		
	}
	
	public void setupForm(){
		List<Site> siteList=siteDao.getSitesForUseracc(HttpHelper.getUser().getUserId());
		if(siteList.size()>0){
			site=siteList.get(0);
			if(site!=null){
				if(sitename!=null)
				sitename.setText(site.getName());
				if(siteurl!=null)
				siteurl.setText(site.getUrl());
				if(sitedesc!=null)
				sitedesc.setValue(site.getDescription());

				
				if(siteadminemail!=null)
				siteadminemail.setText(site.getAdminEmail());
				List<Radio> items=style.getItems();
				Integer s=site.getStyle();
				if(s!=null){
					for (Radio radio : items) {
						if(radio.getValue().equals(s.toString())){
							radio.setSelected(true);
						}
					}
				}
				boolean siteB=(site.getState() != null) && site.getState().equals(1);
				if(sitestate!=null){
					sitestate.setChecked(siteB);
				}
				boolean listedB=(site.getListed() != null) && site.getListed().equals(1);
				if(sitelisted!=null){
					sitelisted.setChecked(listedB);
				}
				if(enableabout!=null){
					boolean enableaboutB=(site.getEnableAbout() != null) && site.getEnableAbout().equals(1);
					boolean enablecontactB=(site.getEnableContact() != null) && site.getEnableContact().equals(1);
					enableabout.setChecked(enableaboutB);
					enablecontact.setChecked(enablecontactB);
				}
			}
		}
		onSitePasswordCheck(null);
	}



	public List<StyleConfig> getStyleConfigList() {
		return styleConfigList;
	}



	public void setStyleConfigList(List<StyleConfig> styleConfigList) {
		this.styleConfigList = styleConfigList;
	}


	public StyleConfigDao getStyleConfigDao() {
		return styleConfigDao;
	}


	public void setStyleConfigDao(StyleConfigDao styleConfigDao) {
		this.styleConfigDao = styleConfigDao;
	}



}	
