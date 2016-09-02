package org.mfr.data;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DictionaryDao extends DictionaryHome {
	private static final String FINDBYKEY="Dictionary.findByKey";
	private static final String FINDBYKEYSITETEMPLATE="Dictionary.findByKeySiteTemplate";
	private static final String FINDBYSITE="Dictionary.findBySite";
	private static final String KEY="key";
	private static final String SITE="site";
	private static final String TEMPLATE="template";
	
	private static final Log log = LogFactory.getLog(DictionaryDao.class);
	public List<Dictionary> findByKey(String key) {
		log.debug("getting Dictionary instance with key: " + key);
		try {
			List<Dictionary> instance = (List<Dictionary>)entityManager.createNamedQuery(FINDBYKEY, Dictionary.class).setParameter(KEY, key).getResultList();
			log.debug("get successful count["+instance.size()+"]");
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public Dictionary findByKeySiteTemplate(String key,Integer site,Integer template) {
		log.debug("getting Dictionary instance findByKeyLangSite key: " + key+" site:"+site);
		try {
			List<Dictionary> instance = entityManager.createNamedQuery(FINDBYKEYSITETEMPLATE, Dictionary.class).
					setParameter(KEY, key).
					setParameter(TEMPLATE, template).
					setParameter(SITE, site).getResultList();
			log.debug("get successful");
			if(instance.size()>1){
				return instance.get(0).getSite()!=null?instance.get(0):instance.get(1);
			}else if (instance.size()==1){
				return instance.get(0);
			}else{
				return null;
			}
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	public List<Dictionary> findBySite(Integer site) {
		log.debug("getting Dictionary instance findBySite site:"+site);
		try {
			List<Dictionary> dct = (List<Dictionary>)entityManager.createNamedQuery(FINDBYSITE, Dictionary.class).setParameter(SITE, site).getResultList();
			log.debug("get successful");
			return dct;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
}
