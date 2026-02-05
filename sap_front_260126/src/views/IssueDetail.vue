<template>
  <div class="detail-container">
    <div class="main-content">
      <div class="detail-header">
        <h2 class="detail-title">
  <span 
    class="status-text" 
    :style="{ color: getStatusConfig(issueData.incident_status).color }"
  >
    [{{ getStatusConfig(issueData.incident_status).label }}]
  </span> 
  
  <input 
    v-if="Edit_titleMode" 
    v-model="issueData.incident_title" 
    class="edit-title-input"
    placeholder="제목을 입력하세요"
  />
  <span v-else>{{ issueData.incident_title || '불러오는 중...' }}</span>
</h2>
        <div class="detail-meta">
          # by <strong>{{ issueData.user_name }}</strong> reported at {{ issueData.create_at || '최근' }}
          
          <div class="header-actions-inline">
            <template v-if="!Edit_titleMode">
              <button class="action-link" @click="Edit_titleMode = true">✏️제목수정</button>
            </template>
            <template v-else>
              <button class="action-link save" @click="update_issueDetail">저장</button>
              <button class="action-link cancel" @click="Edit_titleMode = false">취소</button>
            </template>
          </div>
        </div>
      </div>

      <div class="incident-main-content">
        <div class="comment-item original-post">
          <div class="comment-header">
            <span class="comment-author">상세 내용</span>
            <div class="header-actions">
              <template v-if="!isEditMode">
                <button class="action-link" @click="isEditMode = true">✏️본문편집</button>
              </template>
              <template v-else>
                <button class="action-link save" @click="update_issueDetail">저장</button>
                <button class="action-link cancel" @click="isEditMode = false">취소</button>
              </template>
            </div>
          </div>
          <div class="comment-body">
            <textarea 
              v-if="isEditMode" 
              v-model="issueData.incident_content" 
              class="edit-textarea"
            ></textarea>
            <div v-else class="content-text">
              {{ issueData.incident_content || '내용이 없습니다.' }}
            </div>
          </div>
        </div>
      </div>

      <div class="comment-list">
        <div v-for="(comment, index) in comments" :key="index" class="comment-item">
          <div class="comment-header">
            <div class="header-left">
              <span class="comment-author">{{ comment.user_name }} ({{ comment.user_id }})</span>
              <span class="comment-date">{{ comment.create_at }}</span>
            </div>
            
            <div class="header-actions">
              <template v-if="editingCommentId !== comment.comment_id">
                <button class="action-link" @click="startEdit(comment)">✏️</button>
                <button class="action-link delete" @click="deleteComment(comment.comment_id)">🗑️</button>
              </template>
              <template v-else>
                <button class="action-link save" @click="saveEdit(comment.comment_id)">저장</button>
                <button class="action-link cancel" @click="cancelEdit">취소</button>
              </template>
            </div>
          </div>

          <div class="comment-body">
            <textarea 
              v-if="editingCommentId === comment.comment_id" 
              v-model="editContent" 
              class="edit-comment-textarea"
            ></textarea>
            <div v-else class="comment-text-content">{{ comment.comment_content }}</div>
          </div>
        </div>
      </div>

      <div class="comment-input-section">
        <textarea v-model="newComment" placeholder="댓글을 입력하세요..." class="comment-textarea"></textarea>
        
        <div class="button-group">
          <button class="list-back-btn outline" @click="goToList">목록으로</button>
          <div class="status-change-wrapper">
            <div v-if="activeMenu === 'status'" class="dropdown-menu status-dropdown">
              <div v-for="(label, value) in statusMap" 
                   :key="value" 
                   class="menu-item" 
                   @click.stop="selectStatus(value)">
                {{ label }}
              </div>
            </div>
            <button class="update-btn" @click.stop="toggleMenu('status')">진행 상태변경</button>
          </div>
          <button class="submit-btn" @click="addComment">댓글작성</button>
        </div>
      </div>
    </div>

    <aside class="sidebar" ref="sideContainer">
      <section class="sidebar-section">
        <div class="sidebar-header">
          <span>심각도</span>
          <button class="settings-icon" @click.stop="toggleMenu('severity')">⚙️</button>
        </div>
        <div v-if="activeMenu === 'severity'" class="dropdown-menu">
          <div v-for="lv in ['Critical', 'Major', 'Minor']" :key="lv" 
               class="menu-item" :class="lv.toLowerCase()" @click="selectSeverity(lv)">
            {{ lv }}
          </div>
        </div>
        <div class="sidebar-content">
          <span v-if="issueData.incident_severity" class="badge severity" :class="issueData.incident_severity.toLowerCase()">
            {{ issueData.incident_severity }}
          </span>
        </div>
      </section>

      <section class="sidebar-section">
        <div class="sidebar-header">
          <span>분류 및 위치</span>
          <button class="settings-icon" @click.stop="toggleMenu('location')">⚙️</button>
        </div>
        <div v-if="activeMenu === 'location'" class="dropdown-menu location-picker-simple">
          <StationSimpleSelector @selected="onStationSelected" />
        </div>
        <div class="sidebar-content">
          <div class="badge-row">
          <span v-if="issueData.incident_line_name" 
              class="badge display-box" 
              :class="'line-' + issueData.incident_line_name.replace(' ', '')">
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
const emit = defineEmits(['go-list', 'change-view']);
import { ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';
import StationSimpleSelector from './StationSimpleSelector.vue';

const props = defineProps({
  data: [Number, String]
});

const activeMenu = ref(null);
const isEditMode = ref(false);
const Edit_titleMode = ref(false); // 제목 편집 모드 추가
const newComment = ref('');
const comments = ref([]);
const editingCommentId = ref(null);
const editContent = ref('');

const issueData = ref({
  incident_id: '',
  incident_title: '',
  incident_status: '',
  incident_severity: '',
  incident_line_name: '',
  incident_station_name: '',
  incident_station_id: '',
  incident_content: '',
  user_name: '',
  user_id: '',
  create_at: ''
});

const statusMap = { '1': '대기', '2': '해결중', '3': '완료', '4': '비활성화' };
const goToList = () => {
  emit('go-list');
};
const loadComments = () => {
  if (!props.data) return;
  axios.post("http://localhost:9000/get_comments", { incident_id: props.data })
    .then(resp => { comments.value = resp.data; })
    .catch(err => console.error("댓글 로딩 실패:", err));
};

const loadDetail = () => {
  if (!props.data) return;
  axios.post("http://localhost:9000/get_incident_detail", { incident_id: props.data })
    .then(resp => { if (resp.data) issueData.value = resp.data; })
    .catch(err => console.error("상세 로드 실패:", err));
};

onMounted(() => {
  loadDetail();
  loadComments();
  window.addEventListener('mousedown', handleClickOutside);
});

onUnmounted(() => {
  window.removeEventListener('mousedown', handleClickOutside);
});

const update_issueDetail = () => {
  // 서버에 보내기 전 데이터 확인 (디버깅용)
  console.log("저장될 데이터:", issueData.value);
  axios.post('http://localhost:9000/update_incident', issueData.value)
    .then(resp => {
      if (resp.data === "YES" || resp.data === 1) {
        isEditMode.value = false;
        Edit_titleMode.value = false;
        loadDetail();
      }
    }).catch(() => alert("저장 실패"));
};

const addComment = () => {
  if (!newComment.value.trim()) return;

  // App.vue와 동일한 세션 키 'user_info' 사용
  const loginData = JSON.parse(sessionStorage.getItem("user_info"));

  if (!loginData) {
    alert("로그인이 필요합니다.");
    return;
  }

  const payload = {
    incident_id: Number(issueData.value.incident_id), 
    comment_content: newComment.value,
    user_id: loginData.user_id || loginData.id,
    user_name: loginData.user_name || loginData.name
  };

  axios.post('http://localhost:9000/insert_comment', payload)
    .then(resp => {
      if (resp.data === "YES" || resp.data === 1) {
        newComment.value = '';
        loadComments();
      }
    });
};

const deleteComment = (commentId) => {
  if (!confirm("정말 삭제하시겠습니까?")) return;
  axios.post("http://localhost:9000/delete_comment", { comment_id: commentId })
    .then(() => { loadComments(); });
};

const startEdit = (comment) => {
  editingCommentId.value = comment.comment_id;
  editContent.value = comment.comment_content;
};

const cancelEdit = () => {
  editingCommentId.value = null;
  editContent.value = '';
};

const saveEdit = (commentId) => {
  axios.post("http://localhost:9000/update_commentContent", {
    comment_id: commentId,
    comment_content: editContent.value
  }).then(() => {
    editingCommentId.value = null;
    loadComments();
  });
};

const selectStatus = (val) => {
  // 1. 값 변경
  issueData.value.incident_status = String(val);
  
  axios.post('http://localhost:9000/update_incident_status', issueData.value)
    .then(resp => {
      if (resp.data === "YES") {
        alert('상태 변경 완료');
        activeMenu.value = null;
        loadDetail();
      } else {
        alert('상태 변경 실패 (NO)');
      }
    }).catch(err => {
      console.error(err);
      alert('서버 통신 오류');
    });
};
const selectSeverity = (lv) => { issueData.value.incident_severity = lv; activeMenu.value = null; update_issueDetail(); };

const onStationSelected = (data) => {
 console.log("자식으로부터 받은 데이터:", data); // 디버깅용

  // 1. 자식이 보낸 데이터가 빈 값이어도 그대로 덮어씁니다.
  // (data.line_name이 '' 이면 issueData의 노선도 '' 이 됩니다)
  issueData.value.incident_line_name = data.line_name;
  issueData.value.incident_station_name = data.station_name;
  issueData.value.incident_station_id = data.station_id;
 // 2. 드롭다운 메뉴 닫기
  activeMenu.value = null;
  // 3. 서버에 수정 사항 저장 (DB 업데이트)
  update_issueDetail();
};

const toggleMenu = (type) => { activeMenu.value = activeMenu.value === type ? null : type; };
const handleClickOutside = (e) => {
  if (!e.target.closest('.status-change-wrapper') && !e.target.closest('.settings-icon') && !e.target.closest('.dropdown-menu')) {
    activeMenu.value = null;
  }
};
const getStatusConfig = (status) => {
  const configs = { 
    '1': { label: '대기', color: '#9e9e9e', class: 'wait' }, 
    '2': { label: '해결중', color: '#0e6ca5', class: 'resolving' }, 
    '3': { label: '완료', color: '#17f000', class: 'done' }, 
    '4': { label: '비활성화', color: '#EEEEEE', class: 'disabled' } 
  };
  return configs[status] || { label: '알수없음', color: '#333', class: '' };
};
</script>

<style scoped>
/* 레이아웃 */
.detail-container { display: flex; width: 95%; max-width: 1400px; margin: 20px auto; gap: 30px; font-family: 'Pretendard', sans-serif; }
.main-content { flex: 3; min-width: 0; }

/* 헤더 & 제목 입력창 */
.detail-header { margin-bottom: 25px; padding-bottom: 15px; border-bottom: 1px solid #d1d5da; }
.detail-title { font-size: 26px; font-weight: 600; display: flex; align-items: center; gap: 10px; }
.edit-title-input { font-size: 24px; padding: 5px 10px; border: 2px solid #2188ff; border-radius: 6px; flex: 1; }
.detail-meta { margin-top: 10px; color: #586069; font-size: 14px; display: flex; align-items: center; gap: 15px; }
.header-actions-inline { display: flex; gap: 8px; }

/* 버튼 및 액션 */
.action-link { background: none; border: none; cursor: pointer; color: #586069; font-size: 13px; font-weight: 600; padding: 2px 5px; }
.action-link:hover { text-decoration: underline; }
.action-link.save { color: #28a745; }
.action-link.cancel { color: #d73a49; }
.action-link.delete { color: #cb2431; }

/* 본문 편집 */
.edit-textarea { width: 100%; min-height: 200px; padding: 15px; border: 2px solid #2188ff; border-radius: 6px; resize: vertical; font-size: 15px; line-height: 1.6; }

/* 댓글 스타일 */
.comment-item { border: 1px solid #d1d5da; border-radius: 6px; margin-bottom: 15px; background: #fff; overflow: hidden; }
.comment-header { background-color: #f6f8fa; padding: 10px 15px; border-bottom: 1px solid #d1d5da; font-size: 13px; display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; gap: 15px; align-items: center; }
.comment-body { padding: 20px; white-space: pre-wrap; font-size: 15px; line-height: 1.6; }
.comment-text-content { word-break: break-all; }
.edit-comment-textarea { width: 100%; min-height: 80px; padding: 10px; border: 2px solid #2188ff; border-radius: 4px; resize: vertical; font-size: 15px; }
.list-back-btn{
   background-color: #fff; border: 1px solid #d1d5da; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; 
}
/* 댓글 입력 */
.comment-input-section { margin-top: 30px; }
.comment-textarea { width: 100%; height: 120px; padding: 15px; border: 1px solid #d1d5da; border-radius: 6px; background-color: #f6f8fa; resize: none; box-sizing: border-box; font-size: 14px; }
.button-group { display: flex; justify-content: flex-end; gap: 10px; margin-top: 15px; position: relative; }
.update-btn { background-color: #fff; border: 1px solid #d1d5da; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.submit-btn { background-color: #2ea44f; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }

/* 드롭다운 메뉴 */
.dropdown-menu { position: absolute; bottom: calc(100% + 10px); right: 0; background: #fff; border: 1px solid #ddd; border-radius: 8px; box-shadow: 0 5px 25px rgba(0,0,0,0.1); z-index: 1000; min-width: 160px; }
.sidebar .dropdown-menu { top: 45px !important; bottom: auto; left: 0; right: auto; }
.menu-item { padding: 12px 20px; cursor: pointer; font-size: 13px; border-bottom: 1px solid #f0f0f0; }
.menu-item:hover { background: #f8f9fa; color: #2188ff; }

/* 사이드바 및 배지 */
.sidebar { flex: 1; min-width: 280px; border-left: 1px solid #eee; padding-left: 20px; }
.sidebar-section { margin-bottom: 25px; padding-bottom: 20px; border-bottom: 1px solid #eaecef; position: relative; }
.sidebar-header { display: flex; justify-content: space-between; align-items: center; color: #586069; font-size: 14px; font-weight: 600; margin-bottom: 15px; }
.settings-icon { background: none; border: none; cursor: pointer; font-size: 16px; }

.badge { display: inline-block; padding: 6px 14px; border-radius: 20px; font-size: 11px; font-weight: bold; color: #fff; text-transform: uppercase; }
.badge.severity.critical { background-color: #EE0000; }
.badge.severity.major { background-color: #F89B00; }
.badge.severity.minor { background-color: #17F000; }
.badge.station { background-color: #000; }
.badge-row { display: flex; gap: 8px; margin-bottom: 10px; flex-wrap: wrap; }

/* 노선 색상 */
.line-1호선 { background-color: #2a317c !important; } .line-2호선 { background-color: #2fae35 !important; }
.line-3호선 { background-color: #ff6000 !important; } .line-4호선 { background-color: #1a97dd !important; }
.line-5호선 { background-color: #8936e0 !important; } .line-6호선 { background-color: #b55033 !important; }
.line-7호선 { background-color: #697214 !important; } .line-8호선 { background-color: #e51e6e !important; }
.line-9호선 { background-color: #bb831e !important; }


.status-text {
  font-weight: 800;
  font-size: 18px; 
  margin-right: 5px;
}
</style>