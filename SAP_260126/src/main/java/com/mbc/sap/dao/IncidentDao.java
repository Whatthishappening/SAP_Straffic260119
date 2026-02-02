package com.mbc.sap.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.mbc.sap.dto.IncidentDto;
import com.mbc.sap.dto.Incidentparam;

@Mapper
@Repository
public interface IncidentDao {

	int create_incident(IncidentDto dto);

	List<IncidentDto> get_incidentlist(Incidentparam param);

	int count_incident(Incidentparam param);

	IncidentDto get_incident_detail(String incidentd_id);

}
