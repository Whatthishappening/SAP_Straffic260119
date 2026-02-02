package com.mbc.sap.dto;

public class Incidentparam {
   private String category;
   private String keyword;
   private int pageNumber;
   private String status;
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
   public Incidentparam(String category, String keyword, int pageNumber, String status) {
	super();
	this.category = category;
	this.keyword = keyword;
	this.pageNumber = pageNumber;
	this.status = status;
   }
   @Override
   public String toString() {
	return "Incidentparam [category=" + category + ", keyword=" + keyword + ", pageNumber=" + pageNumber + ", status="
			+ status + "]";
   }


 
}
