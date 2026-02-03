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

    <div class="filter-bar" :class="{ 'bulk-mode': selectedIds.length > 0 }">
      <div class="filter-left">
        <div class="bulk-check-wrapper">
          <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" class="main-checkbox" />
        </div>

        <div v-if="selectedIds.length === 0" class="status-tabs-container">
          <div class="status-tab" :class="{ active: activeStatus === 'open' }" @click="changeStatus('open')">
            <span class="dot open-dot"></span><strong>Open</strong>
          </div>
          <div class="status-tab" :class="{ active: activeStatus === 'close' }" @click="changeStatus('close')">
            <span class="dot close-dot"></span><strong>Close</strong>
          </div>
          <div class="tab-indicator" :style="indicatorStyle"></div>
        </div>
        
        <div v-else class="bulk-info">
          <span class="selected-count"><strong>{{ selectedIds.length }}</strong> selected</span>
        </div>
      </div>
      
      <div class="sort-group">
        <span v-if="selectedIds.length > 0" class="bulk-label">Edit:</span>

        <select v-model="filterSeverity" 
                @change="selectedIds.length > 0 ? handleBulkUpdate('severity') : handleSortOrFilter()">
          <option value="" v-if="selectedIds.length === 0">심각도 전체</option>
          <option value="" v-else disabled selected>심각도 변경</option>
          <option value="Critical">Critical</option>
          <option value="Major">Major</option>
          <option value="Minor">Minor</option>
        </select>

        <select v-model="filterLine" 
                @change="selectedIds.length > 0 ? handleBulkUpdate('line') : handleSortOrFilter()">
          <option value="" v-if="selectedIds.length === 0">호선 전체</option>
          <option value="" v-else disabled selected>호선 변경</option>
          <option v-for="line in lineOptions" :key="line" :value="line">{{ line }}</option>
        </select>

        <select v-model="filterStation" 
                @change="onStationChange"> <option value="" v-if="selectedIds.length === 0">역 전체</option>
          <option value="" v-else disabled selected>역 변경</option>
          <option v-for="st in stationsInLine" :key="st.station_id" :value="st.station_name">
            {{ st.station_name }}
          </option>
        </select>

        <select v-if="selectedIds.length === 0" v-model="filterSort">
          <option value="desc">Newest</option>
          <option value="asc">Oldest</option>
        </select>
        <button v-else class="cancel-bulk" @click="selectedIds = []">Cancel</button>
      </div>
    </div>

    <ul class="issue-list">
      <li v-if="issues.length === 0 && !isLoading" class="empty">데이터가 없습니다.</li>
      
      <li 
        v-for="issue in issues" 
        :key="issue.incident_id" 
        class="issue-item" 
        :class="{ 'is-selected': selectedIds.includes(issue.incident_id) }"
        @click="goToDetail(issue.incident_id)"
      >
        <div class="item-checkbox" @click.stop> 
          <input type="checkbox" :value="issue.incident_id" v-model="selectedIds" />
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
            # by {{ issue.user_name }} reported {{ issue.create_at }}
          </div>
        </div>

        <div class="comment-count" v-if="issue.comment_cnt > 0">
          <span class="comment-icon">💬</span>
          <span class="count-num">{{ issue.comment_cnt }}</span>
        </div>
      </li>
    </ul>

    <div v-if="isLoading" class="loading-trigger">불러오는 중...</div>
    <div v-if="!hasMore && issues.length > 0" class="end-trigger">마지막 데이터입니다.</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import axios from 'axios'

const emit = defineEmits(['go-create', 'change-view']) 

// --- 상태 및 필터 ---
const activeStatus = ref('open')
const searchType = ref('title') 
const searchKeyword = ref('')
const filterSeverity = ref('')
const filterLine = ref('')
const filterStation = ref('')
const issues = ref([])
const pageNumber = ref(0)
const totalCount = ref(0)
const allStations = ref([])
const lineOptions = ['1호선', '2호선', '3호선', '4호선', '5호선', '6호선', '7호선', '8호선', '9호선']
const filterSort = ref('desc')
const isLoading = ref(false)
const hasMore = ref(true)
const selectedIds = ref([]) 

// --- Computed ---
const isAllSelected = computed(() => {
  return issues.value.length > 0 && selectedIds.value.length === issues.value.length;
});

const indicatorStyle = computed(() => {
  const isOpen = activeStatus.value === 'open';
  return {
    left: isOpen ? '0px' : '82px',
    width: isOpen ? '68px' : '72px',
    backgroundColor: isOpen ? '#2ecc71' : '#e74c3c' 
  }
});

const stationsInLine = computed(() => {
  if (!filterLine.value) return allStations.value;
  return allStations.value.filter(st => st.line_name === filterLine.value);
});

// --- 로직 함수 ---

