package org.mfr.data;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CssDao extends CssHome {
	private static final Log log = LogFactory.getLog(CssDao.class);
	public List<Css> findByTargetObject(String target){
		log.debug("findByTargetObject target["+target+"]");
		try {
			List<Css> css = (List<Css>)entityManager.createQuery("from Css where targetObject=:target order by group desc").setParameter("target", target).setMaxResults(50).getResultList();
			return css;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public List<Css> findDefaultCssForTargetAndPage(String targetObject,String applyPage){
		log.debug("findDefaultCssForTargetAndPage applyPage["+applyPage+"] targetObject["+targetObject+"]");
		try {
			List<Css> css = (List<Css>)entityManager.createQuery("from Css where apply=:applyPage and def=1 and targetObject=:target order by group desc").
					setParameter("target", targetObject).
					setParameter("applyPage", applyPage).getResultList();
			return css;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
}
