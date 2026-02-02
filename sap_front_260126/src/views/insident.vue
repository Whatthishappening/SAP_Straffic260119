<template>
  <div class="issue-container">
    <div class="header-section">
      <div class="search-wrapper">
        <select v-model="searchType" class="type-select">
          <option value="title">제목</option>
          <option value="user">작성자</option>
          <option value="station">역 이름</option>
        </select>
        <div class="divider"></div>
        <input 
          type="text" 
          v-model="searchKeyword"
          placeholder="장애 이슈를 검색해보세요..." 
          class="main-search" 
          @keyup.enter="onSearch"
        />
      </div>
      <button class="search-icon-btn" @click="onSearch">🔍</button>
      <button class="create-btn" @click="goToCreatePage">Create</button>
    </div>

    <div class="filter-bar">
      <div class="filter-left">
        <div class="status-tabs-container">
          <div 
            class="status-tab" 
            :class="{ active: activeStatus === 'open' }" 
            @click="changeStatus('open')"
          >
            <span class="dot open-dot"></span>
            <strong>Open</strong>
          </div>
          <div 
            class="status-tab" 
            :class="{ active: activeStatus === 'close' }" 
            @click="changeStatus('close')"
          >
            <span class="dot close-dot"></span>
            <strong>Close</strong>
          </div>
          <div class="tab-indicator" :style="indicatorStyle"></div>
        </div>
      </div>
      
      <div class="sort-group">
        <select v-model="filterSeverity"><option value="">심각도 ▼</option></select>
        <select v-model="filterLine"><option value="">호선 ▼</option></select>
        <select><option value="">역 ▼</option></select>
        <select><option value="">정렬 ▼</option></select>
      </div>
    </div>

    <ul class="issue-list">
      <li v-if="issues.length === 0" class="empty">데이터가 없습니다.</li>
      
      <li 
        v-for="issue in issues" 
        :key="issue.incident_id" 
        class="issue-item" 
        @click="goToDetail(issue.incident_id)"
      >
        <div class="item-checkbox" @click.stop> <input type="checkbox" />
        </div>

        <div class="issue-main">
          <div class="issue-title-row">
            <span class="status-text" :class="issue.incident_severity?.toLowerCase()">
              [{{ getStatusLabel(issue.incident_status) }}]
            </span>
            <span class="title-text">{{ issue.incident_title }}</span>
            <div class="badge-group">
              <span v-if="issue.incident_severity" class="badge severity" :class="issue.incident_severity.toLowerCase()">
                {{ issue.incident_severity }}
              </span>
              <span v-if="issue.incident_line_name" class="badge display-box" :class="'line-' + issue.incident_line_name">
                {{ issue.incident_line_name }}
              </span>
              <span v-if="issue.incident_station_name" class="badge station">
                {{ issue.incident_station_name }}
              </span>
            </div>
          </div>
          <div class="issue-sub-row">
            # by {{ issue.user_name || 'username' }} writed {{ issue.time_ago || 'recently' }}
          </div>
        </div>

        <div class="comment-count" v-if="issue.comment_cnt > 0">
          <span class="comment-icon">💬</span>
          <span class="count-num">{{ issue.comment_cnt }}</span>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const emit = defineEmits(['go-create', 'change-view']) 

const activeStatus = ref('open')
const searchType = ref('title') 
const searchKeyword = ref('')
const filterSeverity = ref('')
const filterLine = ref('')
const issues = ref([])
const pageNumber = ref(0)
const totalCount = ref(0)

// 인디케이터 스타일 계산
const indicatorStyle = computed(() => {
  return {
    left: activeStatus.value === 'open' ? '0px' : '82px',
    width: activeStatus.value === 'open' ? '68px' : '72px'
  }
})

// 리스트 페이지의 goToDetail 수정
const goToDetail = (id) => {
  console.log("상세 페이지로 이동, ID:", id);
  // 두 번째 인자로 incident_id를 실어서 보냅니다.
  emit('change-view', 'IssueDetail', id); 
}

const getStatusLabel = (status) => {
  const labels = { '1': '대기', '2': '해결중', '3': '완료' };
  return labels[status] || status;
}

const fetchIssues = () => {
  let statusType = activeStatus.value === 'open' ? 'active' : 'resolved';
  const sendData = {
    category: searchType.value,
    keyword: searchKeyword.value,
    pageNumber: pageNumber.value,
    status: statusType
  }
  axios.post('http://localhost:9000/get_incidentlist', sendData)
    .then(resp => {
      issues.value = resp.data.incidentList;
      totalCount.value = resp.data.cnt;
    })
    .catch(err => console.error(err));
}

