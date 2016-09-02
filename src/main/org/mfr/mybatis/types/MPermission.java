package org.mfr.mybatis.types;

import java.util.Date;

import org.mfr.data.Site;

public class MPermission {
	private Integer id;
	private MCategory category;
	private MUseracc useracc;
	private String ticket;
	private Date validTill;
	private String sentTo;
	private Integer accessCount;
	private String name;
	private Date modifyDt;
	private String description;
	private Integer allowUpload;
	private Integer allowModify;
	private Integer allowDelete;
	private MUseracc assignedUseracc;
	private String password;
	private Site site;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public MCategory getCategory() {
		return category;
	}
	public void setCategory(MCategory category) {
		this.category = category;
	}
	public MUseracc getUseracc() {
		return useracc;
	}
	public void setUseracc(MUseracc useracc) {
		this.useracc = useracc;
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	public Date getValidTill() {
		return validTill;
	}
	public void setValidTill(Date validTill) {
		this.validTill = validTill;
	}
	public String getSentTo() {
		return sentTo;
	}
	public void setSentTo(String sentTo) {
		this.sentTo = sentTo;
	}
	public Integer getAccessCount() {
		return accessCount;
	}
	public void setAccessCount(Integer accessCount) {
		this.accessCount = accessCount;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getModifyDt() {
		return modifyDt;
	}
	public void setModifyDt(Date modifyDt) {
		this.modifyDt = modifyDt;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getAllowUpload() {
		return allowUpload;
	}
	public void setAllowUpload(Integer allowUpload) {
		this.allowUpload = allowUpload;
	}
	public Integer getAllowModify() {
		return allowModify;
	}
	public void setAllowModify(Integer allowModify) {
		this.allowModify = allowModify;
	}
	public Integer getAllowDelete() {
		return allowDelete;
	}
	public void setAllowDelete(Integer allowDelete) {
		this.allowDelete = allowDelete;
	}
	public MUseracc getAssignedUseracc() {
		return assignedUseracc;
	}
	public void setAssignedUseracc(MUseracc assignedUseracc) {
		this.assignedUseracc = assignedUseracc;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Site getSite() {
		return site;
	}
	public void setSite(Site site) {
		this.site = site;
	}

}
