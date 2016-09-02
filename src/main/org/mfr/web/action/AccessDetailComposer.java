package org.mfr.web.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mfr.data.Category;
import org.mfr.data.Permission;
import org.mfr.data.PermissionDao;
import org.mfr.data.Useracc;
import org.mfr.data.UseraccDao;
import org.mfr.manager.PermissionManager;
import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AccessDetailComposer extends SelectorComposer<Component> {
	private static final Log log = LogFactory.getLog(AccessDetailComposer.class);
	@Wire
	private Label titleText; 
	@WireVariable
	private PermissionDao permissionDao;
	
	@WireVariable
	private PermissionManager permissionManager;
	
	private Window accessDetail;
	@Wire
	private Div olderPermissions;

	@Wire
	private Combobox emailaddress;
	@Wire
	private Textbox name;
	@Wire
	private Textbox desc;
	@Wire
	private Checkbox uploadAccess;
	
	@Wire
	private Textbox directlink;
	
	@Wire
	private A generateLink;
	
	private Category category;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		this.accessDetail=(Window)comp;
		boolean update=false;                                  
		category=(Category)Executions.getCurrent().getAttribute("category");
		int accessType=1;                      
		if(titleText!=null){
			titleText.setValue(Labels.getLabel("share")+" - "+category.getDisplayName());
		}

		generatePermissionsList();
		
		setupDirectLink();
	}
	public void setupDirectLink() throws WrongValueException {
		String directticket=permissionManager.getDirectLick(category);
		if(directticket!=null){
			String l=HttpHelper.getAlbumUrl(null,category.getDisplayName())+"?ticket="+directticket;
			directlink.setValue("http://"+GlobalVariableResolver.getDomain()+"/"+l);
			generateLink.setVisible(false);
		}
	}
	public void generatePermissionsList() {
		final List permissions=permissionDao.getPermissions(category);
		olderPermissions.getChildren().clear();
		if(permissions.size()>0){			
			Component [] comps=accessDetail.getTemplate("olderPermissions").create(olderPermissions,null,new org.zkoss.xel.VariableResolver(){
				public Object resolveVariable(String variable){
					if("permissions".equals(variable)){
						return permissions;
					}
					return null;
				}
			},null);
		}
	}
	public void closeWindow(){

	}
	
	public void deletePermission(final Component o){
			org.mfr.util.ZkUtil.messageBoxConfirm("privacy.delete.confirm",new Object[]{},new EventListener(){
			public void onEvent(Event e){
				Integer data=(Integer)e.getData();
	                switch (data) {
	                	case Messagebox.OK: //OK is clicked
 	   					try{
 	   						Integer id=(Integer)o.getAttribute("pid");
 	   						org.mfr.data.Permission p=permissionDao.findById(id);
 	   						permissionDao.remove(p);
 	   						generatePermissionsList();
 		 				}catch(Exception e1){
 		 					Messagebox.show(Labels.getLabel("image.process.error")+"["+e1.getMessage()+"]");
 		 				}
	                	break;
	                	case Messagebox.CANCEL: //Cancel is clicked
	                		break;
	                }
            }
		});
	}
	
	public void showHint(InputEvent event){
		emailaddress.getItems().clear();
		log.debug("show hint items cleared");
		String value=event.getValue();
		if(value!=null && value.length()>3){
			List<Object[]> hint=permissionDao.matchBeginingNameEmail(value,HttpHelper.getUser().getUseracc(), 5);
			log.debug("show hints["+hint.size()+"]");
			for (Object[] permission : hint) {
				if(permission[0]!=null){
					emailaddress.appendItem(permission[0]+"<"+permission[1]+">");
				}else{
					emailaddress.appendItem((String)permission[1]);
				}
			}
		}
	}
	@Listen("onClick=#generateLink")
	public void setDirectLink(){
        try{                                
            String ticket=permissionManager.createDirectLink(category);
            setupDirectLink();
        }catch(Exception e){
     	   org.mfr.util.ZkUtil.showProcessError(e);
        }
	}
	
	
	
	@Listen("onClick=#setAccess")
	public void setAccess(){
        try{                                
            if(emailaddress.isValid() && name.isValid()){
     		   if(permissionManager.setPermission(category, emailaddress.getValue(),name.getValue(),desc.getValue(), null,(uploadAccess==null || !uploadAccess.isChecked())?0:1)){
     			   closeWindow();
     		   }
            }
            
        }catch(Exception e){
     	   org.mfr.util.ZkUtil.showProcessError(e);
        }
	}
}
