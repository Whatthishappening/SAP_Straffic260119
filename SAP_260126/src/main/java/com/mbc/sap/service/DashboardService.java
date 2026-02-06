package com.mbc.sap.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.mbc.sap.dao.DashboardDao;
import com.mbc.sap.dto.DashboardDto;

@Service
@Transactional
public class DashboardService {
	
	@Autowired
	DashboardDao dao;
	
	public DashboardDto getDashboard(String auth, String station_id, String user_id) {
	    
	    // 1. 기본 대시보드 요약 정보 가져오기 
	    DashboardDto dto = dao.getdashboard(auth, station_id, user_id);
	    
	    // 2. 최근 7일간의 장애 발생 현황 가져오기
	    List<Map<String, Object>> weeklyData = dao.getWeeklyIncidents(auth, station_id, user_id);
	    
	    
	    // 3. 날씨 추가하기
	    DashboardDto weatherData = dao.getWeather();
	    
	    // 4. 요약 정보 DTO에 리스트 데이터를 세팅
	    if (dto != null) {
	        dto.setWeekly_issue(weeklyData);
	        
	        if(weatherData != null) {
	        	dto.setWeather_tm(weatherData.getWeather_tm());
	        	dto.setWeather_wd(weatherData.getWeather_wd());
	        	dto.setWeather_ws(weatherData.getWeather_ws());
	        	dto.setWeather_ta(weatherData.getWeather_ta());
	        	dto.setWeather_hm(weatherData.getWeather_hm());
	        	dto.setWeather_rn(weatherData.getWeather_rn());
	        }
	    }
	    
	    return dto; // 최종 객체 리턴
	}

}
