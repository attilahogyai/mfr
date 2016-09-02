package org.mfr.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.image.Image;
import org.zkoss.io.Files;
import org.zkoss.util.media.ContentTypes;

public class MyZkImage implements Image {
	private static final Logger logger=LoggerFactory.getLogger(MyZkImage.class);
	private String name=null;
	private byte[] data=null;
	private String cType=null;
	private String format=null;
	private String url;
	public MyZkImage(String url){
		File f=new File(url);
		this.url=url;
		name=f.getName();
		format = getFormatByName(name);
		cType = getContentType(format);
	}
	@Override
	public boolean isBinary() {
		return true;
	}

	@Override
	public boolean inMemory() {
		return true;
	}

	@Override
	public byte[] getByteData() {
		if(data==null){
			try {
				data=Files.readAll(new FileInputStream(url));
			} catch (FileNotFoundException e) {
				logger.error("getByteData", e);
			} catch (IOException e) {
				logger.error("getByteData", e);
			}
		}
		return data;
	}
	private static String getContentType(String format) {
		final String ctype = ContentTypes.getContentType(format);
		return ctype != null ? ctype: "image/" + format;
	}
	private static String getFormatByName(String name) {
		if (name != null) {
			final int j = name.lastIndexOf('.') + 1,
				k = name.lastIndexOf('/') + 1;
			if (j > k && j < name.length())
				return name.substring(j); 
		}
		return null;
	}
	
	@Override
	public String getStringData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getStreamData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reader getReaderData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public String getContentType() {
		return cType;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ImageIcon toImageIcon() {
		return null;
	}


}
