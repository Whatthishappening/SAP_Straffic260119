package com.mbc.sap.dto;

public class UserParam {
    
    private String searchKeyword; 
    private int pageNumber;
    private int offset;       
    private String sortOrder; 
    private String line_name;
    private String station_name;
    private String auth;      

    public UserParam() {}

    // Getter & Setter
    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        this.offset = pageNumber * 5; 
    }

    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public String getLine_name() { return line_name; }
    public void setLine_name(String line_name) { this.line_name = line_name; }

    public String getStation_name() { return station_name; }
    public void setStation_name(String station_name) { this.station_name = station_name; }

    // [추가] auth Getter & Setter
    public String getAuth() { return auth; }
    public void setAuth(String auth) { this.auth = auth; }

    @Override
    public String toString() {
        return "UserParam [searchKeyword=" + searchKeyword + ", pageNumber=" + pageNumber + 
               ", offset=" + offset + ", sortOrder=" + sortOrder + 
               ", line_name=" + line_name + ", station_name=" + station_name + 
               ", auth=" + auth + "]"; // toString에도 auth 추가
    }
}