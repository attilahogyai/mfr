package org.mfr.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.mfr.manager.UploadHandler;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.EventListener;

public class ZkUtil {
	private static final Log log = LogFactory.getLog(ZkUtil.class);
	
	//update or delete on table "category" violates foreign key constraint "fk_category" on table "site_galleries"
	static Pattern deleteExceptionPattern=Pattern.compile(".*update or delete on table \"(.*)\" violates foreign key constraint \"(.*)\" on table \"(.*)\"");    	
	static Pattern uniqueExceptionPattern=Pattern.compile(".*update or delete on table \"(.*)\" violates foreign key constraint \"(.*)\" on table \"(.*)\"");    	
	
	private static Random random=new Random();
    private static final String ALPHA_NUM =  
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";  
    private static String[] supportedLanguages=new String[]{"en","hu"};
    private static String workflowid=null;
    private static long errorCount=0;
    static{
    	Arrays.sort(supportedLanguages);
    	SimpleDateFormat sdf=new SimpleDateFormat("yyMMddhhmm");
    	workflowid=sdf.format(new Date())+"-";
    }
	public static final void logProcessError(Exception e) {
		String er=workflowid+errorCount++;
		log.error("PROCESSERROR - "+er,e);
		MailHelper.sendErreReportMail(e,er);
	}   
	public static final void showProcessErrorNoEmail(Exception e) {
		String er=workflowid+errorCount++;
		log.error("PROCESSERROR - "+er,e);
		Messagebox.show(Labels.getLabel("process.error",new Object[]{er}), Labels.getLabel("warning"), Messagebox.OK, Messagebox.EXCLAMATION);
	}
	public static final void showProcessError(Exception e) {
		if(e instanceof ExceptionWrapper){
			Messagebox.show(((ExceptionWrapper) e).getExceptionLocalizedMessage(), Labels.getLabel("warning"), Messagebox.OK, Messagebox.EXCLAMATION);
		}else{		
			showProcessError(e,"process.error");
		}
	}
	private static final void showProcessError(Exception e,String label) {
		String er=workflowid+errorCount++;
		log.error("PROCESSERROR - "+er,e);
		Messagebox.show(Labels.getLabel(label,new Object[]{er}), Labels.getLabel("warning"), Messagebox.OK, Messagebox.EXCLAMATION);
		MailHelper.sendErreReportMail(e,er);
	}
	public static void setLanguage(String setLang, HttpSession session) {
		if(log.isTraceEnabled()){
			log.trace("old Language: " + session.getAttribute("preflang"));
		}

		// set session wide language to new value
		if (!setLang.isEmpty()) {
			session.setAttribute("preflang", setLang);
			session.setAttribute("preflang", setLang);
		}

		// read the session language attribute
		String sessLang = (String) session.getAttribute("preflang");
		if(log.isTraceEnabled()){
			log.trace("new Language: " + session.getAttribute("preflang"));
		}

		// set the new preferred locale
		// otherwise it will use the default language (no session attribute
		// and/or language parameter
		if (!(sessLang == null)) {
			Locale preferredLocale = org.zkoss.util.Locales.getLocale(sessLang);
			session.setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE,
					preferredLocale);
			org.zkoss.util.Locales.setThreadLocal(org.zkoss.util.Locales
					.getLocale(sessLang));
		}
	}
	public static String getCurrentLanguage(HttpSession session){
		if(session!=null){
			String sessLang = (String) session.getAttribute("preflang");
			if(sessLang==null){
				sessLang=org.zkoss.util.Locales.getCurrent().getLanguage();
			}
			if(!(Arrays.binarySearch(supportedLanguages, sessLang)>=0)){
				sessLang="en";
			}
			return sessLang.toUpperCase();
		}else{
			log.warn("query langage not possible session is NULL");
			return null;
		}
		
		
	}
	public static void initUploaders(Session session,Desktop desktop){
		String id=((HttpSession)session.getNativeSession()).getId();
		UploadHandler.setDesktop(id, desktop,true);
	}
	public static void messageBoxInfo(String messageCode){
		Messagebox.show(Labels.getLabel(messageCode),Labels.getLabel("information"),Messagebox.OK,Messagebox.INFORMATION);
	}
	public static void messageBoxInfo(String messageCode,Object args[]){
		Messagebox.show(Labels.getLabel(messageCode,args),Labels.getLabel("information"),Messagebox.OK,Messagebox.INFORMATION);
	}

	public static void messageBoxInfo(String messageCode,String title){
		Messagebox.show(Labels.getLabel(messageCode),title,Messagebox.OK,Messagebox.INFORMATION);
	}
	public static void messageBoxError(String messageCode){
		Messagebox.show(Labels.getLabel(messageCode),Labels.getLabel("warning"),Messagebox.OK,Messagebox.EXCLAMATION);
	}
	public static void messageBoxErrorText(String messageText){
		Messagebox.show(messageText,Labels.getLabel("warning"),Messagebox.OK,Messagebox.INFORMATION);
	}
	
	public static void messageBoxWarning(String messageCode){
		messageBoxWarning(messageCode,new Object [] {});
	}
	public static void messageBoxWarning(String messageCode,String arg){
		messageBoxWarning(messageCode,new Object [] {arg});
	}
	public static void messageBoxWarning(String messageCode,Object [] args){
		Messagebox.show(Labels.getLabel(messageCode,args),Labels.getLabel("warning"),Messagebox.OK,"error");
	}
	public static void messageBoxConfirm(String messageCode,Object args[],EventListener eventlistener){
		Messagebox.show(Labels.getLabel(messageCode,args),Labels.getLabel("warning"),Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,eventlistener);
			
	}
	public static long getRandomLong(long max){
		double rn=random.nextDouble()*max;
		return Math.abs(new Double(rn).longValue());
	}
	public static int getRandomInt(long max){
		double rn=random.nextDouble()*max;
		return Math.abs(new Double(rn).intValue());
	}
    public static String getAlphaNumeric(int len) {  
       StringBuffer sb = new StringBuffer(len);  
       for (int i=0;  i<len;  i++) {  
          int ndx = (int)(getRandomLong(ALPHA_NUM.length()));  
          sb.append(ALPHA_NUM.charAt(ndx));  
       }  
       return sb.toString();  
    }
    public static Object getMapValue(Map map, Object key){
    	Object val=map.get(key.toString());
    	return val;
    }
    
    public static Component findComponentByType(Collection <Component> comList,Class<? extends Component> compType){
    	for (Component component : comList) {
    		
			if(component.getClass().isAssignableFrom(compType)){
				return component; 
			}
		}
    	return null;
    }
    public static void tryTranslateException(Exception e){
		if (e.getCause() instanceof ConstraintViolationException) {
			String messageBoxErrorText=ObjectHelper.getRootException(e).getMessage();
			messageBoxErrorText=translateError(messageBoxErrorText);
			if(messageBoxErrorText==null){
				messageBoxErrorText=Labels.getLabel("constraint.error");
			}
			ZkUtil.messageBoxErrorText(messageBoxErrorText);
		} else {
			org.mfr.util.ZkUtil.showProcessError(e);
		}
    }		
	private static String translateError(String message){
		
    	Matcher m=deleteExceptionPattern.matcher(message);
    	if(m.find()){
    		String main=m.group(1);
    		String key=m.group(2);
    		String table=m.group(3);
    		return Labels.getLabel(main+".used.by."+table);	
    	}
    	uniqueExceptionPattern=Pattern.compile(".*violates unique constraint \"(.*?)\"");
    	m=uniqueExceptionPattern.matcher(message);
    	if(m.find()){
    		String main=m.group(1);
    		return Labels.getLabel(main);
    	}
    	return message;
    }
}
