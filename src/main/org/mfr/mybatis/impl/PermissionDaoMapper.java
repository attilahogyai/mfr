package org.mfr.mybatis.impl;

import org.mfr.mybatis.types.MCategory;
import org.mfr.mybatis.types.MPermission;
import org.mfr.mybatis.types.MUseracc;

public interface PermissionDaoMapper {
	public MPermission getPermission(MUseracc useracc,MCategory category);
	public MPermission getPermission(String ticket);
	public boolean isMyPrivate(MCategory category);
	public void updateAccessCount(MPermission permission);
}