// [태그: 전체 선택] 현재 페이지의 모든 이슈 ID 선택/해제
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = [];
  } else {
    selectedIds.value = issues.value.map(issue => issue.incident_id);
  }
};

const handleSortOrFilter = () => {
  if (selectedIds.value.length === 0) {
    fetchIssues(true);
  }
};

// [태그: 일괄 수정 실행] 선택된 ID 리스트와 수정 타입/값을 서버 전송 (DB 연동)
const handleBulkUpdate = async (type) => {
  if (selectedIds.value.length === 0) return;

  const value = type === 'severity' ? filterSeverity.value : 
                type === 'line' ? filterLine.value : filterStation.value;
  
  if (!value) return;

  if (confirm(`${selectedIds.value.length}개를 일괄 변경하시겠습니까?`)) {
    try {
      await axios.post('http://localhost:9000/update_incident_statusBatch', {
        incident_id: selectedIds.value, // DB 컬럼명 매칭
        updateType: type,
        updateValue: value
      });
      alert('성공적으로 변경되었습니다.');
      
      selectedIds.value = [];
      if (type === 'severity') filterSeverity.value = '';
      else if (type === 'line') filterLine.value = '';
      else if (type === 'station') filterStation.value = '';
      
      fetchIssues(true);
    } catch (err) {
      console.error("일괄 변경 실패:", err);
      alert('변경 중 오류가 발생했습니다.');
    }
  }
};

// [태그: 역-호선 자동매핑 로직] 역을 선택하면 해당 역의 호선을 자동으로 찾아 필터링
const onStationChange = () => {
  if (filterStation.value) {
    const targetStation = allStations.value.find(st => st.station_name === filterStation.value);
    if (targetStation) {
      filterLine.value = targetStation.line_name;
    }
  }

  if (selectedIds.value.length > 0) {
    handleBulkUpdate('station');
  } else {
    handleSortOrFilter();
  }
};

// [태그: 데이터 조회] Java DTO(Incidentparam) 변수명에 맞춰 데이터 요청
const fetchIssues = async (isNewSearch = false) => {
  if (isLoading.value) return;
  if (isNewSearch) {
    pageNumber.value = 0;
    hasMore.value = true;
    selectedIds.value = [];
  }
  if (!hasMore.value) return;

  isLoading.value = true;
  let statusType = activeStatus.value === 'open' ? 'active' : 'resolved';
  
  const sendData = {
    category: searchType.value,
    keyword: searchKeyword.value,
    pageNumber: pageNumber.value,
    status: statusType,
    incident_severity: filterSeverity.value,   // DB 컬럼명 매칭
    incident_line_name: filterLine.value,     // DB 컬럼명 매칭
    incident_station_name: filterStation.value, // DB 컬럼명 매칭
    sortOrder: filterSort.value
  }

  try {
    const resp = await axios.post('http://localhost:9000/get_incidentlist', sendData);
    const newItems = resp.data.incidentList || [];
    if (pageNumber.value === 0) issues.value = newItems;
    else issues.value = [...issues.value, ...newItems];
    totalCount.value = resp.data.cnt;
    if (newItems.length < 10) hasMore.value = false;
  } catch (err) {
    console.error("데이터 로드 실패:", err);
  } finally {
    isLoading.value = false;
  }
};

const handleScroll = () => {
  const { scrollHeight, scrollTop, clientHeight } = document.documentElement;
  if (scrollTop + clientHeight >= scrollHeight - 50) {
    if (!isLoading.value && hasMore.value) {
      pageNumber.value++;
      fetchIssues();
    }
  }
};

watch(filterSort, () => { if(selectedIds.value.length === 0) fetchIssues(true); });
watch(filterLine, (newLine) => { 
  // 호선이 바뀌었을 때 선택된 역이 해당 호선에 없으면 초기화
  if (filterStation.value) {
    const stillExists = allStations.value.some(st => st.station_name === filterStation.value && st.line_name === newLine);
    if (!stillExists) filterStation.value = '';
  }
  if(selectedIds.value.length === 0) fetchIssues(true); 
});

onMounted(async () => {
  window.addEventListener('scroll', handleScroll);
  const res = await axios.get('http://localhost:9000/get_allstations');
  allStations.value = res.data;
  fetchIssues(true);
});

onUnmounted(() => window.removeEventListener('scroll', handleScroll));

const getStatusLabel = (status) => ({ '1': '대기', '2': '해결중', '3': '완료' }[status] || status);
const onSearch = () => fetchIssues(true);
const changeStatus = (status) => { activeStatus.value = status; fetchIssues(true); };
const goToCreatePage = () => emit('change-view', 'Createinsident');
const goToDetail = (id) => emit('change-view', 'IssueDetail', id);
</script>
<style scoped>
.issue-container { max-width: 95%; margin: 20px auto; font-family: 'Pretendard', sans-serif; }

