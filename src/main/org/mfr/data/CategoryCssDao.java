package org.mfr.data;

import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CategoryCssDao extends CategoryCssHome {
	private static final Log log = LogFactory.getLog(CategoryCssDao.class);
	public CategoryCss findByGroupAndAlbum(Integer group,Integer album){
		log.debug("findByGroupAndAlbum group["+group+"] album["+album+"]");
		try {
			CategoryCss css = (CategoryCss) entityManager.createQuery("from CategoryCss where category.id=:albumId and css.group=:groupId").setParameter("groupId", group).setParameter("albumId", album).getSingleResult();
			return css;
		} catch (NoResultException e) {
			log.debug("no result");
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
		return null;
		
	}
	public List<CategoryCss> findByAlbum(Integer album){
		log.debug("findByAlbum album["+album+"]");
		try {
			List<CategoryCss> css = (List<CategoryCss>)entityManager.createQuery("from CategoryCss where category.id=:albumId").setParameter("albumId", album).getResultList();
			return css;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}	
	}
	public List<Css> findCssByAlbumAndApply(Integer album,String apply){
		log.debug("findCssByAlbumAndApply album["+album+"] apply["+apply+"]");
		try {
			List<Css> css = (List<Css>)entityManager.createQuery("select cs.css from CategoryCss cs join cs.category as cat  where cat.id=:albumId and cs.css.apply=:applyPage").
					setParameter("albumId", album).
					setParameter("applyPage", apply).
					getResultList();
			return css;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}	
	}

}