const onSearch = () => { pageNumber.value = 0; fetchIssues(); }
const goToCreatePage = () => { emit('change-view', 'Createinsident'); }
const changeStatus = (status) => { activeStatus.value = status; pageNumber.value = 0; fetchIssues(); }

onMounted(() => { fetchIssues(); })
</script>

<style scoped>
/* 컨테이너 및 폰트 설정 */
.issue-container { max-width: 95%; margin: 20px auto; font-family: 'Pretendard', sans-serif; color: #24292e; }

/* 검색바 */
.header-section { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.search-wrapper { display: flex; align-items: center; flex: 1; background: #f6f8fa; border-radius: 24px; padding: 8px 20px; border: 1px solid #d1d5da; }
.type-select { background: transparent; border: none; outline: none; font-size: 14px; font-weight: bold; cursor: pointer; }
.divider { width: 1px; height: 18px; background-color: #d1d5da; margin: 0 12px; }
.main-search { flex: 1; background: transparent; border: none; outline: none; font-size: 15px; }
.create-btn { background: #2ea44f; color: white; border: none; padding: 10px 24px; border-radius: 8px; font-weight: bold; cursor: pointer; }

/* 필터 바 (회색 배경 + 애니메이션) */
.filter-bar { 
  display: flex; justify-content: space-between; align-items: center; 
  padding: 0 20px; background-color: #f6f8fa; border: 1px solid #d1d5da; 
  border-top-left-radius: 6px; border-top-right-radius: 6px;
}
.status-tabs-container { display: flex; position: relative; gap: 15px; }
.status-tab { display: flex; align-items: center; gap: 8px; padding: 15px 5px; cursor: pointer; color: #586069; font-size: 14px; transition: color 0.3s; z-index: 1; }
.status-tab.active { color: #24292e; }
.tab-indicator { position: absolute; bottom: 0; height: 2px; background-color: #2ecc71; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.dot { width: 8px; height: 8px; border-radius: 50%; }
.open-dot { background-color: #2ecc71; }
.close-dot { background-color: #e74c3c; }
.sort-group { display: flex; gap: 15px; }
.sort-group select { border: none; outline: none; background: transparent; color: #586069; font-size: 13px; cursor: pointer; }

/* 리스트 스타일 - 박스 전체 클릭 느낌 강조 */
.issue-list { list-style: none; padding: 0; margin: 0; }
.issue-item { 
  display: flex; align-items: center; padding: 14px 20px; background-color: #fff; 
  border: 1px solid #d1d5da; border-top: none; cursor: pointer; /* 커서 손가락 모양 */
  transition: background 0.1s;
}
.issue-item:last-child { border-bottom-left-radius: 6px; border-bottom-right-radius: 6px; }
.issue-item:hover { background-color: #f6f8fa; } /* 호선 시 회색 배경 */

.item-checkbox { margin-right: 15px; display: flex; align-items: center; }
.issue-main { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.issue-title-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.status-text { font-weight: 800; font-size: 15px; white-space: nowrap; }
.status-text.critical { color: #EE0000 !important; }
.status-text.major { color: #F89B00 !important; }
.status-text.minor { color: #17F000 !important; }
.title-text { font-size: 16px; font-weight: bold; color: #24292e; margin-right: 5px; }

/* 배지 */
.badge-group { display: flex; gap: 5px; align-items: center; }
.badge { padding: 2px 10px; border-radius: 12px; font-size: 11px; font-weight: bold; color: white; border: 1px solid rgba(0,0,0,0.05); }
.badge.severity.critical { background-color: #EE0000 !important; }
.badge.severity.major { background-color: #F89B00 !important; }
.badge.severity.minor { background-color: #17F000 !important; }
.badge.station { background-color: #444; }

/* 호선 컬러 */
.display-box.line-1호선 { background-color: #2a317c; }
.display-box.line-2호선 { background-color: #2fae35; }
.display-box.line-3호선 { background-color: #ff6000; }
.display-box.line-4호선 { background-color: #1a97dd; }
.display-box.line-5호선 { background-color: #822fe1; }
.display-box.line-6호선 { background-color: #ae4908; }
.display-box.line-7호선 { background-color: #636b10; }
.display-box.line-8호선 { background-color: #e6265b; }
.display-box.line-9호선 { background-color: #bdb092; }

.issue-sub-row { font-size: 12px; color: #586069; }
.comment-count { display: flex; align-items: center; gap: 5px; color: #586069; margin-left: auto; min-width: 45px; justify-content: flex-end; }
.comment-icon { font-size: 18px; filter: grayscale(1); opacity: 0.5; }
.count-num { font-weight: bold; font-size: 14px; }
.empty { text-align: center; padding: 50px; color: #586069; border: 1px solid #d1d5da; border-top: none; }
</style>