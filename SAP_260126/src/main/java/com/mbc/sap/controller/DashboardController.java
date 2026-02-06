package com.mbc.sap.controller;

import java.util.Date;

import javax.management.modelmbean.RequiredModelMBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mbc.sap.dto.DashboardDto;
import com.mbc.sap.dto.UserDto;
import com.mbc.sap.service.DashboardService;

import jakarta.servlet.http.HttpSession;

@RestController
public class DashboardController {
	
	@Autowired
	DashboardService service;
	
	@GetMapping("get_dashboard")
	public ResponseEntity<DashboardDto> getDashboard(
            @RequestParam("auth") String auth,
            @RequestParam(value = "station_id", required = false) String station_id,
            @RequestParam(value = "user_id", required = false) String user_id) { 
		
		// 1. 요청 정보 확인용 로그 (기존 유지)
		// System.out.println("DashboardController get_dashboard 실행 시간: " + new Date());
		// System.out.println("요청 유저ID: " + user_id + ", 권한(auth): " + auth + ", 역 ID(station_id): " + station_id);
		
		// 2. 서비스 호출 
		DashboardDto result = service.getDashboard(auth, station_id, user_id);
		
		// 3. 응답 반환
		return ResponseEntity.ok(result);
	}
}
