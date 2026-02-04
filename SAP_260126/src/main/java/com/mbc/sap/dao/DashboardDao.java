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
	DashboardDto getdashboard(@Param("auth") String auth, 
							  @Param("station_id") String station_id);
	
	List<Map<String, Object>> getWeeklyIncidents(@Param("auth") String auth, 
												 @Param("station_id") String station_id);		
				
}
