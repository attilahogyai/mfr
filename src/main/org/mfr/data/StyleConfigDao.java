package org.mfr.data;

import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StyleConfigDao extends StyleConfigHome {
	private static final Log log = LogFactory.getLog(StyleConfigDao.class);
	public List<StyleConfig> findAll(){
		log.debug("getting all style");
		try {
			List<StyleConfig> instanceList =(List<StyleConfig>) entityManager.createQuery("from StyleConfig where state=1 order by listOrder").getResultList();
			log.debug("get successful size["+instanceList.size()+"]");
			return instanceList;
		} catch (NoResultException re) {
			log.warn("get failed", re);
		}
		return null;		
	}
}
