package org.mfr.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.NoResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mfr.util.ZkUtil;
import org.mfr.data.PhotoCategory;
import org.mfr.data.PhotoCategoryHome;

public class PhotoCategoryDao extends PhotoCategoryHome {
	private static final Log log = LogFactory.getLog(PhotoCategoryDao.class);
	private static List<PhotoCategory> resultList = null;
	private static long lastRandomCached=0;
	public List<PhotoCategory> findByCategory(String albumCode) {
		log.debug("getting Photos by category["+albumCode+"]");
		try {
			List<PhotoCategory> photoCatgoryList =(List<PhotoCategory>) entityManager.createQuery("from PhotoCategory where category.name=:categoryInput").setParameter("categoryInput", albumCode).getResultList();
			log.debug("get successful categoryList.size ["+photoCatgoryList.size()+"]");
			return photoCatgoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public void updateCoverImage(Integer photoId,Integer albumId){
		log.debug("updateCoverImage ["+photoId+"] categories["+albumId+"]");
		try {
			int result=entityManager.createQuery("update PhotoCategory set cover=1 where category.id=:category and photo.id=:photo").
					setParameter("category", albumId).setParameter("photo", photoId).executeUpdate();
			int result2=entityManager.createQuery("update PhotoCategory set cover=0 where category.id=:category and photo.id!=:photo").
			setParameter("category", albumId).setParameter("photo", photoId).executeUpdate();
			log.debug("update result ["+result+"|"+result2+"] res");
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public List<PhotoCategory> findByPhoto(Integer photoId) {
		log.debug("getting PhotoCategory by photoId["+photoId+"]");
		try {
			List<PhotoCategory> photoCatgoryList =(List<PhotoCategory>) entityManager.createQuery("from PhotoCategory where photo.id=:photoId").setParameter("photoId", photoId).getResultList();
			log.debug("get successful PhotoCategory.size ["+photoCatgoryList.size()+"]");
			return photoCatgoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public Photo getCoverPhoto(Integer albumId){
		log.debug("get Cover photo["+albumId+"]");
		try {
			List<Photo> photoCatgoryList =(List<Photo>) entityManager.createQuery("select pc.photo from PhotoCategory pc where pc.category.id=:categoryInput order by pc.cover desc,pc.photo.taken desc,pc.modifyDt desc").
					setMaxResults(2).
					setParameter("categoryInput", albumId).getResultList();
			log.debug("get cover successful photos.size ["+photoCatgoryList.size()+"]");
			if(photoCatgoryList.size()>1){
				log.warn("more than one cover photo");
			}
			if(photoCatgoryList.size()>0){
				return photoCatgoryList.get(0);
			}
			return null;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
		
	}
	public List<PhotoCategory> findByCategoryId(Integer albumId) {
		log.debug("getting Photos by categoryId["+albumId+"]");
		try {
			List<PhotoCategory> photoCatgoryList =(List<PhotoCategory>) entityManager.createQuery("from PhotoCategory where category.id=:categoryInput order by photo.taken desc,modifyDt desc").setParameter("categoryInput", albumId).getResultList();
			log.debug("get successful photos.size ["+photoCatgoryList.size()+"]");
			return photoCatgoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public List<Photo> findPhotosByCategoryId(Category category) {
		log.debug("getting Photos by categoryId["+category.getId()+"]");
		try {
			String[] sortProps=getSortDefinition(category.getSort(), category.getSortDir());
			
			List<Photo> photoCatgoryList =(List<Photo>) entityManager.createQuery("select pc.photo  from PhotoCategory pc left join fetch pc.photo.exifData left join fetch pc.photo.useracc where pc.category.id=:categoryInput order by "+sortProps[0]+" "+sortProps[1]).setParameter("categoryInput", category.getId()).getResultList();
			log.debug("get successful photos.size ["+photoCatgoryList.size()+"]");
			return photoCatgoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public static String[] getSortDefinition(Integer sortOrder,Integer sordDirection) {
		String[] sortProps=new String[2];
		if(sortOrder==null || CategoryDao.SORTING_PHOTO_TAKEN.equals(sortOrder)){
			sortProps[0]="pc.photo.taken";
		}else if(CategoryDao.SORTING_PHOTO_NAME.equals(sortOrder)){
			sortProps[0]="pc.photo.name";
		}else if(CategoryDao.SORTING_PHOTO_UPLOAD_DATE.equals(sortOrder)){
			sortProps[0]="pc.modifyDt";
		}
		if(sordDirection==null || CategoryDao.SORTING_ASC.equals(sordDirection)){
			sortProps[1]="asc";
		}else if(CategoryDao.SORTING_DESC.equals(sordDirection)){
			sortProps[1]="desc";
		}
		return sortProps;
	}
	
	
	
	public List<PhotoCategory> getByCategoryList(List<Category> categories) {
		log.debug("getting Photos by categories["+categories.size()+"]");
		try {
			List<PhotoCategory> photoCatgoryList =(List<PhotoCategory>) entityManager.createQuery("from PhotoCategory where category in (:categorylist) order by category,modifyDt desc").setParameter("categorylist", categories).getResultList();
			log.debug("get successful photos.size ["+photoCatgoryList.size()+"]");
			return photoCatgoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	
	
	public Long countByCategoryId(Integer albumId) {
		log.debug("getting countByCategoryId["+albumId+"]");
		Long count=0L;
		try {
			count =(Long) entityManager.createQuery("select count(*) from PhotoCategory where category.id=:categoryInput").setParameter("categoryInput", albumId).getSingleResult();
			log.debug("get successful count ["+count+"]");
			return count;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public Photo getPublicVisiblePhotoForId(Integer photoId){
		log.debug("getting public getPublicVisiblePhotoForId["+photoId+"]");
		List<Photo> photos=null;
		try {
			photos =(List<Photo>) entityManager.createQuery("select pc.photo from PhotoCategory pc where pc.photo.id=:photoId and (pc.category.ispublic=2 or 1<=(select count(*) from SiteGalleries sg where sg.site.state=1 and (sg.category=pc.category or sg.photo.id=:photoId)) )").setParameter("photoId", photoId).getResultList();
			if(photos.size()>0){
				log.debug("get successful photo name ["+photos.get(0).getName()+"]");
				return photos.get(0); 
			}
			return null;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	
	public List<PhotoCategory> findRecommendedRandomImages(int maxRandom) {
		log.debug("getting random photos["+maxRandom+"]");
		if(resultList==null || resultList.size()==0 || (System.currentTimeMillis()-lastRandomCached)>360000){
			try {
				
				Object []r = (Object[])entityManager.createQuery("select count(*),max(id),min(id) from PhotoCategory where category.recommend=2").getSingleResult();
				resultList = new ArrayList<PhotoCategory>(); 
				if((Long)r[0]>0){
					int max=(Integer)r[1];
					int min=(Integer)r[2];
					if(max<maxRandom){
						maxRandom=max;
					}
					for (int i = 0; resultList.size()< maxRandom && i<maxRandom*2; i++) {
						int rv=ZkUtil.getRandomInt((Long)r[0]);
						List<PhotoCategory> fc = entityManager.createQuery("from PhotoCategory where category.recommend=2").setFirstResult(rv).setMaxResults(1).getResultList();
						
						boolean alreadyexist=false;
						for (int j = 0; j < resultList.size(); j++) {
							if(resultList.get(j).getPhoto().getId().equals(fc.get(0).getPhoto().getId())){
								alreadyexist=true;
							}
						}
						if(!alreadyexist){
							resultList.addAll(fc);
						}
					}
				}
				lastRandomCached=System.currentTimeMillis();
				log.debug("get successful resultList.size ["+resultList.size()+"]");
				return resultList;
			} catch (RuntimeException re) {
				log.error("get failed", re);
				throw re;
			}
		}else{
			log.debug("return cached items");
			return resultList;
		}
	}
	public PhotoCategory findByCategoryAndPhoto(Integer albumId,Integer photoId){
		try {
			PhotoCategory photoCatgoryList =(PhotoCategory)entityManager.createQuery("from PhotoCategory where category.id=:categoryInput and photo.id=:photoId")
			.setParameter("categoryInput", albumId)
			.setParameter("photoId", photoId).getSingleResult();
			return photoCatgoryList;
		} catch (NoResultException re) {
			log.warn("no result found "+re.getMessage());
		}
		return null;
	}
	public PhotoCategory findByCategoryAndPhotoProviderPath(Integer albumId,String path){
		try {
			PhotoCategory photoCatgoryList =(PhotoCategory)entityManager.createQuery("from PhotoCategory where category.id=:categoryInput and photo.providerPath=:path")
			.setParameter("categoryInput", albumId)
			.setParameter("path", path).getSingleResult();
			return photoCatgoryList;
		} catch (NoResultException re) {
			log.warn("no result found "+re.getMessage());
		}
		return null;
	}

	
	public List<PhotoCategory> findByUserId(Integer userId) {
		log.debug("getting PhotoCategory by userid["+userId+"]");
		try {
			List<PhotoCategory> photoCatgoryList =(List<PhotoCategory>) entityManager.createQuery("from PhotoCategory where category.useracc.id=:userId").setParameter("userId", userId).getResultList();
			log.debug("get successful PhotoCategory.size ["+photoCatgoryList.size()+"]");
			return photoCatgoryList;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

}
