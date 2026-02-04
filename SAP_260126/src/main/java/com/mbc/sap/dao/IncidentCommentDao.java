package com.mbc.sap.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.mbc.sap.dto.IncidentCommentDto;

@Mapper
@Repository
public interface IncidentCommentDao {

    // 1. 댓글 목록 조회
    List<IncidentCommentDto> get_comments(int incident_id);

    // 2. 댓글 등록 (서비스에서 사용하기 위해 추가)
    int insert_comment(IncidentCommentDto dto);

 // 4. 댓글 수정
	int update_commentContent(IncidentCommentDto dto);
	// 4. 댓글 삭제
	int delete_comment(IncidentCommentDto dto);

	int approveUsersBatch(List<String> incident_ids);

}