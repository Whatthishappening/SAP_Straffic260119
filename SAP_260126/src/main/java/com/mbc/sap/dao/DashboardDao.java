package com.mbc.sap.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.mbc.sap.dto.DashboardDto;

@Mapper
@Repository
public interface DashboardDao {
	
	// 대시보드 운영 현황
	DashboardDto getdashboard(@Param("auth") String auth, 
							  @Param("station_id") String station_id,
							  @Param("user_id") String user_id);
	
	// 최근 장애 발생 현황
	List<Map<String, Object>> getWeeklyIncidents(@Param("auth") String auth, 
												 @Param("station_id") String station_id,
												 @Param("user_id") String user_id);	
	
	DashboardDto getWeather();
				
}
