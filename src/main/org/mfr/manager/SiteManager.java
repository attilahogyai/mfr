package org.mfr.manager;

import org.mfr.data.SiteDao;
import org.mfr.data.SiteGalleriesDao;
import org.springframework.beans.factory.annotation.Autowired;

public class SiteManager {
	@Autowired
	private SiteDao siteDao;
	@Autowired
	private SiteGalleriesDao siteGalleriesDao;
	
	
}
