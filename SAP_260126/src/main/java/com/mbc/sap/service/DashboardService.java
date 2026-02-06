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
        // (여기서 linetitle이 auth=1일 때 "전노선 운영현황" 등으로 DB에서 넘어오는지 확인 필요)
        DashboardDto dto = dao.getdashboard(auth, station_id, user_id);
        
        // 만약 DB에서 가져온 dto가 null이라면 새로 생성 (안전장치)
        if (dto == null) {
            dto = new DashboardDto();
        }

        // 2. 최근 7일간의 장애 발생 현황 가져오기
        // 마스터(auth=1)인 경우, 우리가 수정한 XML 로직에 의해 
        // 환승역 데이터가 여러 호선으로 복제된 리스트가 weeklyData에 담깁니다.
        List<Map<String, Object>> weeklyData = dao.getWeeklyIncidents(auth, station_id, user_id);
        dto.setWeekly_issue(weeklyData);
        
        // 3. 날씨 추가하기
        DashboardDto weatherData = dao.getWeather();
        
        if (weatherData != null) {
            dto.setWeather_tm(weatherData.getWeather_tm());
            dto.setWeather_wd(weatherData.getWeather_wd());
            dto.setWeather_ws(weatherData.getWeather_ws());
            dto.setWeather_ta(weatherData.getWeather_ta());
            dto.setWeather_hm(weatherData.getWeather_hm());
            dto.setWeather_rn(weatherData.getWeather_rn());
        }
        
        // 4. 권한별 타이틀 강제 설정 (선택 사항: DB에서 처리 안 할 경우 여기서 수행)
        if ("1".equals(auth)) {
            dto.setLinetitle("전노선 운영현황");
        }
        
        return dto;
    }
}