/* 헤더 */
.header-section { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.search-wrapper { display: flex; align-items: center; flex: 1; background: #f6f8fa; border-radius: 24px; padding: 8px 20px; border: 1px solid #d1d5da; }
.type-select { background: transparent; border: none; outline: none; font-size: 14px; font-weight: bold; cursor: pointer; }
.divider { width: 1px; height: 18px; background-color: #d1d5da; margin: 0 12px; }
.main-search { flex: 1; background: transparent; border: none; outline: none; font-size: 15px; }
.search-icon-btn { background: none; border: none; font-size: 18px; cursor: pointer; margin-right: 10px; }
.create-btn { background: #2ea44f; color: white; border: none; padding: 10px 24px; border-radius: 8px; font-weight: bold; cursor: pointer; }

/* 필터 바 수정 */
.filter-bar { 
  display: flex; justify-content: space-between; align-items: center; 
  padding: 0 20px; background-color: #f6f8fa; border: 1px solid #d1d5da; 
  border-top-left-radius: 6px; border-top-right-radius: 6px;
  min-height: 50px;
}
.filter-bar.bulk-mode { background-color: #fffbdd; }

.filter-left { display: flex; align-items: center; gap: 15px; }
.bulk-check-wrapper { display: flex; align-items: center; padding-right: 5px; }
.main-checkbox { width: 16px; height: 16px; cursor: pointer; }

.status-tabs-container { display: flex; position: relative; gap: 15px; }
.status-tab { display: flex; align-items: center; gap: 8px; padding: 15px 5px; cursor: pointer; color: #586069; font-size: 14px; z-index: 1; }
.status-tab.active { color: #24292e; font-weight: bold; }
.tab-indicator { position: absolute; bottom: 0; height: 3px; transition: all 0.3s ease; }
.dot { width: 8px; height: 8px; border-radius: 50%; }
.open-dot { background-color: #2ecc71; }
.close-dot { background-color: #e74c3c; }

.bulk-info { font-size: 14px; color: #24292e; }

.sort-group { display: flex; align-items: center; gap: 5px; }
.sort-group select { 
  border: 1px solid transparent; outline: none; background: transparent; 
  color: #586069; font-size: 13px; cursor: pointer; padding: 10px 5px;
  max-width: 110px;
}
.bulk-label { font-size: 12px; color: #c69026; font-weight: bold; }
.cancel-bulk { background: #fff; border: 1px solid #d1d5da; padding: 4px 10px; border-radius: 4px; font-size: 12px; cursor: pointer; }

/* 리스트 및 태그 수정 (밀림 방지) */
.issue-list { list-style: none; padding: 0; margin: 0; }
.issue-item { display: flex; align-items: center; padding: 14px 20px; background-color: #fff; border: 1px solid #d1d5da; border-top: none; cursor: pointer; }
.issue-item:hover { background-color: #f6f8fa; }
.issue-item.is-selected { background-color: #fffdef; }

.item-checkbox { margin-right: 15px; display: flex; align-items: center; }
.issue-main { flex: 1; min-width: 0; /* 텍스트 넘침 방지 */ }
.issue-title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; flex-wrap: nowrap; overflow: hidden; }
.title-text { font-size: 16px; font-weight: 600; color: #24292e; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 50%; }

.badge-group { display: flex; gap: 4px; flex-shrink: 0; }
.badge { padding: 2px 8px; border-radius: 12px; font-size: 11px; font-weight: bold; color: white; white-space: nowrap; }

.badge.severity.critical { background-color: #EE0000; }
.badge.severity.major { background-color: #F89B00; }
.badge.severity.minor { background-color: #17F000; }
.badge.station { background-color: #444; }

.display-box.line-1호선 { background-color: #2a317c; } .display-box.line-2호선 { background-color: #2fae35; }
.display-box.line-3호선 { background-color: #ff6000; } .display-box.line-4호선 { background-color: #1a97dd; }
.display-box.line-5호선 { background-color: #822fe1; } .display-box.line-6호선 { background-color: #ae4908; }
.display-box.line-7호선 { background-color: #636b10; } .display-box.line-8호선 { background-color: #e6265b; }
.display-box.line-9호선 { background-color: #bdb092; }

.issue-sub-row { font-size: 12px; color: #586069; }
.comment-count { display: flex; align-items: center; gap: 4px; color: #586069; margin-left: 10px; flex-shrink: 0; }
.status-text { font-weight: 800; font-size: 13px; flex-shrink: 0; }
.status-text.critical { color: #EE0000; }
.status-text.major { color: #F89B00; }
.status-text.minor { color: #17F000; }

.empty { text-align: center; padding: 50px; color: #586069; border: 1px solid #d1d5da; border-top: none; }
.loading-trigger, .end-trigger { text-align: center; padding: 20px; font-size: 14px; color: #586069; }
</style>