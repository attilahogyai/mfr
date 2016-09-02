package org.mfr.mybatis.impl;

import org.mfr.mybatis.types.MCategory;

public interface CategoryDaoMapper {
	public MCategory findById(Long id);
	public void incrementAccessCount(MCategory category);
}
