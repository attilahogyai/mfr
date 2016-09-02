package org.mfr.manager;

import java.util.Map;

import org.mfr.data.User;

public interface IFilesService {
	public Map<String, Object> syncFiles(User user,boolean full) ;
	public void handleSyncConfirm();
}
