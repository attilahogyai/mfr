package org.mfr.web;

import java.io.InputStream;

import org.mfr.data.Category;

public class UploadedFileDescriptor {
	private String name;
	private InputStream inputStream;
	private String filePath;
	private String folderName;
	private Category category;
	
	private String remoteUri;
	
	private int provider=0;

	public UploadedFileDescriptor(String fileName, InputStream is) {
		super();
		this.name = fileName;
		this.inputStream=is;
	}
	public String getName() {
		return name;
	}
	public void setName(String fileName) {
		this.name = fileName;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getRemoteUri() {
		return remoteUri;
	}
	public void setRemoteUri(String remoteUri) {
		this.remoteUri = remoteUri;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj==null || name==null) return false;
		UploadedFileDescriptor ufd=(UploadedFileDescriptor)obj;
		boolean file=name.equals(ufd.name);
		if(file && (folderName==null && ufd.folderName==null)){
			return true;
		}else if(file && folderName!=null && ufd.folderName!=null){
			return folderName.equals(ufd.folderName);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return remoteUri!=null?1:2;
	}
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public int getProvider() {
		return provider;
	}
	public void setProvider(int provider) {
		this.provider = provider;
	}
	
}

	