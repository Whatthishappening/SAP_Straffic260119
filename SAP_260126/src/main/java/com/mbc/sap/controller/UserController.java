package com.mbc.sap.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mbc.sap.dto.UserDto;
import com.mbc.sap.dto.UserParam;
import com.mbc.sap.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
public class UserController {

	@Autowired
	UserService service;
	
	// 유저테이블에서 로그인을 위한 ID, PW 체크 기능
	@PostMapping("check_login") // 기능_기능설명
	public UserDto login(UserDto dto, HttpSession session) {
		UserDto user = service.login(dto);
		if(user != null) {
			session.setAttribute("login", user);
		}
		
		return user;
	}
	
	// 회원가입시 user_id 중복확인 
	@PostMapping("check_userid")
	public String check_id(String user_id) {
		boolean isS = service.idcheck(user_id);
		String message = "YES";
		
		if(isS) {
			message = "NO";
		}
		return message;
	}
	
	// 회원가입 성공시
	@PostMapping("add_regi")
	public String add_regi(UserDto dto) {
		boolean isS = service.adduser(dto);
		String message = "YES";
		
		if(!isS) {
			message = "NO";
		}
		return message;
	}
	
	// 회원가입시 station_name을 입력하면 station_id를 불러오기
	@PostMapping("search_stationid")
	public List<String> search_stationid(UserDto dto){
		return service.search_stationid(dto);
	}
	
	// 몇호선을 선택하면 해당 호선의 역이 출력
	@GetMapping("search_stationname")
	public List<Map<String, Object>> search_stationName(@RequestParam("line_name") String line_name){
		return service.getstationname(line_name);
	}
	// 마이페이지 정보 수정

	@PostMapping("update_user")
	public String updateUser(@RequestBody UserDto userDto) {
	    // 2. 비밀번호 변경 모드 체크
	    if (userDto.isPasswordChange()) { 
	        UserDto checkDto = new UserDto();
	        checkDto.setUser_id(userDto.getUser_id()); 
	        checkDto.setUser_pw(userDto.getCurrent_pw()); 
	        UserDto check = service.login(checkDto); 
	        if (check == null) {
	            return "PW_ERROR"; 
	        }
	    }

	    // 3. 업데이트 실행
	    boolean isS = service.update_user(userDto); 
	    return isS ? "YES" : "NO";
	}
	
    @GetMapping("get_newuserlist")
    public Map<String, Object> getNewUserList(UserParam param) {
        List<UserDto> list = service.get_newuserlist(param);
        
        // 2. 전체 대기자 수 (전체 행 개수)
        int count = service.count_newuser();
        // 3. 응답 맵 구성
        Map<String, Object> map = new HashMap<>();
        map.put("newuserlist", list);  // 데이터 리스트
        map.put("cnt", count);         // 전체 개수 (Total Count)
        map.put("curPage", param.getPageNumber()); // 현재 페이지 번호 저장용
        
        return map;
    }
    //신규유저 승인 거절
    @PostMapping("reject_user") 
    public String reject_user(@RequestBody UserDto dto) {
        boolean isS = service.reject_user(dto);
        
        if(isS) {
            return "YES";
        } else {
            return "NO";
        }
    }
    //신규 유저 승인 완료
    @PostMapping("approve_user") 
    public String approve_user(@RequestBody UserDto dto) {
        boolean isS = service.approve_user(dto);
        
        if(isS) {
            return "YES";
        } else {
            return "NO";
        }
    }
    
 // 기존 가입 계정 조회 (검색/필터 포함)
    @GetMapping("get_olduserlist")
    public Map<String, Object> get_olduserlist(UserParam param) {
        // 1. 페이지네이션 처리 (5개씩 노출)
        // Vue에서 page_old를 0부터 시작해서 요청마다 ++하므로 계산식은 적절합니다.
        param.setOffset(param.getPageNumber() * 5); 
        // 2. 서비스 호출
        List<UserDto> list = service.get_olduserlist(param);
        int count = service.count_olduser(param); 
        
        Map<String, Object> map = new HashMap<>();
        map.put("olduserlist", list);
        map.put("cnt", count);
        
        return map;
    }
    
    @PostMapping("approve_users_batch")
    public String approveUsersBatch(@RequestBody Map<String, Object> params) {
        List<String> userIds = (List<String>) params.get("userIds");
        boolean isSuccess = service.approveUsersBatch(userIds);
        return isSuccess ? "YES" : "NO";
    }
    
    @PostMapping("reject_usersBatch")
    public String reject_usersBatch(@RequestBody Map<String, Object> params) {
        List<String> userIds = (List<String>) params.get("userIds");
        boolean isSuccess = service.reject_usersBatch(userIds);
        return isSuccess ? "YES" : "NO";
    }
    
    @PostMapping("update_olduser_auth") 
    public String  update_olduser_auth(@RequestBody UserDto dto) {
        boolean isS = service. update_olduser_auth(dto);
        
        if(isS) {
            return "YES";
        } else {
            return "NO";
        }
    }
    
}
