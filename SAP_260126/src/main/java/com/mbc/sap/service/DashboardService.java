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
	    
	    // [중요!] 0. 조회를 하기 전에 기상청 API를 호출해서 DB를 최신화해야 합니다.
	    syncSeoulWeather(); 
	    
	    // 1. 기본 대시보드 요약 정보 가져오기 (이제 DB에 날씨가 들어갔으므로 같이 조회됨)
	    DashboardDto dto = dao.getdashboard(auth, station_id, user_id);
	    
	    // 2. 최근 7일간의 장애 발생 현황 가져오기
	    List<Map<String, Object>> weeklyData = dao.getWeeklyIncidents(auth, station_id, user_id);
	    
	    // 3. 요약 정보 DTO에 리스트 데이터를 세팅
	    if (dto != null) {
	        dto.setWeekly_issue(weeklyData);
	    }
	    
	    return dto; // 최종 객체 리턴
	}
	
	private void syncSeoulWeather() {
	    try {
	        LocalDateTime now = LocalDateTime.now();
	        String tm = now.format(DateTimeFormatter.ofPattern("yyyyMMddHH00"));
	        String authKey = "J_Fm2-RYT9mxZtvkWD_ZFw";
	        String url = "https://apihub.kma.go.kr/api/typ01/url/kma_sfctm2.php?tm=" + tm + "&stn=108&help=0&authKey=" + authKey;

	        RestTemplate restTemplate = new RestTemplate();
	        String response = restTemplate.getForObject(url, String.class);

	        if (response != null && response.contains("108")) {
	            String[] lines = response.split("\n");
	            for (String line : lines) {
	                if (!line.startsWith("#") && line.contains("108")) {
	                    String[] col = line.trim().split("\\s+");
	                    
	                    DashboardDto weatherDto = new DashboardDto();
	                    weatherDto.setWeather_tm(col[0]);    
	                    weatherDto.setWeather_ta(Double.parseDouble(col[11])); // 기온
	                    weatherDto.setWeather_hm(Double.parseDouble(col[13])); // 습도

	                    // [겨울철 특수 로직] 
	                    // RN(16번/인덱스14)은 지금 시간(17시)에 자료가 없으므로 
	                    // RN_DAY(17번/인덱스16)를 가져와서 오늘 비가 왔는지 확인합니다.
	                    
	                    String rnDayRaw = col[16]; // RN_DAY 위치
	                    double rnDay = 0.0;
	                    
	                    if (!rnDayRaw.equals(".") && !rnDayRaw.equals("-9.0")) {
	                        try {
	                            rnDay = Double.parseDouble(rnDayRaw);
	                        } catch (Exception e) {
	                            rnDay = 0.0;
	                        }
	                    }
	                    
	                    // 현재 시간에 비가 오는지 확인하려면 RN_DAY가 0보다 큰지 보면 됩니다.
	                    // (겨울철 1시간 단위 RN은 기상청에서 제공하지 않기 때문)
	                    weatherDto.setWeather_rn(rnDay);

	                    dao.insertWeather(weatherDto);
	                    System.out.println(">>> [겨울철날씨] 시간: " + col[0] + " | 기온: " + col[11] + " | 일강수량(RN_DAY): " + rnDay);
	                    break; 
	                }
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("날씨 동기화 실패: " + e.getMessage());
	    }
	}
}
