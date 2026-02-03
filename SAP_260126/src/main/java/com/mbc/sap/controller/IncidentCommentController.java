package com.mbc.sap.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mbc.sap.dto.IncidentCommentDto;
import com.mbc.sap.dto.UserDto;
import com.mbc.sap.dto.UserParam;
import com.mbc.sap.service.IncidentCommentService;

import jakarta.servlet.http.HttpSession;


@RestController
public class IncidentCommentController {
	 @Autowired
	 IncidentCommentService service;
	 
	// 1. 댓글 목록 가져오기
	    @PostMapping("/get_comments")
	    public List<IncidentCommentDto> get_comments(@RequestBody Map<String, Object> param) {
	        // Vue에서 보낸 { incident_id: 123 } 값을 꺼냄
	        int incident_id = Integer.parseInt(param.get("incident_id").toString());
	        
	        List<IncidentCommentDto> list = service.get_comments(incident_id);
	        System.out.println("조회된 댓글 수: " + list.size());
	        
	        return list;
	    }
	    
	    // 댓글 저장하기
	    @PostMapping("/insert_comment")
	    @ResponseBody // JSON 응답을 위해
	    public String insert_comment(@RequestBody IncidentCommentDto dto) {
	     
	        boolean isS = service.insert_comment(dto);

	     	
	     	String message = "YES";
	     	if(!isS) {
	     		message="NO";
	     	}
	     	return message;
	     }
	    
	    // 댓글 수정하기
	    @PostMapping("update_commentContent")
	    @ResponseBody // JSON 응답을 위해
	    public String update_comment_content(@RequestBody IncidentCommentDto dto) {
	     
	        boolean isS = service.update_commentContent(dto);

	     	
	     	String message = "YES";
	     	if(!isS) {
	     		message="NO";
	     	}
	     	return message;
	     }
	    
	    // 댓글 수정하기
	    @PostMapping("delete_comment")
	    @ResponseBody // JSON 응답을 위해
	    public String delete_comment(@RequestBody IncidentCommentDto dto) {
	     
	        boolean isS = service.delete_comment(dto);

	     	
	     	String message = "YES";
	     	if(!isS) {
	     		message="NO";
	     	}
	     	return message;
	     }
	
}
