package org.mfr.manager;

import org.mfr.mybatis.types.MCategory;
import org.mfr.mybatis.types.MPermission;

public class MPermissionDetail {
	private MCategory category;
	private MPermission permission;
	public MCategory getCategory() {
		return category;
	}
	public void setCategory(MCategory category) {
		this.category = category;
	}
	public MPermission getPermission() {
		return permission;
	}
	public void setPermission(MPermission permission) {
		this.permission = permission;
	}
}
