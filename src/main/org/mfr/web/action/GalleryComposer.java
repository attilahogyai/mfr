package org.mfr.web.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.mfr.data.Category;
import org.mfr.data.CategoryCss;
import org.mfr.data.CategoryCssDao;
import org.mfr.data.CategoryDao;
import org.mfr.data.Comment;
import org.mfr.data.CommentDao;
import org.mfr.data.Css;
import org.mfr.data.CssDao;
import org.mfr.data.ImageDataModel;
import org.mfr.data.Site;
import org.mfr.manager.CategoryManager;
import org.mfr.manager.ImageDataManager;
import org.mfr.manager.PermissionDetail;
import org.mfr.manager.PermissionManager;
import org.mfr.util.HttpHelper;
import org.mfr.web.DynaServlet;
import org.mfr.web.action.css.CssSelectorComposer;
import org.zkoss.spring.SpringUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.xel.VariableResolver;
import org.zkoss.xel.XelException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zk.ui.util.InitiatorExt;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

public class GalleryComposer extends BasePageInitiator implements Initiator, InitiatorExt {
	public static final String PERMISSION_DETAIL = "permissionDetail";
	private static final long serialVersionUID = 6791676746399940600L;
	@WireVariable
	private PermissionManager permissionManager;
	@WireVariable
	private CategoryManager categoryManager;
	@WireVariable
	private ImageDataManager imageDataManager;
	@WireVariable
	private CommentDao commentDao;
	
	ImageDataModel [] imageData=null;
	PermissionDetail permissionDetail=null;
	private List<Comment> comments=null;
	
	private boolean allowComments;
	
	boolean useSmallImage=false;
	boolean allowDownload=false;
	boolean display=false;
	boolean emptyData=false;
	String description="";
	Integer albumId=null;
	Category category=null;
	@Wire
	private Include comment;
	@Wire
	private Include commentHistory;
	
	@Override
	public void doBeforeComposeChildren(Component comp) throws Exception {
		Double bandWidthAvg=(Double)HttpHelper.getHttpSession().getAttribute("bandWidthAvg");
		category=(Category)comp.getPage().getAttribute("category");
		permissionDetail=(PermissionDetail)comp.getPage().getAttribute(PERMISSION_DETAIL);
		if(bandWidthAvg!=null){
			useSmallImage=bandWidthAvg<10000;
		}
		org.mfr.util.UAgentInfo info=org.mfr.util.UAgentInfo.getUAgentInfo();
		useSmallImage=info.detectMobileQuick();
		String id=((HttpSession)Executions.getCurrent().getSession().getNativeSession()).getId();
		if(category!=null){
			allowDownload=category.getAllowDownload()!=null && category.getAllowDownload()==1;
			albumId=category.getId();
			if(category.getBlog()==1){
				String path=comp.getPage().getRequestPath();
				if(GlobalVariableResolver.getSite()!=null){
					if(!path.equals("/pblog.zul")){
						HttpHelper.getHttpRequest().getServletContext().getRequestDispatcher("/pblog.zul").forward(HttpHelper.getHttpRequest(),HttpHelper.getHttpResponse());
					}
				}else{
					if(!path.equals("/blog.zul")){
						HttpHelper.getHttpRequest().getServletContext().getRequestDispatcher("/blog.zul").forward(HttpHelper.getHttpRequest(),HttpHelper.getHttpResponse());
					}
				}
			}
			if(!permissionManager.isMyPrivate(category)){
				categoryManager.incrementAccessCount(category);
			}
			comp.getPage().setTitle(category.getName());
			description=category.getName()+(category.getDescription()!=null?" - "+category.getDescription():"");
			imageData=imageDataManager.listAlbumContentbyId(category,true,false);
			refreshComments();
			
			try{
				if(imageData!=null){
					if(permissionDetail!=null){
						//similarList=permissionManager.getSimilarPermissions(permissionDetail);
					}
					display=true;
					emptyData=imageData.length==0;
				}
			}catch(Exception e){
				Messagebox.show(Labels.getLabel("image.process.error")+"["+e.getMessage()+"]");
			}
		}

		super.doBeforeComposeChildren(comp);		
	}
	
