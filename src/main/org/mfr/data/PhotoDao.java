package org.mfr.data;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mfr.data.PhotoHome;

public class PhotoDao extends PhotoHome {
	private static final Log log = LogFactory.getLog(PhotoDao.class);
	public Long getPhotoCount(Integer userid) {
		log.debug("getting getPhotoCount");
		try {
			Long photosNr = (Long)entityManager.createQuery("select count(*) from Photo where useracc.id=:userid").setParameter("userid", userid).getSingleResult();
			log.debug("photosNr["+photosNr+"]");
			return photosNr;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public List<Photo> getPhotos(String query){
		log.debug("getting getMainPhoto");
		try {
			List<Photo> photos = (List<Photo>)entityManager.createQuery(query).setMaxResults(1000).getResultList();
			return photos;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public Photo getMainPhoto(Integer userid){
		log.debug("getting getMainPhoto");
		try {
			List<Photo> photos = (List<Photo>)entityManager.createQuery("from Photo where useracc.id=:userid and exifData.imageWidth>exifData.imageHeight and exifData.imageWidth>910").setParameter("userid", userid).setMaxResults(1).getResultList();
			if(photos!=null && photos.size()==1){
				return photos.get(0);
			}
			return null;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public Long getUsedSpace(Integer userid){
		log.debug("getting used space");
		try {
			Long size = (Long)entityManager.createQuery("select sum(size) from Photo where useracc.id=:userid").setParameter("userid", userid).getSingleResult();
			log.debug("usedSpace["+size+"]");
			return size==null?0:size;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public int findPhotoCount(String searchText ){
		log.debug("find photoCount");
		try {
			Query query=entityManager.createQuery("select count(ph.useracc.name) from PhotoCategory pc left join pc.photo as ph " +
					"left join ph.useracc where " +
					"(pc.category.ispublic=2 or pc.category.id in (select sg.category.id from SiteGalleries sg where sg.site.state=1)) and (" +
					"lower(ph.name) like '%"+searchText.toLowerCase()+"%' or " +
					"lower(ph.description) like '%"+searchText.toLowerCase()+"%' or " +
					"lower(ph.useracc.name) like '%"+searchText.toLowerCase()+"%')");
			Long l=(Long)query.getSingleResult();
			return l.intValue();
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	private Query findPhotoQuery(String searchText){
		Query query=entityManager.createQuery("select ph from PhotoCategory pc left join pc.photo as ph left join fetch ph.exifData " +
				"left join fetch ph.useracc where " +
				"(pc.category.ispublic=2 or pc.category.id in (select sg.category.id from SiteGalleries sg where sg.site.state=1)) and (" +
				"lower(ph.name) like '%"+searchText.toLowerCase()+"%' or " +
				"lower(ph.description) like '%"+searchText.toLowerCase()+"%' or " +
				"lower(ph.useracc.name) like '%"+searchText.toLowerCase()+"%') order by ph.useracc.name,ph.name");
		return query;
	}
	public List<Photo> findPhoto(String searchText,int from,int limit){
		log.debug("find photo");
		try {
			Query searchQuery=findPhotoQuery(searchText);
			List<Photo> photos=searchQuery.setFirstResult(from).setMaxResults(limit).getResultList();
			log.debug("photos["+photos.size()+"]");
			return photos;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
}
