package com.mbc.sap.service;

import java.util.List;
import java.util.Map;

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

	public boolean update_incident(IncidentDto dto) {
		  return dao.update_incident(dto) > 0;
    }

	// 6. 장애 단일 상태 업데이트 (기존 기능 복구)
	public boolean update_incident_status(IncidentDto dto) {
	    // dao의 update_incident_status를 호출하여 성공하면 true 반환
	    int result = dao.update_incident_status(dto);
	    return result > 0;
	}

	// 7. 장애 일괄 수정 (새로 만든 기능)
	public boolean update_incident_statusBatch(Map<String, Object> params) {
	    int result = dao.update_incident_statusBatch(params);
	    return result > 0;
	}



}