package org.mfr.util;

//import hu.dorsum.clavis.util.converter.DateConverter;
//import hu.dorsum.clavis.util.converter.LocalDateConverter;

import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class JsonUtility {
	private XStream xStream;
	private boolean debugMode=true;
		
	public JsonUtility(Locale locale){
		if(debugMode){
			xStream = new XStream(new JsonHierarchicalStreamDriver());
		}else{
			xStream = new XStream(new JettisonMappedXmlDriver());
		}
		xStream.setMode(XStream.NO_REFERENCES);
		initDefaultConverters(locale);
	}
	
	protected void initDefaultConverters(Locale locale){
//		addConverter(new DateConverter(locale));
//		addConverter(new LocalDateConverter(locale));
	}
	
	public void addAliasMap(Map<Class,String> alias){
		if(alias!=null){
			for(Class c : alias.keySet()){
				addAlias(alias.get(c), c);	
			}
		}
	}
	
	public void addAliasFieldMap(Map<Class,Map<String,String>> aliasField){
		if(aliasField!=null){
			for(Class c : aliasField.keySet()){
				if(aliasField.get(c)!=null){
					for(String key : aliasField.get(c).keySet()){
						addAliasField(key, c, aliasField.get(c).get(key));
					}
				}
			}
		}
	}
	
	public void addAlias(String name, Class type){
		xStream.alias(name, type);
	}
	
	public void addAliasField(String alias, Class definedIn, String fieldName){
		xStream.aliasField(alias, definedIn, fieldName);
	}
	
	public void addConverter(Converter c){
		xStream.registerConverter(c);
	}
	
	public void toJson(Object object, Writer writer){
		xStream.toXML(object, writer);
	}
	
	public String toJson(Object object){
		return xStream.toXML(object);
	}
	
	public void toJson(Object object, String name, Writer writer){
		if(object instanceof List){
			xStream.alias(name, List.class);
		}else if(object instanceof Map){
			xStream.alias(name, Map.class);
		}
		xStream.alias(name, object.getClass());
		
		xStream.toXML(object,writer);
	}
	
	public String toJson(Object object, String name){
		if(object instanceof List){
			xStream.alias(name, List.class);
		}else if(object instanceof Map){
			xStream.alias(name, Map.class);
		}
		xStream.alias(name, object.getClass());
		
		return xStream.toXML(object);
	}
	
	public Object fromJson(String json){
		return xStream.fromXML(json);
	}
}
