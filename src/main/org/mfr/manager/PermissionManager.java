package org.mfr.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.print.attribute.standard.PDLOverrideSupported;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mfr.data.PermissionDao;
import org.mfr.data.PhotoCategoryDao;
import org.mfr.util.HttpHelper;
import org.mfr.util.MailHelper;
import org.mfr.util.VelocityHelper;
import org.mfr.util.ZkUtil;
import org.mfr.data.Category;
import org.mfr.data.Permission;
import org.mfr.data.PhotoCategory;
import org.mfr.data.Useracc;
import org.mfr.image.ImageConfig;
import org.mfr.mybatis.impl.PermissionDaoMapper;
import org.mfr.mybatis.types.MCategory;
import org.mfr.mybatis.types.MPermission;
import org.mfr.mybatis.types.MUseracc;
import org.openid4java.message.MessageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

public class PermissionManager {
	private static final Log log = LogFactory.getLog(PermissionManager.class);
	@Autowired
	private PermissionDao permissionDao;
	@Autowired
	private PhotoCategoryDao photoCategoryDao;
	private PermissionDaoMapper permissionDaoMapper;
	
	public boolean isPermission(Category category, Useracc user){
		Permission permission=permissionDao.getPermission(user,category);
		if(permission!=null){
			Integer access=permission.getAccessCount();
			access=(access==null?0:access);
			access++;
			permission.setAccessCount(access);
		}
		return permission!=null;
	}
	public boolean isPermission(MCategory category, MUseracc user){
		MPermission permission=permissionDaoMapper.getPermission(user,category);
		if(permission!=null){
			permissionDaoMapper.updateAccessCount(permission);
		}
		return permission!=null;
	}
	
	public Permission isPermission(String ticket){
		Permission permission=permissionDao.getPermission(ticket);
		if(permission!=null){
			Integer access=permission.getAccessCount();
			access=(access==null?0:access);
			access++;
			permission.setAccessCount(access);
			permissionDao.merge(permission);
		}
		return permission;
	}
	public MPermission isMPermission(String ticket){
		MPermission permission=permissionDaoMapper.getPermission(ticket);
		if(permission!=null){
			permissionDaoMapper.updateAccessCount(permission);
		}
		return permission;
	}
	
	public String getDirectLick(Category category){
		Permission permission=permissionDao.getDirectLink(category);
		if(permission!=null){
			return permission.getTicket();
		}else{
			return null;
		}
	}
	public String createDirectLink(Category category){
		return createPermission(category, "direct@link", "Direct link", "", null, 0);
	}
	public boolean setPermission(Category category, String emailaddress,String name, String desc,Date validTill, Integer create){
		try{
			
			String ticket = createPermission(category, emailaddress, name,
					desc, validTill, create);
			Useracc useracc=org.mfr.util.HttpHelper.getUser().getUseracc();
			
			
			VelocityHelper.init(((HttpServletRequest)Executions.getCurrent().getNativeRequest()).getServletContext());
			final List<String> emailaddressList=new ArrayList<String>();
			emailaddressList.add(emailaddress);
			final Map<String,Object> arguments=MailHelper.prepareArgumentsForAlbum(null,null,category);
			arguments.put("recipientName", name);
			arguments.put("ownerName", useracc.getName());
			
			arguments.put("ticket", ticket);
			arguments.put("desc", desc);
			arguments.put("uploadEnabled", create==1);
			
			
			List<PhotoCategory> photos=photoCategoryDao.findByCategoryId(category.getId());
			
			List<File> imagesToEmail=new LinkedList<File>();
			List<String> imagesName=new LinkedList<String>();
			if(photos!=null && photos.size()>0){
				int images=3;
				for (PhotoCategory photoCategory : photos) {
					File f=new File(ImageConfig.THUMBCONFIG.getPath(photoCategory.getPhoto().getPath()));
					imagesToEmail.add(f);
					imagesName.add(HttpHelper.replacetoAsciiChars(f.getName()));
					images--;
					if(images==0){
						break;
					}
				}
			}else{
				log.error("something is not good there is not images in this Album ["+category.getId()+"]");
			}
			arguments.put("imagesName", imagesName);			
			MailHelper.sendMail(emailaddressList,"email/access.vm",new String[]{useracc.getEmail(),useracc.getName()},Labels.getLabel("privacy.access.email.subject"),false,MailHelper.CONTENTTYPE,arguments,imagesToEmail);
			Messagebox.show(Labels.getLabel("privacy.access.email.sent"), Labels.getLabel("information"), Messagebox.OK, Messagebox.INFORMATION);
			return true;
		}catch(Exception e){
			log.error("setPermission",e);
			ZkUtil.showProcessError(e);
			return false;
		}

	}
	public String createPermission(Category category, String emailaddress,
			String name, String desc, Date validTill, Integer create) {
		Permission p=new Permission();
		p.setCategory(category);
		p.setUseracc(category.getUseracc());
		p.setSentTo(emailaddress);
		p.setAllowUpload(create);
		String startS=new Long(System.currentTimeMillis()).toString();
		String ticket=startS+ZkUtil.getAlphaNumeric(32-startS.length());
		p.setTicket(ticket);
		p.setValidTill(validTill);
		p.setName(name);
		p.setDescription(desc);
		permissionDao.persist(p);
		return ticket;
	}
	
