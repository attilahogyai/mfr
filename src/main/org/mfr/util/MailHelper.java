package org.mfr.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.mfr.data.Category;
import org.mfr.data.User;
import org.mfr.data.Useracc;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;

import bsh.org.objectweb.asm.Label;

public class MailHelper {
	private static final String DISABLE_EMAILS = "disable.emails";
	public static final String INFO_MAIL_ADDRESS="info@myfotoroom.com";
	public static final String INFO_MAIL_NAME="MyFotoRoom";
	
	public static final String COMMENT_NOTIFICATION="email/new_comment.vm";
	public static final String REGISTRATION_EMAIL="email/regmail.vm";
	
	
	
	private static final Logger log = Logger.getLogger(MailHelper.class);

	public static final String CONTENTTYPE ="text/html; charset=UTF-8";

	public static void sendMail(List<String> recipientList, String templateName, String sender, String subject,
			boolean singlePart, String contentType, Map mailArguments) throws Exception{
		sendMail(recipientList, templateName, new String[]{sender,Labels.getLabel(sender+".name")}, subject, singlePart, contentType, mailArguments,null );
	}

	public static void sendMail(List<String> recipientList, String templateName, String sender[], String subject,
			boolean singlePart, String contentType, Map mailArguments) throws Exception{
		sendMail(recipientList, templateName, sender, subject, singlePart, contentType, mailArguments,null );
	}

