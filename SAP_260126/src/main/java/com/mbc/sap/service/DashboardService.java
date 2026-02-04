package com.mbc.sap.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mbc.sap.dao.DashboardDao;
import com.mbc.sap.dto.DashboardDto;

@Service
@Transactional
public class DashboardService {
	
	@Autowired
	DashboardDao dao;
	
	public DashboardDto getDashboard(String auth, String station_id) {
		// 1. 기본 대시보드 요약 정보 가져오기 (incident_count 등 포함)
		DashboardDto dto = dao.getdashboard(auth, station_id);
		
		// 2. 최근 7일간의 장애 발생 현황(리스트) 가져오기
		List<Map<String, Object>> weeklyData = dao.getWeeklyIncidents(auth, station_id);
		
		// 3. 요약 정보 DTO에 리스트 데이터를 세팅
		if (dto != null) {
			dto.setWeekly_issue(weeklyData);
		}
		
		return dto;
	}
}
