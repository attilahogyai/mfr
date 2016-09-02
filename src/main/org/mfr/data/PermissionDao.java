package org.mfr.data;

import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jruby.ast.BeginNode;
import org.mfr.data.Category;
import org.mfr.data.Permission;
import org.mfr.data.PermissionHome;
import org.mfr.data.Useracc;

public class PermissionDao extends PermissionHome {
	private static final Log log = LogFactory.getLog(PermissionDao.class);
	public Permission getPermission(String ticket){
		try {
			log.debug("get permissions ticket["+ticket+"]");
			Permission permission =(Permission) entityManager.createQuery("from Permission where ticket=:ticket and (validTill is null or validTill>=current_date())")
			.setParameter("ticket", ticket).getSingleResult();
			return permission;
		} catch (NoResultException e){
			log.debug("no result");
			return null;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}		
	public Permission getPermission(Useracc useracc,Category category){
		try {
			log.debug("get permissions useracc["+useracc.getLogin()+"] category["+category.getName()+"]");
			Permission permission =(Permission) entityManager.createQuery("from Permission where useracc=:useracc and category=:category and (validTill is null or validTill>=current_date())")
			.setParameter("useracc", useracc)
			.setParameter("category", category).getSingleResult();
			return permission;
		} catch (NoResultException e){
			log.debug("no result");
			return null;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public List<Permission> getPermissions(Category category){
		log.debug("getting permissions for category["+category.getId()+"]");
		try {
			List<Permission> permissionList =(List<Permission>) entityManager.createQuery("from Permission where category=:category order by modifyDt desc").setParameter("category", category).getResultList();
			log.debug("get successful permissionList.size ["+permissionList.size()+"]");
			return permissionList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}	
	public Permission getDirectLink(Category category){
		log.debug("getting permissions for category["+category.getId()+"]");
		try {
			Permission directLink =(Permission) entityManager.createQuery("from Permission where sentTo='direct@link' and " +
					"category=:category order by modifyDt desc").setParameter("category", category).getSingleResult();
			log.debug("get successful getDirectLink ["+directLink+"]");
			return directLink;
		} catch (NoResultException re) {
			log.debug("get failed", re);
			return null;
		}
	}	
	
	public List<Permission> getSimilarPermissions(Permission p){
		log.debug("getting similar Permissions for["+p.getSentTo()+"]");
		try {
			List<Permission> permissionList =(List<Permission>) entityManager.createQuery("from Permission where sentTo=:sentTo order by modifyDt").setParameter("sentTo", p.getSentTo()).getResultList();
			log.debug("get successful permissionList.size ["+permissionList.size()+"]");
			return permissionList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}	
	
	public List<Category> getSharedCategory(Useracc useracc){
		log.debug("get shared categories["+useracc.getName()+"]");
		try {
			List<Category> categoryList =(List<Category>) entityManager.createQuery("select p.category from Permission p where p.assignedUseracc=:assigned").setParameter("assigned", useracc).getResultList();
			log.debug("get successful categoryList.size ["+categoryList.size()+"]");
			return categoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
		
	}
	
	public List<Object[]> matchBeginingNameEmail(String begining, Useracc useracc,int max){
		log.debug("getting matched Permissions for["+begining+"]");
		try {
			List<Object[]> permissionList =(List<Object[]>) entityManager.createQuery("select distinct name,sentTo from Permission p where (p.category.useracc=:useracc or p.useracc=:useracc) and (upper(p.sentTo) like :startStr or upper(p.name) like :startStr)").setParameter("startStr", begining.toUpperCase()+"%").
					setParameter("useracc", useracc).setMaxResults(max).getResultList();
			log.debug("get successful matchBeginingNameEmail.size ["+permissionList.size()+"]");
			return permissionList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
}