	public static void sendMail(List<String> recipientList, String templateName, String sender[], String subject,
			boolean singlePart, String contentType, Map mailArguments,List<File> attachmentFiles ) throws Exception{
		Boolean disableEmails=Boolean.parseBoolean(System.getProperty(DISABLE_EMAILS, "false"));
		if(!disableEmails){
			if(!recipientList.isEmpty()){
				for (Iterator<String> iterator = recipientList.iterator(); iterator.hasNext();) {
					String recipient = iterator.next();
					log.info("Recipient address: "+recipient);
				}
				String mailMessageText = null;
				log.debug("Arguments: "+mailArguments);
				
				User accUser=null;
				String language="HU";
				if(Executions.getCurrent()!=null){
					VelocityHelper.init(((HttpServletRequest)Executions.getCurrent().getNativeRequest()).getServletContext());
					if(mailArguments.get("user")==null){
						accUser=HttpHelper.getUser();
						mailArguments.put("user", accUser);
					}
					language=ZkUtil.getCurrentLanguage(((HttpServletRequest)Executions.getCurrent().getNativeRequest()).getSession());
				}
				mailArguments.put("language", language);
				
				mailMessageText = VelocityHelper.mergeFile(templateName, mailArguments);
				log.debug("Mail body: "+mailMessageText);
				String smtpHost=System.getProperty("smtp.host");
				String user=System.getProperty("email.user");
				String password=System.getProperty("email.password");
				log.debug("smtp host["+smtpHost+"] user["+user+"] password["+(password==null?"null":"is set")+"]");
				AuthProps authProps=new AuthProps(user!=null, user, password);
				if(smtpHost==null){
					throw new RuntimeException("smtpHost not set");
				}
				
				//Send mail
				try {
					MailHelper.sendMail(smtpHost, sender, recipientList, subject, mailMessageText, singlePart, contentType, authProps, attachmentFiles );
				} catch (Exception e) {
					log.error("SEND MAIL exception", e);
					if(e.getMessage()!=null && e.getMessage().indexOf("Invalid Addresses")>-1){
						throw new ExceptionWrapper(e, "error.sending.email.invalidaddress",recipientList.toArray(new String[recipientList.size()]));
					}else{
						throw new ExceptionWrapper(e, "error.sending.email",recipientList.toArray(new String[recipientList.size()]));
					}
				}
			}
		}else{
			log.warn("Email sending is disabled");
		}
	}	
	public static void sendMail(String smtpHost, String sender[], List<String> recipients, String subject, String message, boolean singlePart, String contentType,  final AuthProps authProps, List<File> attachmentFiles) throws AddressException, MessagingException, UnsupportedEncodingException{

		log.debug("Begin send mail, singlePart:" + singlePart + ", contentType:" + contentType);
		if (contentType == null || contentType.trim().length() == 0) {
			contentType = "text/html";
			log.debug("contentType is blank use insted:" + contentType);
		}

		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", smtpHost);
		props.setProperty("mail.smtp.class", "com.sun.mail.smtp.SMTPTransport");
		props.setProperty("mail.smtp.port", "25");
		props.setProperty("mail.smtp.socketFactory.port", "25");
		props.setProperty("mail.gm.class", "com.sun.mail.smtp.SMTPTransport");
		props.setProperty("mail.gm.port", "25");
		
		
		Session session = null;
		props.put("mail.debug", "true");

		if (authProps.isAuthenticaton_flag()) {

		    props.put("mail.smtp.auth", "true");
			session = Session.getDefaultInstance(props, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(authProps.getUsername(), authProps.getPassword());
					}});

		} else {
			session = Session.getDefaultInstance(props, null);
		}
		MimeMessage msg = new MimeMessage(session);
		if(sender.length>1){
			msg.setFrom(new InternetAddress(sender[0],sender[1],"UTF-8"));
		}else{
			msg.setFrom(new InternetAddress(sender[0]));
		}
		List<Address> addresses = new ArrayList<Address>();
		for (Iterator iterator = recipients.iterator(); iterator.hasNext();) {
			addresses.add(InternetAddress.parse((String) iterator.next())[0]);
		}
		msg.setRecipients(Message.RecipientType.TO,addresses.toArray(new Address[0]));
		log.debug("recipient: " + addresses.toArray(new Address[0])[0]);
        msg.setSubject(subject);
        msg.setSentDate(new Date());

        if(! singlePart) {
	
        	// msg.setContent(message,"text/html; charset=ISO-8859-2");
	        MimeMultipart multipart = new MimeMultipart("related");
	
	        // first part  (the html)
	        BodyPart messageBodyPart = new MimeBodyPart();
	        messageBodyPart.setContent(message, contentType);
	        // add it
	        multipart.addBodyPart(messageBodyPart);
	        String mailPicDir=System.getProperty("mailsender.pic.dir");
	
			File files = mailPicDir == null ? null : new File(mailPicDir);
			if (files != null) {
				if (files.isDirectory()) {
					for (File file : files.listFiles()) {
						String fileName = file.getName();
						if (message.indexOf("cid:" + fileName) > 0) {
							DataSource fds = new FileDataSource(file);
							BodyPart messageBodyPartt = new MimeBodyPart();
							messageBodyPartt.setDataHandler(new DataHandler(fds));
							messageBodyPartt.setHeader("Content-ID", "<" + fileName
									+ ">");
							multipart.addBodyPart(messageBodyPartt);
						}
					}
				} else {
						log.error("the file set in property as mailpicdir is not a directory");
				}
			}
			if(attachmentFiles!=null && !attachmentFiles.isEmpty()){
				for(File file :attachmentFiles){ 

					DataSource fds = new FileDataSource(file);
					BodyPart messageBodyPartt = new MimeBodyPart();
					messageBodyPartt.setDataHandler(new DataHandler(fds));
					messageBodyPartt.setHeader("Content-ID", "<" + HttpHelper.replacetoAsciiChars(file.getName()) + ">");
					multipart.addBodyPart(messageBodyPartt);
					log.debug("content file name part, fileName:" + file.getName());
				}
			}
	        msg.setContent(multipart);

        } else {
        	msg.setText(message, "utf-8","html");
        }

		if (authProps.isAuthenticaton_flag()) {

		    Transport t = session.getTransport("smtp");
		    log.info(sender +"  "+ authProps.getUsername() +"   "+authProps.getPassword());
	        t.connect(smtpHost, authProps.getUsername(), authProps.getPassword());
	        t.sendMessage(msg, msg.getAllRecipients());
	        t.close();

		} else {
			Transport t = session.getTransport("smtp");
			t.send(msg);

		}

		log.debug("Finish send mail, singlePart:" + singlePart);
        log.info("Message sent OK.");

	}
	public static void sendErreReportMail(Exception e,String workflowid) {
		List address=new ArrayList();
		address.add("info@myfotoroom.com");
		Map args=new HashMap();
		
		args.put("exception", e);
		User user=HttpHelper.getUser();
		args.put("user", user);
		args.put("wfid", workflowid);
		try {
			sendMail(address, "email/exceptionreport.vm", "info@myfotoroom.com", "error report ["+workflowid+"]", true, "text/plain",args);
		} catch (Exception e1) {
			log.error("send error report error",e1);
		}
	}
	public static Map<String,Object> prepareArgumentsForAlbum(Map <String,Object>arguments,String sourceUrl,Category category){
		if(arguments==null){
			arguments=new HashMap();
		}
		String referer=HttpHelper.getHttpRequest().getHeader("Referer");
		if(referer!=null){
			arguments.put("albumlink", referer.replaceAll("#",""));
		}else{
			arguments.put("albumlink", HttpHelper.getAlbumFullLink(sourceUrl,category));	
		}
    	arguments.put("category", category);
    	
    	arguments.put("actuallink", HttpHelper.getHttpRequest().getRequestURI());
    	arguments.put("albumname", category.getName());
		return arguments;
	}

}
