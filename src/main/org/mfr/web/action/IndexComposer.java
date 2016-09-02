package org.mfr.web.action;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.mfr.data.CategoryDao;
import org.mfr.data.PhotoDao;
import org.mfr.data.SiteDao;
import org.mfr.data.UseraccDao;
import org.mfr.manager.CategoryManager;
import org.mfr.manager.PermissionManager;
import org.zkoss.spring.SpringUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Initiator;

public class IndexComposer extends SelectorComposer<Component> implements Initiator{
	@WireVariable
	private CategoryManager categoryManager;
	@WireVariable
	private SiteDao siteDao;
	@WireVariable
	private PhotoDao photoDao;
	@WireVariable
	private UseraccDao useraccDao;
	@WireVariable
	private CategoryDao categoryDao;
	
	
	private static final long serialVersionUID = -1592603786757647736L;




	@Override
	public void doInit(Page page, Map<String, Object> args) throws Exception {
		String id = ((HttpSession) Executions.getCurrent().getSession()
				.getNativeSession()).getId();
		categoryManager=(CategoryManager)SpringUtil.getBean("categoryManager");
		siteDao=(SiteDao)SpringUtil.getBean("siteDao");
		useraccDao=(UseraccDao)SpringUtil.getBean("useraccDao");
		photoDao=(PhotoDao)SpringUtil.getBean("photoDao");
		categoryDao=(CategoryDao)SpringUtil.getBean("categoryDao");
		
		List categories = categoryManager.findPublicTopAlbums(12);

		org.mfr.data.User user = org.mfr.util.HttpHelper.getUser();

		List lastSites = siteDao.getLastSites(8);
		List topSites = siteDao.getTopSites(10);

		Integer categoryCount = 0;
		Long photoCount = 0L;
		String totalSpace = "";

		String usedSpace = "";
		List privateCategories = null;
		boolean portfolio = false;
		String portfolioLink = "";
		org.mfr.data.Site site = null;

		String mainImageUrl = org.mfr.web.action.GlobalVariableResolver.mainImageUrl;

		if (user != null) {
			long space = user.getUsedSpace();
			if (space == 0) {
				space = photoDao.getUsedSpace(user.getUserId());
				user.getUseracc().setUsedStorage(space);
				useraccDao.merge(user.getUseracc());
				user.setUsedSpace(space);
			}

			usedSpace = org.mfr.util.HttpHelper.getDisplayFormat(space);
			totalSpace = org.mfr.util.HttpHelper.getDisplayFormat(user
					.getUseracc().getstorageLimit());
			usedSpace = Labels.getLabel("used.space",
					new String[] { usedSpace });
			totalSpace = Labels.getLabel("total.space",
					new String[] { totalSpace });
			photoCount = photoDao.getPhotoCount(user.getUserId());

			privateCategories = categoryDao.findByUseracc(user.getUseracc());
			org.mfr.data.Photo p = photoDao.getMainPhoto(user.getUserId());
			if (p != null) {
				org.mfr.util.HttpHelper.setImageAccessForPublic(p.getId(),
						p.getName(), p.getPath());
				mainImageUrl = "/mimg/medium/id/" + p.getId();
			}

			categoryCount = privateCategories.size();
			if (categoryCount < 7) {
				privateCategories = privateCategories.subList(0, categoryCount);
			} else {
				privateCategories = privateCategories.subList(0, 7);
			}

			if (site != null) {
				portfolio = true;
				portfolioLink = "http://" + site.getUrl() + "."
						+ org.mfr.web.action.GlobalVariableResolver.getDomain();
			}

		}


		page.setAttribute("topCategories", categories);
		page.setAttribute("lastSites", lastSites);
		page.setAttribute("topSites", topSites);
		page.setAttribute("privateCategories", privateCategories);
		page.setAttribute("photoCount", photoCount);
		page.setAttribute("totalSpace", totalSpace);
		page.setAttribute("usedSpace", usedSpace);
		page.setAttribute("mainImageUrl", mainImageUrl);
		page.setAttribute("portfolio", portfolio);
		page.setAttribute("portfolioLink", portfolioLink);
		page.setAttribute("categoryCount", categoryCount);
		
	}


}
