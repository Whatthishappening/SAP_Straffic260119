<template>
  <div class="mypage-wrapper">
    <div class="mypage-header">
      <div class="header-content">
        <h2 class="title">마이페이지</h2>
        <button class="edit-btn" @click="user_update">수정하기</button>
      </div>
    </div>

    <div class="info-section">
      <div class="section-top">
        <p class="section-desc">관리자 시스템에 등록된 본인의 기본 정보입니다.</p>
      </div>
      
      <div class="info-table">
        <div class="table-row">
          <div class="table-label">
            <span class="bullet">●</span> 이름
          </div>
          <div class="table-value">{{ user_name }}</div>
        </div>

        <div class="table-row">
          <div class="table-label">
            <span class="bullet">●</span> 아이디(ID)
          </div>
          <div class="table-value">{{ user_id }}</div>
        </div>

        <div class="table-row">
          <div class="table-label">
            <span class="bullet">●</span> 비밀번호
          </div>
          <div class="table-value">••••••••</div>
        </div>

        <div class="table-row">
          <div class="table-label">
            <span class="bullet">●</span> 이메일
          </div>
          <div class="table-value">{{ user_email }}</div>
        </div>

        <div class="table-row route-row">
          <div class="table-label">
            <span class="bullet">●</span> 담당 노선 정보
          </div>
          <div class="table-value">
            <div class="route-display">
              <div class="display-box" :class="'line-' + line_name">
                {{ line_name }}
              </div>
              <div class="display-box station-box">
                {{ station_name }}
              </div>
              <span class="route-info-text">* 현재 해당 구역의 실시간 모니터링 권한을 보유하고 있습니다.</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user_id: "",
      user_name: "",
      user_email: "",
      line_name: "",
      station_name: ""
    }
  },
  mounted() {
    const sessionData = sessionStorage.getItem("user_info") || sessionStorage.getItem("login");
    const login = JSON.parse(sessionData);

    if (!login) {
      console.error("세션에 데이터가 없습니다!");
    } else {
      this.user_id = login.id || login.user_id || '';
      this.user_name = login.name || login.user_name || ''; 
      this.user_email = login.user_email || '';
      this.line_name = login.line_name || '';
      this.station_name = login.station_name || '';
    }
  },
  methods: {
    user_update() {
      this.$emit('change-view', 'myedit');
    }
  }
}
</script>

<style scoped>
/* 전체 컨테이너: 사이드바 옆을 꽉 채우도록 설정 */
.mypage-wrapper {
  width: 100%;
  min-height: 100%;
  background-color: #fff;
  font-family: 'Pretendard', sans-serif;
}

/* 헤더: 화면 상단에 고정된 느낌으로 여백 확보 */
.mypage-header {
  padding: 10px 40px;
  border-bottom: 1px solid #eef0f3;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 35px;
  font-weight: 700;
  color: #1a1c23;
  margin: 0;
}

/* 수정 버튼: 대시보드 테마에 맞는 다크 블루 */
.edit-btn {
  background-color: #3f417e; 
  color: #fff;
  border: none;
  padding: 10px 30px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

/* 메인 섹션 */
.info-section {
  padding: 40px;
}

.section-top {
  margin-bottom: 30px;
}

.section-subtitle {
  font-size: 18px;
  font-weight: 700;
  color: #3f417e;
  margin: 0 0 8px 0;
}

.section-desc {
  font-size: 14px;
  color: #888;
  margin: 0;
}

/* 테이블형 구조: 카드형을 버리고 한 줄씩 깔끔하게 나열 */
.info-table {
  border-top: 2px solid #3f417e;
}

.table-row {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #edeff2;
  min-height: 65px;
}

.table-label {
  width: 200px;
  background-color: #f9fafc;
  height: 100%;
  padding: 20px 25px;
  font-weight: 700;
  color: #4a5568;
  display: flex;
  align-items: center;
}

.table-value {
  padding: 20px 30px;
  flex: 1;
  color: #2d3748;
  font-size: 16px;
}

.bullet {
  font-size: 6px;
  margin-right: 12px;
  color: #3f417e;
}

/* 노선 정보 영역 전용 */
.route-row {
  align-items: flex-start;
}

.route-display {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.display-box {
  padding: 8px 20px;          
  border-radius: 4px;          
  font-size: 14px;
  font-weight: 700;
  text-align: center;
}

.station-box {
  background-color: #fff;
  color: #3f417e;
  border: 1px solid #3f417e;
}

.route-info-text {
  font-size: 13px;
  color: #999;
  margin-left: 10px;
}

/* --- 호선별 컬러 시스템 (원본 유지) --- */
.line-1호선 { background-color: #2a317c; color: white; }
.line-2호선 { background-color: #2fae35; color: white; }
.line-3호선 { background-color: #ff6000; color: white; }
.line-4호선 { background-color: #1a97dd; color: white; }
.line-5호선 { background-color: #822fe1; color: white; }
.line-6호선 { background-color: #ae4908; color: white; }
.line-7호선 { background-color: #636b10; color: white; }
.line-8호선 { background-color: #e6265b; color: white; }
.line-9호선 { background-color: #bdb092; color: white; }
</style>