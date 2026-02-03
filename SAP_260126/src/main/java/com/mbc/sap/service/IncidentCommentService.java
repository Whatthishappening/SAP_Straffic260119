package com.mbc.sap.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mbc.sap.dao.IncidentCommentDao; // Dao 이름 확인 필요
import com.mbc.sap.dto.IncidentCommentDto;


@Service
@Transactional
public class IncidentCommentService {

    @Autowired
    IncidentCommentDao dao;

    // 1. 댓글 목록 조회
    public List<IncidentCommentDto> get_comments(int incident_id) {
        return dao.get_comments(incident_id);
    }

    // 2. 댓글 등록
    public boolean insert_comment(IncidentCommentDto dto) {
        return dao.insert_comment(dto) > 0;
    }

 
    // 3. 댓글 수정 (옵션)
	public boolean update_commentContent(IncidentCommentDto dto) {
		   return dao.update_commentContent(dto) > 0;
    }

	public boolean delete_comment(IncidentCommentDto dto) {
		  return dao.delete_comment(dto) > 0;
    }

	

}