package com.mbc.sap.dto;

import java.time.LocalDateTime;

public class IncidentCommentDto {
	private Integer comment_id;      // comment_id (SERIAL)
    private Integer incident_id;     // incident_id (int)
    private String comment_content;  // comment_content (VARCHAR)
    private String user_id;          // user_id (VARCHAR)
    private String user_name;        // user_name (VARCHAR)
    private LocalDateTime create_at; // create_at (TIMESTAMP)

    // 화면 출력용 포맷팅 필드 
    private String formatted_date;
    
    public IncidentCommentDto() {
	}

	public Integer getComment_id() {
		return comment_id;
	}

	public void setComment_id(Integer comment_id) {
		this.comment_id = comment_id;
	}

	public Integer getIncident_id() {
		return incident_id;
	}

	public void setIncident_id(Integer incident_id) {
		this.incident_id = incident_id;
	}

	public String getComment_content() {
		return comment_content;
	}

	public void setComment_content(String comment_content) {
		this.comment_content = comment_content;
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

	public LocalDateTime getCreate_at() {
		return create_at;
	}

	public void setCreate_at(LocalDateTime create_at) {
		this.create_at = create_at;
	}

	public String getFormatted_date() {
		return formatted_date;
	}

	public void setFormatted_date(String formatted_date) {
		this.formatted_date = formatted_date;
	}

	public IncidentCommentDto(Integer comment_id, Integer incident_id, String comment_content, String user_id,
			String user_name, LocalDateTime create_at, String formatted_date) {
		super();
		this.comment_id = comment_id;
		this.incident_id = incident_id;
		this.comment_content = comment_content;
		this.user_id = user_id;
		this.user_name = user_name;
		this.create_at = create_at;
		this.formatted_date = formatted_date;
	}

	@Override
	public String toString() {
		return "IncidentCommentDto [comment_id=" + comment_id + ", incident_id=" + incident_id + ", comment_content="
				+ comment_content + ", user_id=" + user_id + ", user_name=" + user_name + ", create_at=" + create_at
				+ ", formatted_date=" + formatted_date + "]";
	}
    
    
}
