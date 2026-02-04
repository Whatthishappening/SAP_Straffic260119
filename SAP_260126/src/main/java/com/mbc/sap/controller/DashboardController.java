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
	public ResponseEntity<DashboardDto> getDashboard(@RequestParam("auth") String auth,
													 @RequestParam("station_id") String station_id){
		System.out.println("DashboardController get_dashboard " + new Date());
		
		return ResponseEntity.ok(service.getDashboard(auth, station_id));
	}

}
