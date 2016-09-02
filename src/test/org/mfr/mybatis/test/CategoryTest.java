package org.mfr.mybatis.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mfr.mybatis.impl.CategoryDaoMapper;
import org.mfr.mybatis.types.MCategory;

public class CategoryTest extends AbstractTest {
	@Before
	public void before() {

		super.before(CategoryDaoMapper.class);
	}

	@Test
	public void findById() {
		CategoryDaoMapper mapper = (CategoryDaoMapper) getMapper(CategoryDaoMapper.class);
		MCategory f = mapper.findById(2413L);
		Assert.assertTrue(f.getName().equals("Fákról"));
	}
	@Test
	public void incrementTest() {
		CategoryDaoMapper mapper = (CategoryDaoMapper) getMapper(CategoryDaoMapper.class);
		MCategory f = mapper.findById(2413L);
		mapper.incrementAccessCount(f);
	}
}
