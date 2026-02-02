package com.mbc.sap.service;

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
		return dao.getdashboard(auth, station_id);
	}
}
