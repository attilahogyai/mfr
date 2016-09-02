package org.mfr.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.mfr.data.Category;
import org.mfr.data.Comment;
import org.mfr.data.CommentDao;
import org.mfr.data.User;
import org.mfr.data.Useracc;
import org.mfr.data.UseraccPrefs;
import org.mfr.data.UseraccPrefsDao;
import org.mfr.util.HttpHelper;
import org.mfr.util.MailHelper;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class CommentComposer extends SelectorComposer<Component> {
	private static final Log log = LogFactory.getLog(CommentComposer.class);
	@Wire
	private Textbox commentText;
	@WireVariable
	private CommentDao commentDao;
	@WireVariable
	private UseraccPrefsDao useraccPrefsDao;
	
	
	private Category category;
	
	private Window win;
	
	private Integer parent;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		win=(Window)comp;
		category=(Category)Executions.getCurrent().getAttribute("category");
		parent=(Integer)Executions.getCurrent().getAttribute("parent");
	}

	
	@Listen("onClick=#sendComment")
	public void addComment(){
        try{
        	if(category!=null){
        		Comment oriComment=null;
        		if(parent!=null){
        			try{
        				oriComment=commentDao.findById(parent);
        			}catch(Exception e){
        				log.debug("find parent failed:"+e.getMessage());
        			}
        		}
        		Useracc user=HttpHelper.getUser().getUseracc();
	        	Comment comment=new Comment();
	        	String t=HttpHelper.textBeautifier(commentText.getText());
	        	comment.setComment(t);
	        	comment.setCategory(category);
	        	comment.setUseracc(user);
	        	comment.setOriginal(oriComment);
	        	commentDao.persist(comment);
	        	Events.postEvent("onClose",win,null);
	        	
	        	
	        	String requestPath=null;
	        	if(Executions.getCurrent()!=null && Executions.getCurrent().getDesktop()!=null){
	        		requestPath=Executions.getCurrent().getDesktop().getRequestPath();
	        	}
	        	
	        	if(oriComment!=null && !oriComment.getUseracc().getId().equals(category.getUseracc().getId())){
	        		// send message to original sender
		        	Map<String,Object> arguments=MailHelper.prepareArgumentsForAlbum(null, requestPath,category);
		        	arguments.put("comment", commentText.getText());
		        	arguments.put("recipientName", oriComment.getUseracc().getName());
		        	arguments.put("senderName", user.getName());
		        	arguments.put("reply", true);
		        	
		        	final List<String> emailaddressList=new ArrayList<String>();
					emailaddressList.add(oriComment.getUseracc().getEmail());
					try{
						MailHelper.sendMail(emailaddressList,MailHelper.COMMENT_NOTIFICATION,new String[]{MailHelper.INFO_MAIL_ADDRESS,MailHelper.INFO_MAIL_NAME},Labels.getLabel("new.comment.email.subject.reply",new Object[]{category.getName(),user.getName()}),false,MailHelper.CONTENTTYPE,arguments,new ArrayList());
					}catch(Exception e){
						log.error("error sending mail to original commenter",e);
					}
	        		
	        	}
	        	UseraccPrefs prefs=useraccPrefsDao.findById(category.getUseracc().getId());
	        	if(!category.getUseracc().isDisableEmailOwner(prefs) && !comment.getUseracc().getId().equals(category.getUseracc().getId())){
	        		
		        	Map<String,Object> arguments=MailHelper.prepareArgumentsForAlbum(null, requestPath,category);
		        	arguments.put("comment", commentText.getText());
		        	arguments.put("recipientName", category.getUseracc().getName());
		        	arguments.put("senderName", user.getName());
		        	
		        	final List<String> emailaddressList=new ArrayList<String>();
					emailaddressList.add(category.getUseracc().getEmail());
					try{
						MailHelper.sendMail(emailaddressList,MailHelper.COMMENT_NOTIFICATION,new String[]{MailHelper.INFO_MAIL_ADDRESS,MailHelper.INFO_MAIL_NAME},Labels.getLabel("new.comment.email.subject",new Object[]{category.getName(),user.getName()}),false,MailHelper.CONTENTTYPE,arguments,new ArrayList());
		        	}catch(Exception e){
						log.error("error sending mail to category owner",e);
					}					
	        	}
        	}else{
        		throw new Exception("category is null error adding comment");
        	}
        }catch(Exception e){
     	   org.mfr.util.ZkUtil.showProcessError(e);
        }
	}
}
