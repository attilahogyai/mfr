package org.mfr.mybatis.types;

import java.util.Date;

public class MCategory {
	private Long id;
	private String name;
	private String owner1;
	private String description;
	private Integer ispublic;
	private Date modifyDt;
	private MUseracc useracc;
	private Integer accessCount;
	private Integer recommend;
	private Integer blog;
	private Integer allowDownload;
	private Integer provider;
	private String path;
	private Date createDt;
	private Integer sorting;
	private Integer sortingDir;
	private Integer showComment;
	private transient Integer photoCount;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOwner1() {
		return owner1;
	}
	public void setOwner1(String owner1) {
		this.owner1 = owner1;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getIspublic() {
		return ispublic;
	}
	public void setIspublic(Integer ispublic) {
		this.ispublic = ispublic;
	}
	public Date getModifyDt() {
		return modifyDt;
	}
	public void setModifyDt(Date modifyDt) {
		this.modifyDt = modifyDt;
	}
	public MUseracc getUseracc() {
		return useracc;
	}
	public void setUseracc(MUseracc useracc) {
		this.useracc = useracc;
	}
	public Integer getAccessCount() {
		return accessCount;
	}
	public void setAccessCount(Integer accessCount) {
		this.accessCount = accessCount;
	}
	public Integer getRecommend() {
		return recommend;
	}
	public void setRecommend(Integer recommend) {
		this.recommend = recommend;
	}
	public Integer getBlog() {
		return blog;
	}
	public void setBlog(Integer blog) {
		this.blog = blog;
	}
	public Integer getAllowDownload() {
		return allowDownload;
	}
	public void setAllowDownload(Integer allowDownload) {
		this.allowDownload = allowDownload;
	}
	public Integer getProvider() {
		return provider;
	}
	public void setProvider(Integer provider) {
		this.provider = provider;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Date getCreateDt() {
		return createDt;
	}
	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}
	public Integer getSorting() {
		return sorting;
	}
	public void setSorting(Integer sorting) {
		this.sorting = sorting;
	}
	public Integer getSortingDir() {
		return sortingDir;
	}
	public void setSortingDir(Integer sortingDir) {
		this.sortingDir = sortingDir;
	}
	public Integer getShowComment() {
		return showComment;
	}
	public void setShowComment(Integer showComment) {
		this.showComment = showComment;
	}
	public Integer getPhotoCount() {
		return photoCount;
	}
	public void setPhotoCount(Integer photoCount) {
		this.photoCount = photoCount;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
