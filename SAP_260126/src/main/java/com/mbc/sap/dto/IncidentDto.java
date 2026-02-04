package com.mbc.sap.dto;

public class IncidentDto {

	private Integer incident_id;       
    private String incident_title;      
    private String incident_line_name;  
    private String incident_station_id;   
    private String incident_station_name;
    private String incident_content;    
    private String incident_status;    
    private String incident_severity; 
    private String user_id;           
    private String user_name;           
    private String create_at;     
    private String complete_at;
    private Integer comment_cnt;
   
	@Override
	public String toString() {
		return "IncidentDto [incident_id=" + incident_id + ", incident_title=" + incident_title
				+ ", incident_line_name=" + incident_line_name + ", incident_station_id=" + incident_station_id
				+ ", incident_station_name=" + incident_station_name + ", incident_content=" + incident_content
				+ ", incident_status=" + incident_status + ", incident_severity=" + incident_severity + ", user_id="
				+ user_id + ", user_name=" + user_name + ", create_at=" + create_at + ", complete_at=" + complete_at
				+ ", comment_cnt=" + comment_cnt + "]";
	}
	public Integer getIncident_id() {
		return incident_id;
	}
	public void setIncident_id(Integer incident_id) {
		this.incident_id = incident_id;
	}
	public String getIncident_title() {
		return incident_title;
	}
	public void setIncident_title(String incident_title) {
		this.incident_title = incident_title;
	}
	public String getIncident_line_name() {
		return incident_line_name;
	}
	public void setIncident_line_name(String incident_line_name) {
		this.incident_line_name = incident_line_name;
	}
	public String getIncident_station_id() {
		return incident_station_id;
	}
	public void setIncident_station_id(String incident_station_id) {
		this.incident_station_id = incident_station_id;
	}
	public String getIncident_station_name() {
		return incident_station_name;
	}
	public void setIncident_station_name(String incident_station_name) {
		this.incident_station_name = incident_station_name;
	}
	public String getIncident_content() {
		return incident_content;
	}
	public void setIncident_content(String incident_content) {
		this.incident_content = incident_content;
	}
	public String getIncident_status() {
		return incident_status;
	}
	public void setIncident_status(String incident_status) {
		this.incident_status = incident_status;
	}
	public String getIncident_severity() {
		return incident_severity;
	}
	public void setIncident_severity(String incident_severity) {
		this.incident_severity = incident_severity;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public String getCreate_at() {
		return create_at;
	}
	public void setCreate_at(String create_at) {
		this.create_at = create_at;
	}
	public String getComplete_at() {
		return complete_at;
	}
	public void setComplete_at(String complete_at) {
		this.complete_at = complete_at;
	}
	public void setComment_cnt(Integer comment_cnt) {
		this.comment_cnt = comment_cnt;
	}
}