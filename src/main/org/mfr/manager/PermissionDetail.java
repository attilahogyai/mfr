package org.mfr.manager;

import org.mfr.data.Category;
import org.mfr.data.Permission;

public class PermissionDetail {
	private Category category;
	private Permission permission;
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public Permission getPermission() {
		return permission;
	}
	public void setPermission(Permission permission) {
		this.permission = permission;
	}
}
