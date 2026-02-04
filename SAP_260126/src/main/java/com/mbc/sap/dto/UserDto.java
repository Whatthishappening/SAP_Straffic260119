package com.mbc.sap.dto;

public class UserDto {
    private String user_id;
    private String user_pw;
    private String station_id;
    private String station_name;
    private String line_name;
    private String user_name;
    private String user_email;
    private String auth;
    private String current_pw;        
    private boolean passwordChange;

    public UserDto() {}

   
    public boolean isPasswordChange() {
        return passwordChange;
    }

    public void setPasswordChange(boolean passwordChange) {
        this.passwordChange = passwordChange; // 파라미터 이름을 확실하게 대입
    }

    public String getCurrent_pw() {
        return current_pw;
    }

    public void setCurrent_pw(String current_pw) {
        this.current_pw = current_pw;
    }

    // 나머지 Getter/Setter
    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }
    public String getUser_pw() { return user_pw; }
    public void setUser_pw(String user_pw) { this.user_pw = user_pw; }
    public String getStation_id() { return station_id; }
    public void setStation_id(String station_id) { this.station_id = station_id; }
    public String getStation_name() { return station_name; }
    public void setStation_name(String station_name) { this.station_name = station_name; }
    public String getLine_name() { return line_name; }
    public void setLine_name(String line_name) { this.line_name = line_name; }
    public String getUser_name() { return user_name; }
    public void setUser_name(String user_name) { this.user_name = user_name; }
    public String getUser_email() { return user_email; }
    public void setUser_email(String user_email) { this.user_email = user_email; }
    public String getAuth() { return auth; }
    public void setAuth(String auth) { this.auth = auth; }

    @Override
    public String toString() {
        return "UserDto [user_id=" + user_id + ", user_pw=" + user_pw + ", current_pw=" + current_pw 
                + ", passwordChange=" + passwordChange + ", station_id=" + station_id 
                + ", station_name=" + station_name + ", line_name=" + line_name 
                + ", user_name=" + user_name + ", user_email=" + user_email + ", auth=" + auth + "]";
    }
}