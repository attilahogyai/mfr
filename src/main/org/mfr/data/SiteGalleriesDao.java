package org.mfr.data;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SiteGalleriesDao extends SiteGalleriesHome{
	public static final Integer MAINIMAGE=1;
	public static final Integer MAINALBUMLIST=2;
	public static final Integer ABOUT=3;
	public static final Integer GALLERY=4;
	
	private static final Log log = LogFactory.getLog(SiteGalleriesDao.class);
	public List<SiteGalleries> findSiteGalleriesForType(Integer site,Integer type){
		log.debug("getting SiteGalleries for site["+site+"] type["+type+"]");
		try {
			List<SiteGalleries> siteGalleriesList=null;
			siteGalleriesList =(List<SiteGalleries>) entityManager.createQuery("select g from SiteGalleries g where g.site.id=:siteId and g.type=:type").
					setParameter("siteId", site).setParameter("type", type).getResultList();
				log.debug("get successful SiteGalleries.size ["+siteGalleriesList.size()+"]");
			return siteGalleriesList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}

	}
	public List<Category> findSiteCategoriesForType(Integer site,Integer type){
	log.debug("getting Categories for site["+site+"] type["+type+"]");
	try {
		List<Category> siteGalleriesList=null;
		siteGalleriesList =(List<Category>) entityManager.createQuery("select g.category from SiteGalleries g where g.site.id=:siteId and g.type=:type order by g.id").
					setParameter("siteId", site).setParameter("type", type).getResultList();
				log.debug("get successful categories.size ["+siteGalleriesList.size()+"]");
		return siteGalleriesList;
	} catch (RuntimeException re) {
		log.error("get failed", re);
		throw re;
	}
	}
	public void deleteByCategorySiteType(Integer category, Integer site, Integer type){
		List<SiteGalleries> galleries=(List<SiteGalleries>) entityManager.createQuery("from SiteGalleries g where g.site.id=:siteId and g.type=:type and g.category.id=:category").
				setParameter("category", category).
				setParameter("siteId", site).setParameter("type", type).getResultList();
			log.debug("get successful categories.size ["+galleries.size()+"]");
		 for (SiteGalleries siteGalleries : galleries) {
			 remove(siteGalleries);
		}
	}

}
