package org.mfr.data;

public class Constants {
	public static String IM4JAVA_USEGM=null;
	public static String SEARCHPATH=null;
	public static String TEMPDIR=null;
	public static String STOREDIR=null;
	public static final String MEDIUMPATH="medium/";
	public static final String PREVIEWPATH="preview/";
	public static final String THUMBPATH="thumb/";
	public static final String ORIGINALPATH="original/";
	public static final String PNGPATH="png/";
	public static final String WORKPNGPATH="workpng/";
	public static final String TMPPATH="tmp/";
	
	public static Long STORAGELIMIT=0L;
	
	static{
		System.setProperty("im4java.useGM", "true");
		loadConstants();
	}	
	public static final void loadConstants(){
		IM4JAVA_USEGM=System.getProperty("im4java.useGM", "true");
		SEARCHPATH=System.getProperty("searchPath", "d:/prog/GraphicsMagick-1.3.16-Q8/;c:/Program Files (x86)/GraphicsMagick-1.3.12-Q8/;d:/prog/exiftool-8.61/;d:/prog/");
		TEMPDIR=System.getProperty("tempdir","i:/tmp/");
		STOREDIR=System.getProperty("store","i:/store/");
		STORAGELIMIT=Long.parseLong(System.getProperty("storageLimit","16106127360"));
	}
		
		
}
