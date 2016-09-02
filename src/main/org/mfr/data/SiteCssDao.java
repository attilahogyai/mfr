package org.mfr.data;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SiteCssDao extends SiteCssHome {
	private static final Log log = LogFactory.getLog(SiteCssDao.class);
	public List<Css> findCssBySite(Integer site){
		log.debug("findCssBySite site["+site+"]");
		try {
			List<Css> css = (List<Css>)entityManager.createQuery("select cs.css from SiteCss cs where cs.site.id=:siteId").
					setParameter("siteId", site).
					getResultList();
			return css;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}	
	}

}