	protected void refreshComments() {
		showComment();
		if(allowComments){
			Integer userId=HttpHelper.getUser()!=null?HttpHelper.getUser().getUseracc().getId():null;
			comments=commentDao.findByAlbum(category.getId(),userId);
		}
		if(comment!=null){
			String src=comment.getSrc();
			comment.setDynamicProperty("comments", comments);
			comment.setSrc(null);
			comment.setSrc(src);
		}
	}
	protected void refreshCommentsHistory(List<Comment> commentsHistory) {
		// commentHistory
		if(commentHistory!=null){
			String src=commentHistory.getSrc();
			commentHistory.setDynamicProperty("comments", commentsHistory);
			commentHistory.setSrc(null);
			commentHistory.setSrc(src);
		}
		
	}
	
	
	protected boolean showComment() {
		allowComments=category.getShowComment()!=null && category.getShowComment()==1;
		return allowComments;
	}
	@Listen("onDownload = #maincontainer")
	public void onDownload(){
		
		Map arg=new HashMap();
		arg.put("albumid", category.getId());
	 	Component comp=Executions.createComponents("/comp/downloadpopup.zul", null, arg);
	 	((Window)comp).doModal();
	}
	@Listen("onNewComment = #comment")
	public void onNewComment(Event event){
		
		final Integer parent = (Integer)event.getData();
		
		if(HttpHelper.isNormalUser()){
			Map arg=new HashMap();
			arg.put("albumid", category.getId());
			arg.put("parent", parent);
			Executions.getCurrent().setAttribute("category",category);
			Executions.getCurrent().setAttribute("parent",parent);
			
		 	Component comp=Executions.createComponents("/comp/new_comment_popup.zul", null, arg);
		 	((Window)comp).doModal();
		 	comp.addEventListener("onClose", new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					refreshComments();
					List<Comment> parentList=commentDao.findByParent(parent);
					refreshCommentsHistory(parentList);
				}
			});
		}else{
			showLogin();
		}
	}
	@Listen("onShowHistory = #comment")
	public void onShowHistory(Event event){
		final Integer parent=(Integer)event.getData();
		Map arg=new HashMap();
		arg.put("albumid", category.getId());
		List<Comment> parentList=commentDao.findByParent(parent);
		arg.put("comments", parentList);
		arg.put("parent", parent);
	 	Component comp=Executions.createComponents("/comp/comment_popup.zul", null, arg);
	 	((Window)comp).doModal();
	 	commentHistory=(Include)comp.getFellowIfAny("commentHistory");
	}

	
	
	@Listen("onSwitchCommentState = #comment")
	public void onSwitchCommentState(Event e){
		Integer commentId=(Integer)e.getData();
		Comment c=commentDao.findById(commentId);
		if(HttpHelper.isLoggedUser(category.getUseracc())){
			if(c.getStatus()==null || c.getStatus().equals(1)){
				c.setStatus(0);
			}else{
				c.setStatus(1);
			}
			commentDao.merge(c);
			refreshComments();
		}
	}
	
	
	public boolean isUseSmallImage() {
		return useSmallImage;
	}

	public void setUseSmallImage(boolean useSmallImage) {
		this.useSmallImage = useSmallImage;
	}

	public boolean isAllowDownload() {
		return allowDownload;
	}

	public void setAllowDownload(boolean allowDownload) {
		this.allowDownload = allowDownload;
	}

	public boolean isDisplay() {
		return display;
	}

	public void setDisplay(boolean display) {
		this.display = display;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public ImageDataModel[] getImageData() {
		return imageData;
	}

	public void setImageData(ImageDataModel[] imageData) {
		this.imageData = imageData;
	}

	public Integer getAlbumId() {
		return albumId;
	}

	public void setAlbumId(Integer albumId) {
		this.albumId = albumId;
	}

	public PermissionDetail getPermissionDetail() {
		return permissionDetail;
	}

	public void setPermissionDetail(PermissionDetail permissionDetail) {
		this.permissionDetail = permissionDetail;
	}
	public boolean isUploadAllowed(){
		if(permissionDetail!=null){
			return permissionDetail.getPermission().getAllowUpload()!=null && permissionDetail.getPermission().getAllowUpload().equals(1);
		}
		return false;
	}

	public boolean isEmptyData() {
		return emptyData;
	}

	public void setEmptyData(boolean emptyData) {
		this.emptyData = emptyData;
	}

	@Override
	public void doAfterCompose(Page page, Component[] comps) throws Exception {
		CategoryCssDao categoryCssDao=(CategoryCssDao)SpringUtil.getBean("categoryCssDao");
		CssDao cssDao=(CssDao)SpringUtil.getBean("cssDao");
		category=(Category)page.getAttribute("category");
		if(category!=null){
			List<Css> categoryCssList=categoryCssDao.findCssByAlbumAndApply(category.getId(),page.getId());
			if(showComment()){
				Css c=new Css();
				c.setUrl("/dc/css/comment/struct/normal.css");
				categoryCssList.add(c);
			}
			Map<Integer,String> urlList=getCssUrls(cssDao,CssSelectorComposer.CATEGORY_TARGET,page.getId(),categoryCssList);
			HttpHelper.getHttpSession().setAttribute(DynaServlet.CSSLIST, urlList);
		}
		
	}

	@Override
	public void doInit(Page page, Map<String, Object> arg1) throws Exception {
		CategoryDao categoryDao=(CategoryDao)SpringUtil.getBean("categoryDao");
		PermissionManager permissionManager=(PermissionManager)SpringUtil.getBean("permissionManager");
		Site site=GlobalVariableResolver.getSite();
		
		Integer albumId=null;
		
		String ticket=(String)Executions.getCurrent().getParameter("ticket");
		try{
			albumId=Integer.parseInt((String)Executions.getCurrent().getParameter("albumid"));
		}catch(Exception e){}
		
		Category category=null;
		PermissionDetail permissionDetail = null;
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
		if(category!=null){
			page.setAttribute("category", category);
			page.setAttribute("pageDesc", category.getDescription());
			page.setAttribute(PERMISSION_DETAIL, permissionDetail);
			HttpHelper.getHttpSession().setAttribute(PERMISSION_DETAIL, permissionDetail);
		}
		
	}
	
	
	
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public boolean isAllowComments() {
		return allowComments;
	}
	public void setAllowComments(boolean allowComments) {
		this.allowComments = allowComments;
	}


}