	public PermissionDetail isAllowed(Category category,String ticket){
		PermissionDetail pd=new PermissionDetail();
		boolean display=category!=null && (category.getIspublic()==null?false:category.getIspublic()==1);
		if(!display && org.mfr.util.HttpHelper.getUser()!=null && category!=null){
			display=isPermission(category,org.mfr.util.HttpHelper.getUser().getUseracc());
		}
		if(!display && ticket!=null){
			Permission p=isPermission(ticket);
			if(p!=null){
				category=p.getCategory();
				pd.setPermission(p);
				display=true;
			}
		}
		if(display){
			pd.setCategory(category);
			return pd;
		}
		return null;
	}
	public MPermissionDetail isAllowed(MCategory category,String ticket){
		MPermissionDetail pd=new MPermissionDetail();
		boolean display=category!=null && (category.getIspublic()==null?false:category.getIspublic()==1);
		if(!display && org.mfr.util.HttpHelper.getUser()!=null && category!=null){
			display=isPermission(category,HttpHelper.copyToMUser(HttpHelper.getUser().getUseracc()));
		}
		if(!display && ticket!=null){
			MPermission p=isMPermission(ticket);
			if(p!=null){
				category=p.getCategory();
				pd.setPermission(p);
				display=true;
			}
		}
		if(display){
			pd.setCategory(category);
			return pd;
		}
		return null;
	}	
	public boolean isMyPrivate(Category category){
		boolean isPrivate=false;
		if(org.mfr.util.HttpHelper.getUser()!=null && org.mfr.util.HttpHelper.getUser().getUseracc()!=null && category.getUseracc()!=null){
			isPrivate=org.mfr.util.HttpHelper.getUser().getUseracc().getId().equals(category.getUseracc().getId());
		}		
		return isPrivate;
	}
	public boolean isMyPrivate(MCategory category){
		boolean isPrivate=false;
		if(org.mfr.util.HttpHelper.getUser()!=null && org.mfr.util.HttpHelper.getUser().getUseracc()!=null && category.getUseracc()!=null){
			isPrivate=org.mfr.util.HttpHelper.getUser().getUseracc().getId().equals(category.getUseracc().getId());
		}		
		return isPrivate;
	}	
	public void incrementAccessCount(PermissionDetail permissionDetail){
		if(!HttpHelper.isGoogleCrawler()){
			Integer accessCount=permissionDetail.getPermission().getAccessCount();
			accessCount=(accessCount==null?1:accessCount+1);
			permissionDetail.getPermission().setAccessCount(accessCount);
			permissionDao.merge(permissionDetail.getPermission());
		}
	}
	public List<PermissionDetail> getSimilarPermissions(PermissionDetail p){
		List<Permission> permissionList=permissionDao.getSimilarPermissions(p.getPermission());
		List<PermissionDetail> permissionDetailList=new ArrayList<PermissionDetail>(permissionList.size());
		Set<Integer> categories=new HashSet<Integer>();
		categories.add(p.getCategory().getId());
		for (Permission permission : permissionList) {
			if(categories.add(permission.getCategory().getId())){
				PermissionDetail pd=new PermissionDetail();
				pd.setPermission(permission);
				pd.setCategory(permission.getCategory());
				permissionDetailList.add(pd);
			}
		}
		return permissionDetailList;
	}
	public PermissionDao getPermissionDao() {
		return permissionDao;
	}

	public void setPermissionDao(PermissionDao permissionDao) {
		this.permissionDao = permissionDao;
	}
	public PhotoCategoryDao getPhotoCategoryDao() {
		return photoCategoryDao;
	}
	public void setPhotoCategoryDao(PhotoCategoryDao photoCategoryDao) {
		this.photoCategoryDao = photoCategoryDao;
	}
	
}
