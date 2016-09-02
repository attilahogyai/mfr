package org.mfr.util;

import java.lang.reflect.Method;

import javax.persistence.Column;
import javax.persistence.JoinColumn;

import org.mfr.data.Category;

public class ObjectHelper {
	public static String getJpaFieldList(String prefix,Class obj){
		Method [] fields=obj.getDeclaredMethods();
		StringBuffer sb=new StringBuffer();
		for (Method field : fields) {
			Column col=field.getAnnotation(Column.class);
			if(col!=null){
				sb.append(prefix).append(col.name()).append(",");
			}else{
				JoinColumn joinCol=field.getAnnotation(JoinColumn.class);
				if(joinCol!=null){
					sb.append(prefix).append(joinCol.name()).append(",");	
				}
			}
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	public static Throwable getRootException(Throwable e){
		if(e!=null && e.getCause()!=null){
			return getRootException(e.getCause());
		}else{
			return e;
		}
	}
	public static final void main (String [] args){
		System.out.println(getJpaFieldList("act.", Category.class));
	}
	
}
