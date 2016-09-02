package org.mfr.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.mfr.data.Useracc;
import org.mfr.data.UseraccHome;
import org.mfr.util.ZkUtil;

public class UseraccDao extends UseraccHome {
	private static final Log log = LogFactory.getLog(UseraccDao.class);
	
	private static final String STATUS_ACTIVE="1"; // password confirmed
	private static final String STATUS_INACTIV="2";
	private static final String STATUS_REGISTERED="0";
	
	public Useracc findByLoginAndPassword(String login,String password) {
		log.debug("getting Useracc instance with login: " + login);
		try {
			Useracc instance =(Useracc) entityManager.createQuery("from Useracc where login=:login and status!="+STATUS_INACTIV).setParameter("login", login).getSingleResult();
			Query query=entityManager.createNativeQuery("select crypt('"+password+"', '"+instance.getPassword()+"')");
			String retPassword=(String)query.getSingleResult();
			if(!retPassword.equals(instance.getPassword())){
				throw new NoResultException("password check failed");
			}
			Hibernate.initialize(instance.getUseraccPrefs());				
			return instance;
		} catch (NoResultException re) {
			log.debug("get failed", re);
		}
		return null;
	}
	public Useracc getUserAccForLogin(String loginName){
		log.debug("getting Useracc instance with login: " + loginName);
		try {
			Useracc instance =(Useracc) entityManager.createQuery("from Useracc where login=:login and status!="+STATUS_INACTIV).setParameter("login", loginName).getSingleResult();
			return instance;
		} catch (NoResultException re) {
			log.warn("get failed"+re.getMessage());
		}
		return null;
	}
	public Useracc findByPrivateCode(String privateCode){
		log.debug("getting Useracc instance with privateCode: " + privateCode);
		try {
			Useracc instance =(Useracc) entityManager.createQuery("from Useracc where privateCode=:privateCode").setParameter("privateCode", privateCode).getSingleResult();
			log.debug("get successful");
			return instance;
		} catch (NoResultException re) {
			log.error("get failed", re);
		}
		return null;
	}
	
	public void encodePassword(final Useracc transientInstance){
		((Session)this.entityManager.getDelegate()).doWork(
			    new Work() {
			        public void execute(Connection con) throws SQLException 
			        { 
			    		Statement st=null;
			    		try {
			    			st = con.createStatement();
			    			ResultSet rs=st.executeQuery("select  crypt('"+transientInstance.getPassword()+"', gen_salt('md5'))");
			    			if(rs.next()){
			    				transientInstance.setPassword(rs.getString(1));
			    			}else{
			    				log.error("NO RESULT for encode user password");
			    			}
			    		} catch (SQLException e) {
			    			log.error("persist USER error",e);
			    		}finally{
			    			try {
			    				if(st!=null)st.close();
			    			} catch (SQLException e) {}
			    		}
			        }
			    }
			);
	}
	public List<Useracc> matchBeginingName(String name,int maximum){
		log.debug("getting matchBeginingName name: " + name);
		try {
			List<Useracc> instanceList =(List<Useracc>) entityManager.createQuery("from Useracc where login~=:input or email~=:input").setParameter("input", name).setMaxResults(maximum).getResultList();
			log.debug("get successful size["+instanceList.size()+"]");
			return instanceList;
		} catch (NoResultException re) {
			log.warn("get failed", re);
		}
		return null;		
	}
	
	@Override
	public void persist(Useracc transientInstance) {
		
		
		super.persist(transientInstance);
	}
}
