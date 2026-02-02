<template>
  <div class="detail-container">
    <div class="main-content">
      
      <div class="detail-header">
        <h2 class="detail-title">
          <span class="status-text" :class="getStatusClass(issueData.incident_status)">
            [{{ getStatusLabel(issueData.incident_status) }}]
          </span> 
          {{ issueData.incident_title || '불러오는 중...' }}
        </h2>
        <div class="detail-meta">
          # by <strong>{{ issueData.user_name }}</strong> reported at {{ issueData.create_at || '최근' }}
        </div>
      </div>

      <div class="incident-main-content">
        <div class="comment-item original-post">
          <div class="comment-header">
            <span class="comment-author">📝 상세 내용</span>
            <span class="comment-date">ID: {{ issueData.incident_id }}</span>
          </div>
          <div class="comment-body">
            {{ issueData.incident_content || '내용이 없습니다.' }}
          </div>
        </div>
      </div>

      <div class="comment-list">
        <div v-for="(comment, index) in comments" :key="index" class="comment-item">
          <div class="comment-header">
            <span class="comment-author">{{ comment.user }}</span>
            <span class="comment-date">{{ comment.time }}</span>
            <div class="comment-actions">
              <button class="action-btn edit">✏️편집</button>
              <button v-if="comment.isMine" class="action-btn delete">🗑️삭제</button>
            </div>
          </div>
          <div class="comment-body">
            {{ comment.content }}
          </div>
        </div>
      </div>

      <div class="comment-input-section">
        <textarea v-model="newComment" placeholder="댓글을 입력하세요..." class="comment-textarea"></textarea>
        <div class="button-group">
          <button class="status-btn">상태변경</button>
          <button class="submit-btn" @click="addComment">댓글작성</button>
        </div>
      </div>
    </div>
    <aside class="sidebar">
      <section class="sidebar-section">
        <div class="sidebar-header">
          <span>심각도</span>
          <button class="settings-icon">⚙️</button>
        </div>
        <div class="sidebar-content">
          <span v-if="issueData.incident_severity" 
                class="badge severity" 
                :class="issueData.incident_severity.toLowerCase()">
            {{ issueData.incident_severity }}
          </span>
        </div>
      </section>

      <section class="sidebar-section">
        <div class="sidebar-header">
          <span>분류 및 위치</span>
          <button class="settings-icon">⚙️</button>
        </div>
        <div class="sidebar-content">
          <div class="badge-row">
            <span v-if="issueData.incident_line_name" 
                  class="badge display-box" 
                  :class="'line-' + issueData.incident_line_name">
              {{ issueData.incident_line_name }}
            </span>
            <span v-if="issueData.incident_station_name" class="badge station">
              {{ issueData.incident_station_name }}
            </span>
          </div>
          <div class="reporter-info">
             작성자 ID: {{ issueData.user_id }}<br>
             담당자: {{ issueData.user_name }}
          </div>
        </div>
      </section>
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps({
  data: [Number, String]
});

const issueData = ref({
  incident_id: '',
  incident_title: '',
  incident_status: '',
  incident_severity: '',
  incident_line_name: '',
  incident_station_name: '',
  incident_content: '',
  user_name: '',
  user_id: '',
  create_at: ''
});

const newComment = ref('')
const comments = ref([
  { user: 'BaeYoungHwan', time: '5 hours ago', content: '현장 확인이 필요합니다.', isMine: false },
])

onMounted(() => {
  const incidentId = props.data;
  if (incidentId) {
    // 쿼리 파라미터가 아닌 Body 데이터로 전송 (PostgreSQL 캐스팅 문제 해결 완료 가정)
    axios.post("http://localhost:9000/get_incident_detail", {
      incident_id: incidentId
    })
    .then(resp => {
      console.log("서버 데이터 로드 완료:", resp.data);
      if (resp.data) {
        issueData.value = resp.data;
      }
    })
    .catch(err => {
      console.error("로딩 실패:", err);
      alert("데이터를 가져오는 중 오류가 발생했습니다.");
    });
  }
});

const getStatusLabel = (s) => ({ '1': '대기', '2': '해결중', '3': '완료' }[s] || '알수없음')
const getStatusClass = (s) => ({ '1': 'wait', '2': 'resolving', '3': 'done' }[s] || '')

const addComment = () => {
  if(!newComment.value.trim()) return
  comments.value.push({
    user: '현장관리자',
    time: '방금 전',
    content: newComment.value,
    isMine: true
  })
  newComment.value = ''
}
</script>

<style scoped>
.detail-container {
  display: flex;
  width: 95%;
  max-width: 1400px;
  margin: 20px auto;
  gap: 30px; /* 메인과 사이드바 간격 */
  font-family: 'Pretendard', sans-serif;
  color: #24292e;
}

.main-content {
  flex: 3; /* 3:1 비율 */
  min-width: 0;
}

/* 게시글 헤더 스타일 */
.detail-header {
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #d1d5da;
}
.detail-title { font-size: 26px; font-weight: 600; margin-bottom: 10px; }
.status-text.resolving { color: #2188ff; }
.status-text.wait { color: #6a737d; }
.status-text.done { color: #28a745; }
.detail-meta { color: #586069; font-size: 14px; }

/* 본문 및 댓글 스타일 공통 */
.incident-main-content { margin-bottom: 30px; }
.comment-list { display: flex; flex-direction: column; gap: 15px; }
.comment-item {
  border: 1px solid #d1d5da;
  border-radius: 6px;
  background: white;
  overflow: hidden;
}

/* 본문(Original Post) 강조 */
.original-post { border: 1px solid #2188ff; } 
.original-post .comment-header { background-color: #f1f8ff; }

.comment-header {
  background-color: #f6f8fa;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #d1d5da;
  font-size: 13px;
}
.comment-author { font-weight: 600; margin-right: 10px; }
.comment-date { color: #586069; flex: 1; }
.comment-body {
  padding: 20px;
  line-height: 1.6;
  font-size: 15px;
  white-space: pre-wrap; /* 엔터키 반영 */
}

/* 댓글 입력 */
.comment-input-section { margin-top: 30px; }
.comment-textarea {
  width: 100%; height: 100px; padding: 15px;
  border: 1px solid #d1d5da; border-radius: 6px;
  resize: none; background-color: #f6f8fa; box-sizing: border-box;
}
.button-group { display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px; }
.submit-btn { background-color: #2ea44f; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }

/* 사이드바 */
.sidebar { flex: 1; min-width: 250px; }
.sidebar-section {
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eaecef;
}
.sidebar-header {
  display: flex; justify-content: space-between;
  color: #586069; font-size: 14px; font-weight: 600; margin-bottom: 15px;
}

/* 배지들 */
.badge { padding: 4px 12px; border-radius: 12px; color: white; font-weight: bold; font-size: 12px; display: inline-block; }
.badge.severity.critical { background-color: #EE0000; }
.badge.severity.major { background-color: #F89B00; }
.badge.severity.minor { background-color: #17F000; }
.badge.station { background-color: #444; }
.badge-row { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 10px; }

/* 호선 컬러 */
.line-1호선 { background-color: #2a317c; }
.line-2호선 { background-color: #2fae35; }
.line-3호선 { background-color: #ff6000; }
.line-4호선 { background-color: #1a97dd; }
.line-5호선 { background-color: #822fe1; }
.line-6호선 { background-color: #ae4908; }

.reporter-info { font-size: 13px; color: #586069; line-height: 1.8; }
</style>