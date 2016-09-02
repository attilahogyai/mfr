package org.mfr.data;

import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mfr.data.Useracc;
import org.mfr.data.UseraccData;
import org.mfr.data.UseraccDataHome;
import org.mfr.util.ZkUtil;

public class UseraccDataDao extends UseraccDataHome{
	public static final String IMAGE_VIEWER_TYPE="IMAGE_VIEWER_TYPE";
	public static final String ACCESS_TOKEN_SUFIX = "_access_token";
	public static final String REFRESH_TOKEN_SUFIX  = "_refresh_token";
	public static final String GOOGLE_ACCESS_TOKEN = "google_access_token";
	public static final String GOOGLE_REFRESH_TOKEN = "google_refresh_token";
	public static final String FACEBOOK_ACCESS_TOKEN = "facebook_access_token";

	
	private static final Log log = LogFactory.getLog(UseraccDataDao.class);
	
	public List<UseraccData> findUseraccData(Useracc useracc) {
		log.debug("getting UseraccData list with login: " + useracc.getLogin());
		try {
			List<UseraccData> instanceList =(List<UseraccData>) entityManager.createQuery("from UseraccData where useracc=:useracc").setParameter("useracc", useracc).getResultList();
			log.debug("get successful size["+instanceList.size()+"]");
			return instanceList;
		} catch (NoResultException re) {
			log.warn("get failed", re);
		}
		return null;
	}
	public UseraccData findUseraccData(String login,String key) {
		log.debug("getting UseraccData list with login: " + login+" key: "+key);
		try {
			List<UseraccData> instanceList =(List<UseraccData>) entityManager.createQuery("from UseraccData where useracc.login=:login and key=:key").setParameter("login", login).setParameter("key", key).getResultList();
			log.debug("get successful value["+instanceList.size()+"]");
			if(instanceList.size()>1){
				ZkUtil.logProcessError(new Exception("to many rowws for ["+login+"]["+key+"]"));
			}
			return instanceList.size()>0?instanceList.get(0):null;
		} catch (NoResultException re) {
			log.warn("get failed", re);
		}
		return null;
	}
}
