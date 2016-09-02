package org.mfr.mybatis.types;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.mfr.data.ExifData;
import org.mfr.data.PhotoCategory;
import org.mfr.data.Useracc;

public class MPhoto {
	private Integer id;
	private String name;
	private String description;
	private Date importDate;
	private String owner;
	private String path;
	private Integer accessCount;
	private MUseracc useracc;
	private Date taken;
	private String title;
	private Integer provider;
	private String providerPath;
	private String providerUrl;
	private MExifData exifData;
	private Integer size;
	private Set<PhotoCategory> photoCategories = new HashSet<PhotoCategory>(0);
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getImportDate() {
		return importDate;
	}
	public void setImportDate(Date importDate) {
		this.importDate = importDate;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Integer getAccessCount() {
		return accessCount;
	}
	public void setAccessCount(Integer accessCount) {
		this.accessCount = accessCount;
	}
	public MUseracc getUseracc() {
		return useracc;
	}
	public void setUseracc(MUseracc useracc) {
		this.useracc = useracc;
	}
	public Date getTaken() {
		return taken;
	}
	public void setTaken(Date taken) {
		this.taken = taken;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getProvider() {
		return provider;
	}
	public void setProvider(Integer provider) {
		this.provider = provider;
	}
	public String getProviderPath() {
		return providerPath;
	}
	public void setProviderPath(String providerPath) {
		this.providerPath = providerPath;
	}
	public String getProviderUrl() {
		return providerUrl;
	}
	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}
	public MExifData getExifData() {
		return exifData;
	}
	public void setExifData(MExifData exifData) {
		this.exifData = exifData;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public Set<PhotoCategory> getPhotoCategories() {
		return photoCategories;
	}
	public void setPhotoCategories(Set<PhotoCategory> photoCategories) {
		this.photoCategories = photoCategories;
	}
}
