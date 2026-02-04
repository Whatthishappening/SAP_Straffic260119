package com.mbc.sap.dto;

public class Incidentparam {
   private String category;
   private String keyword;
   private int pageNumber;
   private String status;
   
   private String severity;
   private String line_name;
   private String station_name;
   private String sortOrder;
   public String getSeverity() {
	return severity;
}
   public void setSeverity(String severity) {
	this.severity = severity;
   }
   public String getLine_name() {
	return line_name;
   }
   public void setLine_name(String line_name) {
	this.line_name = line_name;
   }
   public String getStation_name() {
	return station_name;
   }
   public void setStation_name(String station_name) {
	this.station_name = station_name;
   }
   public String getSortOrder() {
	return sortOrder;
   }
   public void setSortOrder(String sortOrder) {
	this.sortOrder = sortOrder;
   }
   public Incidentparam() {
	
}
   public String getCategory() {
	return category;
   }
   public void setCategory(String category) {
	this.category = category;
   }
   public String getKeyword() {
	return keyword;
   }
   public void setKeyword(String keyword) {
	this.keyword = keyword;
   }
   public int getPageNumber() {
	return pageNumber;
   }
   public void setPageNumber(int pageNumber) {
	this.pageNumber = pageNumber;
   }
   public String getStatus() {
	return status;
   }
   public void setStatus(String status) {
	this.status = status;
   }

   
   
   public Incidentparam(String category, String keyword, int pageNumber, String status, String severity, String line_name,
		String station_name, String sortOrder) {
	super();
	this.category = category;
	this.keyword = keyword;
	this.pageNumber = pageNumber;
	this.status = status;
	this.severity = severity;
	this.line_name = line_name;
	this.station_name = station_name;
	this.sortOrder = sortOrder;
}
   @Override
   public String toString() {
	return "Incidentparam [category=" + category + ", keyword=" + keyword + ", pageNumber=" + pageNumber + ", status="
			+ status + ", severity=" + severity + ", line_name=" + line_name + ", station_name=" + station_name
			+ ", sortOrder=" + sortOrder + "]";
   }
 


 
}
