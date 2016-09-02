package org.mfr.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.internal.SessionImpl;
import org.hibernate.type.IntegerType;
import org.hibernate.type.ObjectType;
import org.hibernate.type.Type;
import org.mfr.util.HttpHelper;
import org.mfr.util.ObjectHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zkoss.zul.Listitem;

@Controller
public class CategoryDao extends CategoryHome {
	private static final Log log = LogFactory.getLog(CategoryDao.class);
	public static final String DEFAULT_CATEGORY="default";
	
	public static final Integer SORTING_PHOTO_TAKEN=0;
	public static final Integer SORTING_PHOTO_NAME=1;
	public static final Integer SORTING_PHOTO_UPLOAD_DATE=2;
	public static final Integer SORTING_ASC=0;
	public static final Integer SORTING_DESC=1;
	
	private static Comparator<Category> categoryComp=new Comparator<Category>(){
		@Override
		public int compare(Category o1, Category o2) {
			if(o1.getName().equals(DEFAULT_CATEGORY)) return -1;
			if(o2.getName().equals(DEFAULT_CATEGORY)) return 1;
			return o1.getName().compareTo(o2.getName());
		}
	};
	@RequestMapping(method=RequestMethod.GET, value="/albumowner/{owner}")
	public @ResponseBody List<Category> findByOwner(@PathVariable String owner) {
		return findByOwner(owner,"modifyDt desc");
	}
	public List<Category> findByOwner(String owner,String orderbyField) {
		log.debug("getting Categories by owner["+owner+"]");
		try {
			List<Category> categoryList =(List<Category>) entityManager.createQuery("from Category where owner=:ownerInput order by "+orderbyField).setParameter("ownerInput", owner).getResultList();
			Collections.sort(categoryList, categoryComp);
			log.debug("get successful categoryList.size ["+categoryList.size()+"]");
			return categoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public List<Category> findByOwnerAndName(Integer useracc,String name) {
		log.debug("getting Category by owner["+useracc+"] and name["+name+"]");
		try {
			List<Category> categoryList =(List<Category>) entityManager.createQuery("from Category where name=:name and useracc.id=:useracc")
			.setParameter("useracc", useracc)
			.setParameter("name", name)
			.getResultList();
			log.debug("get successful");
			return categoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}


	public List<Category> findByPermissionFetchPhotoCategory(Useracc useracc,String orderbyField) {
		log.debug("findByOwnerFetchPhotoCategory owner["+useracc.getLogin()+"]");
		try {
			List<Object[]> categoryListResult =(List<Object[]>) entityManager.createQuery("select p.category,(select count(pc.id) from PhotoCategory pc where pc.category=p.category) from Permission p where p.assignedUSeracc=:ownerInput order by c."+orderbyField).setParameter("ownerInput", useracc).getResultList();
			List <Category>categoryList=new LinkedList<Category>();
			for (Object[] result : categoryListResult) {
				((Category)result[0]).setPhotoCount(((Long)result[1]).intValue());
				categoryList.add(((Category)result[0]));
			}
			//Collections.sort(categoryList, categoryComp);
			log.debug("get successful categoryList.size ["+categoryList.size()+"]");
			return categoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	public List<Category> findByOwnerFetchPhotoCategory(Useracc useracc,String orderbyField) {
		log.debug("findByOwnerFetchPhotoCategory owner["+useracc.getLogin()+"]");
		try {
			List<Object[]> categoryListResult =(List<Object[]>) entityManager.createQuery("select c,(select count(pc.id) from PhotoCategory pc where pc.category=c) from Category c where c.useracc=:ownerInput order by c."+orderbyField).setParameter("ownerInput", useracc).getResultList();
			List <Category>categoryList=new LinkedList<Category>();
			for (Object[] result : categoryListResult) {
				((Category)result[0]).setPhotoCount(((Long)result[1]).intValue());
				categoryList.add(((Category)result[0]));
			}
			//Collections.sort(categoryList, categoryComp);
			log.debug("get successful categoryList.size ["+categoryList.size()+"]");
			return categoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public List<Category> findByOwnerFetchPhotoCategory(Useracc useracc) {
		if(useracc==null){
			log.error("error fetching category list user is null");
			return null;
		}
		return findByOwnerFetchPhotoCategory(useracc,"name ");
	}
	
	public List<Object[]> findByUserId(Integer owner) {
		return findByUserId(owner,null);
	}
	public int findCategoryCount(String text) {
		text=text.replaceAll("'", "\\'");
		log.debug("findCategoryCount["+text+"]");
		try {
			Query query=entityManager.createQuery("select count(pc.category) from PhotoCategory pc " +
					" where (pc.category.ispublic=2 or pc.category.id in (select sg.category.id from SiteGalleries sg where sg.site.state=1) )" +
					" and pc.category in (from Category c where lower(c.name) like '%"+text.toLowerCase()+"%' or lower(c.useracc.name) like '%"+text.toLowerCase()+"%' or lower(c.description) like '%"+text.toLowerCase()+"%')");
			Long result=(Long)query.getSingleResult();
			return result.intValue();
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public Query findCategoryQuery(String text){
		text=text.replaceAll("'", "\\'");
		return entityManager.createQuery("select pc.category,pc.photo.id,pc.photo.name,pc.photo.path from PhotoCategory pc " +
				" where (pc.category.ispublic=2 or pc.category.id in (select sg.category.id from SiteGalleries sg where sg.site.state=1) ) and " +
				"pc.category in (from Category c where lower(c.name) like '%"+text.toLowerCase()+"%' or lower(c.useracc.name) like '%"+text.toLowerCase()+"%' or lower(c.description) like '%"+text.toLowerCase()+"%')" +
						" order by pc.category.id,pc.cover desc,pc.photo.taken desc,pc.modifyDt desc");
	}
	public List<Object[]> findCategory(String text) {
		log.debug("findCategory["+text+"]");
		List<Object[]> resultList=new ArrayList<Object[]>();
		try {
			List<Object[]> categoryList =(List<Object[]>)findCategoryQuery(text).
							getResultList();
			int categoryId=0;
			for (Object[] result : categoryList) {
				if(((Category)result[0]).getId()!=categoryId){
					categoryId=((Category)result[0]).getId();
					HttpHelper.PublicImageDesc pc=new HttpHelper.PublicImageDesc();
					pc.id=(Integer)result[1];
					pc.name=(String)result[2];
					pc.path=(String)result[3];
					result[1]=pc;
					resultList.add(result);
				}
			}
			
//			List<Category> categoryList =(List<Category> )entityManager.createQuery("from Category c" +
//					" where c.name like '%"+text+"%' or c.useracc.name like '%"+text+"%' or c.description like '%"+text+"%' order by c.modifyDt desc").getResultList();
			if(resultList.size()>200){
				log.warn("WARNING there are more that 200 albums in search resuét",new Exception("WARNING there are more that 200 albums in search results"));
			}
			log.debug("get successful categoryList.size ["+resultList.size()+"]");
			return resultList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}

	}

	public List<Object[]> findByUserId(Integer owner,Integer maxResult) {
		log.debug("getting Categories by findByUserId["+owner+"]");
		try {
			SessionImpl hibernateSession=(SessionImpl)entityManager.getDelegate();
			org.hibernate.Query query=hibernateSession.createSQLQuery("select {cat.*},count(pc.id)  as photoCount from mfr.photo_category pc right " +
					"join mfr.category cat on pc.category=cat.id where " +
					"cat.useracc=:ownerInput group by "+ObjectHelper.getJpaFieldList("cat.", Category.class)+" order by cat.name").addEntity("cat",Category.class)
					.addScalar("photoCount", IntegerType.INSTANCE)
					.setParameter("ownerInput", owner);
			if(maxResult!=null){
				query.setMaxResults(maxResult);
			}
			List<Object[]> categoryList =query.list();
			log.debug("get successful categoryList.size ["+categoryList.size()+"]");
			return categoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}	
	public List<Category> findByUseracc(Useracc useracc) {
		return findByUseracc(useracc,"modifyDt desc");
	}
	public List<Category> findByUseracc(Useracc useracc,String orderbyField) {
		log.debug("getting Categories by useracc["+useracc.getId()+"]");
		try {
			List<Category> categoryList =(List<Category>) entityManager.createQuery("from Category where useracc=:userInput order by "+orderbyField).setParameter("userInput", useracc).getResultList();
			log.debug("get successful categoryList.size ["+categoryList.size()+"]");
			return categoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	public List<Category> findPublicTopAlbums(int maxCount) {
		try {
			List<Category> categoryList=new LinkedList<Category>();
			
			Query query=entityManager.createNativeQuery("select distinct c.id,c.name,c.useracc,ua.name as uname,c.modify_dt from mfr.category c, " +
					"mfr.photo_category pc, mfr.useracc ua where pc.category=c.id and c.ispublic=2" +
					" and c.useracc=ua.id order by c.modify_dt desc limit "+maxCount);
			List<Object> resultList =query.getResultList();			
			for (Object object : resultList) {
				Object []row=(Object[])object;
				Integer id=(Integer)row[0];
				String name=(String)row[1];
				Category cat=new Category();
				cat.setId(id);
				cat.setName(name);
				Useracc ua=new Useracc();
				ua.setId((Integer)row[2]);
				ua.setName((String)row[3]);
				cat.setUseracc(ua);
				categoryList.add(cat);
			}
			log.debug("get successful public categoryList.size ["+categoryList.size()+"]");
			return categoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public List<Category> findPublicAlbums(int maxCount) {
		try {
			List<Category> categoryList=new LinkedList<Category>();
			
			
			SessionImpl hibernateSession=(SessionImpl)entityManager.getDelegate();
			org.hibernate.Query query=hibernateSession.createSQLQuery("select {cat.*},{ua.*},count(1) as photoCount  from mfr.photo_category pc,mfr.category cat,mfr.useracc ua " +
					"where cat.ispublic=2 and pc.category=cat.id and cat.useracc=ua.id group by "+ObjectHelper.getJpaFieldList("cat.", Category.class)+","+ObjectHelper.getJpaFieldList("ua.", Useracc.class)+" order by cat.modify_dt desc, cat.create_dt " +
							"desc")
					.addEntity("cat",Category.class)
					.addEntity("ua",Useracc.class)
					.addScalar("photoCount", IntegerType.INSTANCE)
					.setMaxResults(maxCount);
			List<Category> resultList =query.list();			
			
//			List<Object> resultList =(List<Object>) entityManager.createQuery("select {c.*},count(1) as photoCount  from mfr.photo_category pc,mfr.category cat where cat.ispublic=2 and pc.category=cat.id group by "+ObjectHelper.getJpaFieldList("cat.", Category.class)+" order by cat.create_dt desc,cat.modify_dt desc").setMaxResults(maxCount).getResultList();
			for (Object object : resultList) {
				Category cat=(Category)((Object[])object)[0];
				Useracc ua=(Useracc)((Object[])object)[1];
				Integer count=(Integer)((Object[])object)[2];
				if(count>0){
					categoryList.add(cat);
				}
			}
			log.debug("get successful public categoryList.size ["+categoryList.size()+"]");
			return (List)categoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public Category findDefaultByOwner(String owner) {
		log.debug("getting Categories by owner["+owner+"]");
		try {
			Category category =(Category) entityManager.createQuery("from Category where owner=:ownerInput and name='default'").setParameter("ownerInput", owner).getSingleResult();
			log.debug("get single successful");
			return category;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public Category getPublicVisibleCategoryForId(Integer categoryId){
		log.debug("getting public category["+categoryId+"]");
		List<Category> category=null;
		try {
			category =(List<Category>) entityManager.createQuery("select pc from Category pc left join fetch pc.useracc where (pc.ispublic!=2 and pc.id=:categoryId) or 1<=(select count(*) from SiteGalleries sg where sg.category.id=:categoryId)").setParameter("categoryId", categoryId).getResultList();
			if(category.size()>0){
				log.debug("get successful photo ["+category.get(0).getName()+"]");
				return category.get(0); 
			}
			return null;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public List<Object[]> getSiteCategoriesForUser(Integer userId,Integer siteId){
		
		log.debug("getting Categories by findByUserId["+siteId+"]");
		try {
			SessionImpl hibernateSession=(SessionImpl)entityManager.getDelegate();
			org.hibernate.Query query=hibernateSession.createSQLQuery("select {cat.*},count(1) as photoCount  from mfr.photo_category pc,mfr.category cat where " +
					"pc.category=cat.id and cat.id in (select category from mfr.site_galleries where category is not null and site=:siteid) group by "+ObjectHelper.getJpaFieldList("cat.", Category.class)).addEntity("cat",Category.class)
					.addScalar("photoCount", IntegerType.INSTANCE)
					.setParameter("siteid", siteId);
			List<Object[]> categoryList =query.list();
			log.debug("get successful categoryList.size ["+categoryList.size()+"]");
			return categoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
//		
//		
//		
//		log.debug("getting public userId["+userId+"] siteId["+siteId+"]");
//		List<Category> category=null;
//		try {
//			category =(List<Category>) entityManager.createQuery("select pc from Category pc left join fetch pc.useracc where pc.useracc.id=:userId and " +
//					"1<=(select count(*) from SiteGalleries sg where sg.site.id=:siteId and sg.category=pc.id and pc.useracc.id=:userId) order by pc.name")
//					.setParameter("siteId", siteId).setParameter("userId", userId).getResultList();
//			log.debug("get successful categories ["+category.size()+"]");
//			return category;
//		} catch (RuntimeException re) {
//			log.error("get failed", re);
//			throw re;
//		}
	}
	public Category findCategoryByUserAndPath(Integer user,String path){
		log.debug("getting Category by owner["+user+"] and path["+path+"]");
		try {
			Category category =(Category) entityManager.createQuery("from Category where useracc.id=:ownerInput and path=:path").setParameter("ownerInput", user).setParameter("path", path).getSingleResult();
			log.debug("get single successful");
			return category;
		} catch (NoResultException e){
			log.debug("Category not found for user["+user+"] path["+path+"]");
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
		return null;
	}
	
	
	public static Integer findIndex(List<Listitem> categories, Integer id){
		int i=0;
		for (Listitem object : categories) {
			if(object.getValue().equals(id)){
				return i;
			}
			i++;
		}
		return i;
	}
	
	
	
	@Override
	public void persist(Category transientInstance) {
		transientInstance.setModifyDt(new Date());
		super.persist(transientInstance);
	}
	@Override
	public Category merge(Category detachedInstance) {
		detachedInstance.setModifyDt(new Date());
		return super.merge(detachedInstance);
	}
}
