package com.mbc.sap.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mbc.sap.dao.IncidentDao;
import com.mbc.sap.dto.IncidentDto;
import com.mbc.sap.dto.Incidentparam;

@Service
@Transactional
public class IncidentService {

    @Autowired
    IncidentDao dao;
    
    // 1. 장애 보고 등록
    public boolean create_incident(IncidentDto dto) {
        return dao.create_incident(dto) > 0;
    }

    // 2. 장애 목록 조회
    public List<IncidentDto> get_incidentlist(Incidentparam param) {
        return dao.get_incidentlist(param);
    }

    // 3. 전체 개수 카운트
    public int count_incident(Incidentparam param) {
        // DAO의 count_incident 호출
        return dao.count_incident(param);
    }
    // 4. 장애 상새목록 조회
	public IncidentDto get_incident_detail(String incident_id) {
		   return dao.get_incident_detail(incident_id);
    }



}