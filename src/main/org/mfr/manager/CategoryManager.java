package org.mfr.manager;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.Useracc;
import org.mfr.mybatis.impl.CategoryDaoMapper;
import org.mfr.mybatis.types.MCategory;
import org.mfr.util.CallbackEvent;
import org.mfr.util.EventData;
import org.mfr.util.HttpHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

public class CategoryManager {
	private static final Log logger = LogFactory.getLog(CategoryManager.class);
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private CategoryDaoMapper categoryDaoMapper;
	
	public void createCategory(final String name,
			final String description,
			final boolean ispublic,
			final boolean requestPublic,
			final boolean blog,
			final boolean allowDownload,
			final boolean showComment,
			final Integer sortMode,
			final Integer sortDir,
			final CallbackEvent callbackEvent){
		final Useracc useracc=org.mfr.util.HttpHelper.getUser().getUseracc();
		List<Category> cat=categoryDao.findByOwnerAndName(useracc.getId(),name);
		if(cat.size()>0){
			org.mfr.util.ZkUtil.messageBoxConfirm("create.album.confirm.question",new Object[]{name},new EventListener(){
				public void onEvent(Event e){
					Integer data=(Integer)e.getData();
		                switch (data) {
		                	case Messagebox.OK: //OK is clicked
		                		Category category=new Category();
		                		updateCategory(category,name, description, ispublic, requestPublic,blog,
		            					allowDownload,showComment, useracc,sortMode,sortDir);
		                		categoryDao.persist(category);
		                		callbackEvent.onEvent(new EventData(category));
		                		break;
		                	case Messagebox.CANCEL: //Cancel is clicked
		                		break;
		                }
	            }				
			});
		}else{
			Category category=new Category();
			updateCategory(category,name, description, ispublic, requestPublic,blog,
					allowDownload, showComment,useracc,sortMode,sortDir);
			categoryDao.persist(category);
			callbackEvent.onEvent(new EventData(category));
		}
	}
	public void updateCategory(Category category,
			String name, 
			String description,
			boolean ispublic, 
			boolean requestPublic, 
			boolean blog, 
			boolean allowDownload,
			boolean showComment,
			Useracc useracc,Integer sortMode,Integer sortDir) {
		category.setUseracc(useracc);
		category.setName(name);
		category.setOwner(useracc.getLogin());
		category.setDescription(description);
		category.setIspublic(ispublic?2:0);
		category.setRecommend(requestPublic?1:0);
		category.setAllowDownload(allowDownload?1:0);
		category.setBlog(blog?1:0);
		category.setShowComment(showComment?1:0);
		category.setSort(sortMode);
		category.setSortDir(sortDir);
	}
	
	public void deleteCategory(Category category){
		category=categoryDao.merge(category);
		categoryDao.remove(category);
	}
	public void mergeCategory(Category category){
		categoryDao.merge(category);
	}
	public CategoryDao getCategoryDao() {
		return categoryDao;
	}

	public void setCategoryDao(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}
	public List<Category> findPublicTopAlbums(int count){
		return categoryDao.findPublicTopAlbums(count);
	}
	public void incrementAccessCount(Category category){
		if(!HttpHelper.isGoogleCrawler()){
			Integer accessCount=category.getAccessCount();
			accessCount=(accessCount==null?1:accessCount+1);
			category.setAccessCount(accessCount);
			categoryDao.merge(category);
		}
	}
	public void incrementAccessCount(MCategory category){
		if(!HttpHelper.isGoogleCrawler()){
			Integer accessCount=category.getAccessCount();
			accessCount=(accessCount==null?1:accessCount+1);
			category.setAccessCount(accessCount);
			categoryDaoMapper.incrementAccessCount(category);
		}
	}
	
	
	public static String getCategoryName(Category category){
		if(category==null){
			return "";
		}
		if(CategoryDao.DEFAULT_CATEGORY.equals(category.getName())){
			return Labels.getLabel("default.album");
		}
		return category.getName();
	}
	public static String getCategoryOwnerName(Category category){
		if(category==null){
			return "";
		}
		return category.getUseracc().getName();
	}
	
}
