package com.mbc.sap.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mbc.sap.dao.IncidentDao;
import com.mbc.sap.dto.IncidentDto;
import com.mbc.sap.dto.Incidentparam;
import com.mbc.sap.dto.UserDto;
import com.mbc.sap.dto.UserParam;
import com.mbc.sap.service.IncidentService;

@RestController
public class IncidentController {
 @Autowired
 IncidentService service;
 
 @PostMapping("get_incidentlist")
 public Map<String, Object> get_incidentlist(@RequestBody Incidentparam param) {
       

     
     // 1. 이슈 리스트 조회
     List<IncidentDto> list = service.get_incidentlist(param);
     
     // 2. 전체 결과 개수 조회
     int count = service.count_incident(param); 
     
     Map<String, Object> map = new HashMap<>();
     map.put("incidentList", list); // 키 이름을 명확하게 변경
     map.put("cnt", count);
     map.put("curPage", param.getPageNumber());
     
     return map;
 }
 @PostMapping("create_incident")
 public String create_incident(@RequestBody IncidentDto dto) {

 	boolean isS = service.create_incident(dto);
 	
 	String message = "YES";
 	if(!isS) {
 		message="NO";
 	}
 	return message;
 }
 @PostMapping("get_incident_detail")
 public IncidentDto get_incident_detail(@RequestBody Map<String, Object> payload) {
     String incident_id = String.valueOf(payload.get("incident_id"));
     IncidentDto dto = service.get_incident_detail(incident_id);
     return dto;
 }
 //장애 이슈 정보 업데이트 
 @PostMapping("update_incident") 
 public String update_user(@RequestBody IncidentDto dto) {
     boolean isS = service.update_incident(dto);
     
     if(isS) {
         return "YES";
     } else {
         return "NO";
     }
 }
 //장애 이슈 진행 상태 업데이트
 @PostMapping("update_incident_status") 
 public String update_incident_status(@RequestBody IncidentDto dto) {
    

     
     boolean isS = service.update_incident_status(dto);
     
     if(isS) {
         return "YES";
     } else {
         return "NO";
     }
 }
 
 // 단체 리스트 수정하기
 @PostMapping("update_incident_statusBatch")
 public String approveUsersBatch(@RequestBody Map<String, Object> params) {
  
     boolean isSuccess = service.update_incident_statusBatch(params); 
     
     return isSuccess ? "YES" : "NO";
 }
 
 
}
