package org.mfr.mybatis.test;

import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import static org.junit.Assert.*;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.mfr.util.Constants;

public class AbstractTest {
	static{
		System.setProperty("catalina.base", ".");
	}
	private static final Logger log = Logger.getLogger(AbstractTest.class);
	
	private static SqlSessionFactory oracleSessionFactory;
	private static SqlSessionFactory msSessionFactory;
	private static DataSource postDataSource;
	private static boolean postConnectionOk;

	protected List<SqlSession> sessions = new ArrayList<>();
	protected List mappers = new ArrayList();
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		Constants.UNITTESTMODE=true;
		System.setProperty("unittest", "true");
		try (Reader reader = Resources.getResourceAsReader("org/mfr/mybatis/impl/mybatis-config.xml")) {
			XMLConfigBuilder parser = new XMLConfigBuilder(reader, null, null);
			Configuration config = parser.parse();
			config.addMappers("org.mfr.mybatis.impl");

			postDataSource = getPostDataSource();
			oracleSessionFactory = createSessionFactory("oracle", config, postDataSource);
			postConnectionOk = isConnectionOK(postDataSource);
		}
	}

	private static SqlSessionFactory createSessionFactory(String name, Configuration config, DataSource dataSource) {
		TransactionFactory transactionFactory = new JdbcTransactionFactory();
		Environment environment = new Environment(name, transactionFactory, dataSource);
		config.setEnvironment(environment);
		return new SqlSessionFactoryBuilder().build(config);
	}

	private static DataSource getPostDataSource() throws SQLException {
		DataSource dataSource = new PooledDataSource("org.postgresql.Driver", "jdbc:postgresql://myfotoroom.com:5432/photospot", "hati", "H2t1k2");
		dataSource.setLoginTimeout(1);
		return dataSource;
	}
	
	@SuppressWarnings("resource")
	public void before(Class<?> cl) {
		if (postConnectionOk) {
			SqlSession session = oracleSessionFactory.openSession(false);
			sessions.add(session);
			log.info("db connection OK");
		} else {
			log.info("db connection FAIL");
		}
		//If none of the database connections are avaiable, we skip the test
		Assume.assumeFalse(sessions.isEmpty());
	}

	protected <cl> cl getMapper(Class<?> cl) {
		for (SqlSession session : sessions) {
			Object mapper = session.getMapper(cl);
			assertNotNull(mapper);
			return (cl)mapper;
		}
		return null;
	}

	@After
	public void after() {
		for (SqlSession session : sessions) {
			session.rollback();
			//session.commit();
			session.close();
		}
	}
	
	private static boolean isConnectionOK(DataSource datasource) {
		try (Connection connection = datasource.getConnection();
			 Statement statement = connection.createStatement()) {
			statement.execute("set search_path=mfr,public");
			return true;
		} catch(Exception e) {
			log.info("Unable to connecto to database - skipping MyBatis mapper tests", e);
		}
		return false;
	}

}
