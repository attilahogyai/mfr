package org.mfr.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.metainfo.Property;
import org.zkoss.zk.ui.sys.IdGenerator;

public class CustomIdGenerator implements IdGenerator {
	private static final String T = "t_";
	private static final String _0 = "0";
	private static final String _ = "_";
	private static final String TDTID = "tdtid";
	private static final String ID_NUM = "Id_Num";
	private static final String $ = "[-${}\\.]";
	private static final Log log = LogFactory.getLog(CustomIdGenerator.class);
	private static int ids=0;
	
	
    private static ThreadLocal<HttpServletResponse> response = new ThreadLocal<HttpServletResponse>();
    private static AtomicInteger ai = new AtomicInteger();
    private static final String IDPOSTFIX="ID";
    
    public String nextComponentUuid(Desktop desktop, Component comp, ComponentInfo compInfo) {
        String number;
        List<Property> properties=(List<Property>)comp.getAttribute("allProperties");
        if(properties!=null){
        	for (int i = 0; i < properties.size(); i++) {
        		Property p=properties.get(i);
        		if(p.getName().equals("id") && p.getRawValue()!=null){
        			Integer ids=(Integer)desktop.getAttribute(p.getRawValue()+IDPOSTFIX);
        			if(ids==null){
        				ids=0;
        			}
        			desktop.setAttribute(p.getRawValue()+IDPOSTFIX,++ids);
        			String id=p.getRawValue().replaceAll($, "")+_+ids;
        			return id;
        		}
        		
			}
        }
        
        if ((number = (String) desktop.getAttribute(ID_NUM)) == null) {
            number = _0;
            desktop.setAttribute(ID_NUM, number);
        }
        
        int i = Integer.parseInt(number);
        i++;// Start from 1
        desktop.setAttribute(ID_NUM, String.valueOf(i));
        return T + i;
    }
 
    public String nextDesktopId(Desktop desktop) {
        HttpServletRequest req = (HttpServletRequest)Executions.getCurrent().getNativeRequest();
        String dtid = req.getParameter(TDTID);
        if(dtid!=null){
        }
        return dtid==null?null:dtid;
    }
 
    public String nextPageUuid(Page page) {
        return null;
    }
}
