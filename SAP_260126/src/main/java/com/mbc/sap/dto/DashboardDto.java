package com.mbc.sap.dto;

import java.util.List;
import java.util.Map;

public class DashboardDto {
	// 헤더 및 요약 정보
	private String linetitle;	// 'auth=1' : 전노선 운영현황 , 'auth=2' : O호선 OO역 운영현황
	private String station_id;	// 역 ID
	private int linetotal;		// 전체 노선수
	private int stationtotal;	// 전체 역수
	private int line_stationcount; // 해당 호선의 역수 
	
	// 총보관함 갯수, 사용중인 갯수 
	private int incident_count;			// 장애발생건수
	private int total_lockers; 			// 물품보관함 전체 설치 개수
	private int used_lockers;			// 물품보관함 실시간 사용중 갯수
	
	// 차트 및 그래프용
	private int hour_usedlocker; 		// 시간대별 사용률
	private List<Map<String, Object>> weekly_issue; // 7일간 장애발생현황
	
	// 날씨
	private String weather_tm;			// 관측시각(YYMMDDHH00)
	private double weather_wd;			// 풍향
	private double weather_ws;			// 풍속
	private double weather_ta;			// 기온
	private double weather_hm;			// 습도
	private double weather_rn;			// 강수량
	
		
	public DashboardDto() {
		
	}


	public DashboardDto(String linetitle, String station_id, int linetotal, int stationtotal, int line_stationcount,
			int incident_count, int total_lockers, int used_lockers, int hour_usedlocker,
			List<Map<String, Object>> weekly_issue, String weather_tm, double weather_wd, double weather_ws,
			double weather_ta, double weather_hm, double weather_rn) {
		super();
		this.linetitle = linetitle;
		this.station_id = station_id;
		this.linetotal = linetotal;
		this.stationtotal = stationtotal;
		this.line_stationcount = line_stationcount;
		this.incident_count = incident_count;
		this.total_lockers = total_lockers;
		this.used_lockers = used_lockers;
		this.hour_usedlocker = hour_usedlocker;
		this.weekly_issue = weekly_issue;
		this.weather_tm = weather_tm;
		this.weather_wd = weather_wd;
		this.weather_ws = weather_ws;
		this.weather_ta = weather_ta;
		this.weather_hm = weather_hm;
		this.weather_rn = weather_rn;
	}


	public String getLinetitle() {
		return linetitle;
	}


	public void setLinetitle(String linetitle) {
		this.linetitle = linetitle;
	}


	public String getStation_id() {
		return station_id;
	}


	public void setStation_id(String station_id) {
		this.station_id = station_id;
	}


	public int getLinetotal() {
		return linetotal;
	}


	public void setLinetotal(int linetotal) {
		this.linetotal = linetotal;
	}


	public int getStationtotal() {
		return stationtotal;
	}


	public void setStationtotal(int stationtotal) {
		this.stationtotal = stationtotal;
	}


	public int getLine_stationcount() {
		return line_stationcount;
	}


	public void setLine_stationcount(int line_stationcount) {
		this.line_stationcount = line_stationcount;
	}


	public int getIncident_count() {
		return incident_count;
	}


	public void setIncident_count(int incident_count) {
		this.incident_count = incident_count;
	}


	public int getTotal_lockers() {
		return total_lockers;
	}


	public void setTotal_lockers(int total_lockers) {
		this.total_lockers = total_lockers;
	}


	public int getUsed_lockers() {
		return used_lockers;
	}


	public void setUsed_lockers(int used_lockers) {
		this.used_lockers = used_lockers;
	}


	public int getHour_usedlocker() {
		return hour_usedlocker;
	}


	public void setHour_usedlocker(int hour_usedlocker) {
		this.hour_usedlocker = hour_usedlocker;
	}


	public List<Map<String, Object>> getWeekly_issue() {
		return weekly_issue;
	}


	public void setWeekly_issue(List<Map<String, Object>> weekly_issue) {
		this.weekly_issue = weekly_issue;
	}


	public String getWeather_tm() {
		return weather_tm;
	}


	public void setWeather_tm(String weather_tm) {
		this.weather_tm = weather_tm;
	}


	public double getWeather_wd() {
		return weather_wd;
	}


	public void setWeather_wd(double weather_wd) {
		this.weather_wd = weather_wd;
	}


	public double getWeather_ws() {
		return weather_ws;
	}


	public void setWeather_ws(double weather_ws) {
		this.weather_ws = weather_ws;
	}


	public double getWeather_ta() {
		return weather_ta;
	}


	public void setWeather_ta(double weather_ta) {
		this.weather_ta = weather_ta;
	}


	public double getWeather_hm() {
		return weather_hm;
	}


	public void setWeather_hm(double weather_hm) {
		this.weather_hm = weather_hm;
	}


	public double getWeather_rn() {
		return weather_rn;
	}


	public void setWeather_rn(double weather_rn) {
		this.weather_rn = weather_rn;
	}


	@Override
	public String toString() {
		return "DashboardDto [linetitle=" + linetitle + ", station_id=" + station_id + ", linetotal=" + linetotal
				+ ", stationtotal=" + stationtotal + ", line_stationcount=" + line_stationcount + ", incident_count="
				+ incident_count + ", total_lockers=" + total_lockers + ", used_lockers=" + used_lockers
				+ ", hour_usedlocker=" + hour_usedlocker + ", weekly_issue=" + weekly_issue + ", weather_tm="
				+ weather_tm + ", weather_wd=" + weather_wd + ", weather_ws=" + weather_ws + ", weather_ta="
				+ weather_ta + ", weather_hm=" + weather_hm + ", weather_rn=" + weather_rn + "]";
	}
	
	
	


}	

	