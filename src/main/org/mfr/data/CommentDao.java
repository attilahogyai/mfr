package org.mfr.data;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommentDao extends CommentHome {
	private static final Log log = LogFactory.getLog(CommentDao.class);
	public List<Comment> findByAlbum(Integer album, Integer readerUserId) {
		log.debug("getting Comments by album: " + album);
		try {
			List<Comment> instance = (List<Comment>)entityManager.createQuery("from Comment where category.id=:category and category.showComment=1 and (status is null or status=1 or category.useracc.id=:readerUserID) order by createDt desc").
					setParameter("readerUserID", readerUserId).
					setParameter("category", album).getResultList();
			log.debug("get successful count["+instance.size()+"]");
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public List<Comment> findByParent(Integer parent) {
		
		log.debug("getting Comments by parent: " + parent);
		
		try {
			List<Comment> instance = (List<Comment>)entityManager.createQuery("from Comment where original.id=:parent or id=:parent  order by createDt desc").
					setParameter("parent", parent).getResultList();
			log.debug("get successful count["+instance.size()+"]");
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
}
