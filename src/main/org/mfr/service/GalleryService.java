package org.mfr.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mfr.data.Category;
import org.mfr.data.CategoryDao;
import org.mfr.data.ImageDataModel;
import org.mfr.data.Site;
import org.mfr.manager.CategoryManager;
import org.mfr.manager.ImageDataManager;
import org.mfr.manager.MPermissionDetail;
import org.mfr.manager.PermissionDetail;
import org.mfr.manager.PermissionManager;
import org.mfr.mybatis.impl.CategoryDaoMapper;
import org.mfr.mybatis.impl.PermissionDaoMapper;
import org.mfr.mybatis.types.MCategory;
import org.mfr.mybatis.types.MImageDataModel;
import org.mfr.util.HttpHelper;
import org.mfr.web.action.GalleryComposer;
import org.mfr.web.action.GlobalVariableResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class GalleryService {
	private static final Logger logger = LoggerFactory
			.getLogger(GalleryService.class);
	
	@Autowired
	CategoryDaoMapper categoryDao;
	@Autowired
	PermissionManager permissionManager;
	@Autowired
	private CategoryManager categoryManager;
	@Autowired
	private ImageDataManager imageDataManager;
	
	@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, reason = "internal workflow exception")
	@ExceptionHandler({ Throwable.class })
	public String runtimeError(HttpServletRequest req, Exception exception) {
		logger.error("InternalException", exception);
		return exception.getMessage();
	}	
	@RequestMapping(method=RequestMethod.GET, value="/album.json")
	public @ResponseBody Map<String,Object> findByOwner(HttpServletRequest request, @RequestParam(required=false) Long albumId,
			@RequestParam(required=false) String ticket) {
		Site site=GlobalVariableResolver.getSite();
		MCategory category=null;
		MPermissionDetail permissionDetail = null;
		if(albumId!=null){
			category=categoryDao.findById(albumId);
			if(category!=null && category.getIspublic()!=2 && !permissionManager.isMyPrivate(category) && !HttpHelper.isSiteGranted(site)){
				category=null;
			}
		}else if(ticket!=null){
			permissionDetail=permissionManager.isAllowed(category,ticket);
			if(permissionDetail!=null){
				category=permissionDetail.getCategory();
			}
		}
		Map<String,Object> resultMap=new HashMap<String,Object>(); 
		if(category!=null){
			resultMap.put("category", category);
			resultMap.put("pageDesc", category.getDescription());
			resultMap.put("pageTitle", category.getName());
			
			resultMap.put(GalleryComposer.PERMISSION_DETAIL, permissionDetail);
			HttpHelper.getHttpSession().setAttribute(GalleryComposer.PERMISSION_DETAIL, permissionDetail);
		}else{
			throw new RuntimeException("album not found");
		}
		
		Double bandWidthAvg=(Double)HttpHelper.getHttpSession().getAttribute("bandWidthAvg");
		boolean useSmallImage = false;
		boolean allowDownload = false;
		if(bandWidthAvg!=null){
			useSmallImage=bandWidthAvg<10000;
		}
		org.mfr.util.UAgentInfo info=org.mfr.util.UAgentInfo.getUAgentInfo(request);
		useSmallImage=info.detectMobileQuick();
		String id=request.getSession().getId();
		if(category!=null){
			allowDownload=category.getAllowDownload()!=null && category.getAllowDownload()==1;
			albumId=category.getId();
			if(category.getBlog()==1){
				logger.debug("THIS is a BLOG");
			}
			if(!permissionManager.isMyPrivate(category)){
				categoryManager.incrementAccessCount(category);
			}
			MImageDataModel[] imageData;
			try {
				imageData = imageDataManager.listAlbumContentbyId(category,true,false,false);
				for (int i = 0; i < imageData.length; i++) {
					imageData[i].setPhoto(null);
				}
				//resultMap.put("imageData", imageData);
			} catch (Exception e) {
				logger.error("error listing",e);
			}
		}
		return resultMap;
	}
}
