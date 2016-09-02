package org.mfr.data;

import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mfr.util.HttpHelper;
import org.mfr.web.action.GlobalVariableResolver;

public class SiteDao extends SiteHome{
	private static final Log log = LogFactory.getLog(SiteDao.class);
	
	public static boolean notIsPublicOrOwner(Site site){
		return site==null || ((site.getState()==null || !site.getState().equals(1)) && !GlobalVariableResolver.isSiteOwner());
	}
	public static boolean isSitePasswordRequired(Site site){
		if(site!=null && site.getPassword()!=null && site.getPassword().length()>0){
			return true;
		}
		return false;
	}
	public List<Site> getSitesForUseracc(Integer useracc) {
		log.debug("getting Site by user["+useracc+"]");
		try {
			List<Site> siteList =(List<Site>) entityManager.createQuery("from Site where owner.id=:userId").setParameter("userId", useracc).getResultList();
			log.debug("get successful siteList.size ["+siteList.size()+"]");
			return siteList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public Site getMainSiteForUseracc(Integer useracc) {
		log.debug("getting getMainSiteForUseracc["+useracc+"]");
		try {
			List<Site> site =(List<Site>) entityManager.createQuery("from Site where owner.id=:userId order by id desc").setParameter("userId", useracc).getResultList();
			if(site.size()>0){
				log.debug("get site successful");
				return site.get(0); 
			}
			log.debug("site not found");
			return null;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}	
	public Site getSiteForUrl(String url) {
		log.debug("getting Site for url["+url+"]");
		try {
			Site site=(Site) entityManager.createQuery("from Site where url=:url").setParameter("url", url).getSingleResult();
			log.debug("get site successful");
			return site;
		//} catch (NoResultException e){
		} catch (RuntimeException re) {
			log.error("get failed", re);
		}
		return null;
	}
	public List<Site> getLastSites(int count) {
		log.debug("getLastSites");
		try {
			List<Site> site=(List<Site>) entityManager.createQuery("from Site as s left join fetch s.owner where s.state=1 and listed=1 order by s.modifyDt desc").setMaxResults(count).getResultList();
			log.debug("get site list successful");
			return site;
		} catch (RuntimeException re) {
			log.error("get failed", re);
		}
		return null;
	}
	public List<Site> getTopSites(int count) {
		log.debug("getLastSites");
		try {
			List<Site> site=(List<Site>) entityManager.createQuery("from Site as s left join fetch s.owner where s.state=1 and listed=1 order by s.rank desc, s.modifyDt desc").setMaxResults(count).getResultList();
			log.debug("get site list successful");
			return site;
		} catch (RuntimeException re) {
			log.error("get failed", re);
		}
		return null;
	}
	
}
