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
 
 @PostMapping("/get_incidentlist")
 public Map<String, Object> get_incidentlist(@RequestBody Incidentparam param) {
       
     System.out.println("검색 요청 데이터: " + param.toString());
     
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
 	System.out.println("작성창실행..."+ new Date());
 	boolean isS = service.create_incident(dto);
 	
 	String message = "YES";
 	if(!isS) {
 		message="NO";
 	}
 	return message;
 }
 @PostMapping("get_incident_detail")
 public IncidentDto get_incident_detail(@RequestBody Map<String, Object> payload) {
     System.out.println("====== [상세조회 시작] ======");
     
     String incident_id = String.valueOf(payload.get("incident_id"));
     System.out.println("👉 요청받은 ID: " + incident_id);
     
     IncidentDto dto = service.get_incident_detail(incident_id);
     
     if(dto != null) {
         // 이제 toString() 덕분에 데이터가 예쁘게 찍힙니다!
         System.out.println("✅ DB에서 가져온 데이터: " + dto.toString());
     } else {
         System.out.println("🚨 [오류] DB 조회 결과가 NULL입니다. ID를 확인하세요.");
     }
     
     return dto;
 }
 //장애 이슈 정보 업데이트 
 @PostMapping("update_incident") 
 public String update_user(@RequestBody IncidentDto dto) {
    
     System.out.println("전달받은 데이터 확인: " + dto.toString()); 
     
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
    
     System.out.println("전달받은 데이터 확인: " + dto.toString()); 
     
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
     System.out.println("일괄 수정 요청 발생: " + params);
     
     boolean isSuccess = service.update_incident_statusBatch(params); 
     
     return isSuccess ? "YES" : "NO";
 }
 
 
}
