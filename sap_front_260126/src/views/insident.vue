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

      <button class="search-icon-btn" @click="onSearch"><img src="@/assets/검색돋보기(B).png" alt="search" style="width: 20px; height: 20px; vertical-align: middle;"></button>
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
        <template v-if="selectedIds.length > 0">
          <span class="bulk-label">Edit Mode:</span>
          
          <div class="bulk-menu-item">
            <button class="bulk-toggle-btn" @click.stop="toggleBulkMenu('status')">상태 ▾</button>
            <div v-if="activeBulkMenu === 'status'" class="bulk-dropdown">
              <div v-for="(label, val) in statusMap" :key="val" class="menu-item" @click="handleBulkUpdate('status', val)">
                {{ label }}
              </div>
            </div>
          </div>

          <div class="bulk-menu-item">
            <button class="bulk-toggle-btn" @click.stop="toggleBulkMenu('severity')">심각도 ▾</button>
            <div v-if="activeBulkMenu === 'severity'" class="bulk-dropdown">
              <div v-for="lv in ['Critical', 'Major', 'Minor']" :key="lv" class="menu-item" @click="handleBulkUpdate('severity', lv)">
                {{ lv }}
              </div>
            </div>
          </div>

          <div class="bulk-menu-item">
            <button class="bulk-toggle-btn" @click.stop="toggleBulkMenu('location')">위치 ▾</button>
            <div v-if="activeBulkMenu === 'location'" class="bulk-dropdown location-picker">
              <StationSimpleSelector @selected="onBulkStationSelected" />
            </div>
          </div>

          <button class="cancel-bulk" @click="clearSelection">Cancel</button>
        </template>

        <template v-else>
          <select v-model="filterSeverity" @change="fetchIssues(true)">
            <option value="">심각도 전체</option>
            <option value="Critical">Critical</option>
            <option value="Major">Major</option>
            <option value="Minor">Minor</option>
          </select>
          <select v-model="filterLine" @change="fetchIssues(true)">
            <option value="">호선 전체</option>
            <option v-for="line in lineOptions" :key="line" :value="line">{{ line }}</option>
          </select>
          <select v-model="filterStation" @change="onStationChange">
            <option value="">역 전체</option>
            <option v-for="st in stationsInFilterLine" :key="st.station_id" :value="st.station_name">{{ st.station_name }}</option>
          </select>
          <select v-model="filterSort" @change="fetchIssues(true)">
             <option value="">정렬</option>
            <option value="desc">Newest</option>
            <option value="asc">Oldest</option>
          </select>
         <button type="button" class="reset-btn" @click="resetFilter">정렬리셋</button>
        </template>
      </div>
    </div>

    <ul class="issue-list">
      <li v-if="issues.length === 0 && !isLoading" class="empty">데이터가 없습니다.</li>
      <li v-for="issue in issues" :key="issue.incident_id" 
          class="issue-item" 
          :class="{ 'is-selected': selectedIds.includes(issue.incident_id) }" 
          @click="goToDetail(issue.incident_id)">
        <div class="item-checkbox" @click.stop>
          <input type="checkbox" :value="issue.incident_id" v-model="selectedIds" />
        </div>
        <div class="issue-main">
          <div class="issue-title-row">
            <span class="status-text" :style="{ color: getStatusConfig(issue.incident_status).color }">
              [{{ getStatusConfig(issue.incident_status).label }}]
            </span>
            <span class="title-text">{{ issue.incident_title }}</span>
            <div class="badge-group">
              <span v-if="issue.incident_severity" class="badge severity" :class="issue.incident_severity.toLowerCase()">{{ issue.incident_severity }}</span>
              <span v-if="issue.incident_line_name" class="badge display-box" :class="'line-' + issue.incident_line_name.replace(' ', '')">{{ issue.incident_line_name }}</span>
              <span v-if="issue.incident_station_name" class="badge station">{{ issue.incident_station_name }}</span>
            </div>
          </div>
       <div class="issue-sub-row">
  <span class="reporter-info"># by {{ issue.user_name }}</span>
  <span class="date-info">
    reported {{ formatDate(issue.create_at) }}
    <template v-if="issue.complete_at">
      <span class="complete-separator"> | </span>
      <span class="completed-text">✅ completed {{ formatDate(issue.complete_at) }}</span>
    </template>
  </span>
</div>
        </div>
        <div class="comment-count" v-if="issue.comment_cnt > 0">
          <span class="comment-icon"><img src="@/assets/말풍선.png" alt="search" style="width: 20px; height: 20px; vertical-align: middle;"></span><span class="count-num">{{ issue.comment_cnt }}</span>
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
import StationSimpleSelector from './StationSimpleSelector.vue';

const emit = defineEmits(['go-create', 'change-view']) 
// 날짜 형식을 '26.02.05 12:30' 형태로 변환
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return new Intl.DateTimeFormat('ko-KR', {
    year: '2-digit',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(date);
};
// --- 상태 관리 ---
const activeBulkMenu = ref(null); 
const statusMap = { '1': '대기', '2': '해결중', '3': '완료', '4': '삭제' };

const filterSeverity = ref('')
const filterLine = ref('')
const filterStation = ref('')
const filterSort = ref('')

const activeStatus = ref('open')
const searchType = ref('title') 
const searchKeyword = ref('')
const issues = ref([])
const pageNumber = ref(0)
const totalCount = ref(0)
const allStations = ref([])
const lineOptions = ['1호선', '2호선', '3호선', '4호선', '5호선', '6호선', '7호선', '8호선', '9호선']
const isLoading = ref(false)
const hasMore = ref(true)
const selectedIds = ref([]) 

// --- Computed ---
const isAllSelected = computed(() => issues.value.length > 0 && selectedIds.value.length === issues.value.length)
const indicatorStyle = computed(() => {
  const isOpen = activeStatus.value === 'open'
  return { left: isOpen ? '0px' : '82px', width: isOpen ? '68px' : '72px', backgroundColor: isOpen ? '#2ecc71' : '#e74c3c' }
})

const onBulkStationSelected = async (stationData) => {
  // stationData 예시: { station_name: '강남', line_name: '2호선' }
  if (!stationData || selectedIds.value.length === 0) return;

  const confirmMsg = `${selectedIds.value.length}개의 이슈 위치를 [${stationData.line_name} ${stationData.station_name}]으로 일괄 변경하시겠습니까?`;

  if (confirm(confirmMsg)) {
    try {
      const payload = {
        incident_id: selectedIds.value,
        updateType: 'location',
        updateValue: stationData.station_name, // 역 이름
        lineName: stationData.line_name,       // 서버 Mybatis에서 사용할 호선 정보
        station_id: stationData.station_id
      
      };

      await axios.post('http://localhost:9000/update_incident_statusBatch', payload);
      
      alert('위치 정보가 성공적으로 변경되었습니다.');
      clearSelection();
      fetchIssues(true);
    } catch (err) {
      console.error(err);
      alert('위치 변경 중 오류가 발생했습니다.');
    }
  }
};
const stationsInFilterLine = computed(() => {
let list = !filterLine.value 
    ? allStations.value 
    : allStations.value.filter(st => st.line_name === filterLine.value);

  // 2. 중복 제거 로직 추가 (역 이름 기준)
  const seen = new Set();
  const uniqueList = list.filter(st => {
    if (seen.has(st.station_name)) {
      return false; // 이미 있는 이름이면 제외
    }
    seen.add(st.station_name);
    return true;
  });

  // 3. 한글 가나다순 정렬
  return [...uniqueList].sort((a, b) => a.station_name.localeCompare(b.station_name, 'ko'));
});
// --- Logic ---
const toggleBulkMenu = (type) => {
  activeBulkMenu.value = activeBulkMenu.value === type ? null : type;
};

const clearSelection = () => { 
  selectedIds.value = []; 
  activeBulkMenu.value = null;
}

const toggleSelectAll = () => { 
  selectedIds.value = isAllSelected.value ? [] : issues.value.map(i => i.incident_id); 
}
const resetFilter = () => {
  // 1. 모든 필터 변수 초기화
  searchKeyword.value = '';
  searchType.value = 'title';
  filterSeverity.value = '';
  filterLine.value = '';
  filterStation.value = '';
  filterSort.value = '';
  // 2. 일괄 선택 상태도 초기화
  clearSelection();
  // 3. 서버에서 데이터 다시 불러오기
  fetchIssues(true);
};
const getStatusConfig = (status) => {
  const configs = { '1': { label: '대기', color: '#9e9e9e' }, '2': { label: '해결중', color: '#0e6ca5' }, '3': { label: '완료', color: '#17f000' }, '4': { label: '삭제', color: '#EEEEEE' } }
  return configs[status] || { label: status, color: '#333' }
}

// 일괄 업데이트 함수
// handleBulkUpdate 함수 수정
const handleBulkUpdate = async (type, value) => {
  if (selectedIds.value.length === 0 || !value) return;
  
  const confirmMsg = type === 'status' && value === '3' 
    ? `${selectedIds.value.length}개를 '완료' 처리하시겠습니까? (완료 시간이 기록됩니다)` 
    : `${selectedIds.value.length}개를 일괄 변경하시겠습니까?`;

  if (confirm(confirmMsg)) {
    try {
      // 보낼 데이터 객체 생성
      const payload = {
        incident_id: selectedIds.value,
        updateType: type, 
        updateValue: value 
      };

      // ★ 상태 변경이면서 값이 '3'(완료)일 때 status 파라미터를 명시적으로 추가
      if (type === 'status' && value === '3') {
        payload.status = '3';
      }

      await axios.post('http://localhost:9000/update_incident_statusBatch', payload);
      
      alert('성공적으로 변경되었습니다.'); 
      clearSelection();
      fetchIssues(true);
    } catch (err) { 
      alert('변경 중 오류가 발생했습니다.'); 
    }
  }
}

const handleClickOutside = (e) => {
  if (!e.target.closest('.bulk-menu-item')) {
    activeBulkMenu.value = null;
  }
};

const onStationChange = () => {

  fetchIssues(true);
}

const fetchIssues = async (isNewSearch = false) => {
  if (isLoading.value) return;
  if (isNewSearch) { pageNumber.value = 0; hasMore.value = true; selectedIds.value = []; }
  if (!hasMore.value) return;

  isLoading.value = true;
  const statusType = activeStatus.value === 'open' ? 'active' : 'resolved';
  
  const sendData = {
    category: searchType.value,
    keyword: searchKeyword.value,
    pageNumber: pageNumber.value,
    status: statusType,
    sortOrder: filterSort.value,
    severity: filterSeverity.value,
    line_name: filterLine.value,
    station_name: filterStation.value
  };

  try {
    const resp = await axios.post('http://localhost:9000/get_incidentlist', sendData);
    const newItems = resp.data.incidentList || [];
    if (pageNumber.value === 0) issues.value = newItems;
    else issues.value = [...issues.value, ...newItems];
    totalCount.value = resp.data.cnt;
    if (newItems.length < 10) hasMore.value = false;
  } catch (err) { console.error("데이터 로드 실패:", err); }
  finally { isLoading.value = false; }
}

const handleScroll = () => {
  const { scrollHeight, scrollTop, clientHeight } = document.documentElement;
 if (Math.ceil(scrollTop + clientHeight) >= scrollHeight - 100) {
    if (!isLoading.value && hasMore.value) {
      pageNumber.value++; 
      fetchIssues();
    }
  }
}

const onSearch = () => fetchIssues(true);
const changeStatus = (status) => { activeStatus.value = status; };
const goToCreatePage = () => emit('change-view', 'Createinsident');
const goToDetail = (id) => emit('change-view', 'IssueDetail', id);

watch(activeStatus, () => fetchIssues(true));
watch(filterLine, (newLine) => { 
  if (filterStation.value) {
    const exists = allStations.value.some(st => st.station_name === filterStation.value && st.line_name === newLine);
    if (!exists) filterStation.value = '';
  }
  fetchIssues(true); 
});

onMounted(async () => {
  window.addEventListener('mousedown', handleClickOutside)
  window.addEventListener('scroll', handleScroll);
  const res = await axios.get('http://localhost:9000/get_allstations');
  allStations.value = res.data;
  fetchIssues(true);
});

onUnmounted(() => {
  window.removeEventListener('mousedown', handleClickOutside);
  window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
.issue-container { max-width: 95%; margin: 20px auto; font-family: 'Pretendard', sans-serif; }
.header-section { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.search-wrapper { display: flex; align-items: center; flex: 1; background: #f6f8fa; border-radius: 24px; padding: 8px 20px; border: 1px solid #d1d5da; }
.type-select { background: transparent; border: none; outline: none; font-size: 14px; font-weight: bold; cursor: pointer; }
.divider { width: 1px; height: 18px; background-color: #d1d5da; margin: 0 12px; }
.main-search { flex: 1; background: transparent; border: none; outline: none; font-size: 15px; }
.search-icon-btn { background: none; border: none; font-size: 18px; cursor: pointer; margin-right: 10px; }
.create-btn { background: #2ea44f; color: white; border: none; padding: 10px 24px; border-radius: 8px; font-weight: bold; cursor: pointer; }
.filter-bar { display: flex; justify-content: space-between; align-items: center; padding: 0 20px; background-color: #f6f8fa; border: 1px solid #d1d5da; border-top-left-radius: 6px; border-top-right-radius: 6px; min-height: 50px; }
.filter-bar.bulk-mode { background-color: #fffbdd; }
.filter-left { display: flex; align-items: center; gap: 15px; }
.bulk-check-wrapper { display: flex; align-items: center; padding-right: 5px; }
.main-checkbox { width: 16px; height: 50px; cursor: pointer; }
.status-tabs-container { display: flex; position: relative; gap: 15px; }
.status-tab { display: flex; align-items: center; gap: 8px; padding: 15px 5px; cursor: pointer; color: #586069; font-size: 14px; z-index: 1; }
.status-tab.active { color: #24292e; font-weight: bold; }
.tab-indicator { position: absolute; bottom: 0; height: 3px; transition: all 0.3s ease; }
.dot { width: 8px; height: 8px; border-radius: 50%; }
.open-dot { background-color: #2ecc71; }
.close-dot { background-color: #e74c3c; }
.sort-group { display: flex; align-items: center; gap: 5px; }
.sort-group select { border: 1px solid transparent; outline: none; background: transparent; color: #586069; font-size: 13px; cursor: pointer; padding: 10px 5px; max-width: 110px; }
.bulk-label { font-size: 12px; color: #c69026; font-weight: bold; }
.cancel-bulk { background: #fff; border: 1px solid #d1d5da; padding: 4px 10px; border-radius: 4px; font-size: 12px; cursor: pointer; }
.issue-list { list-style: none; padding: 0; margin: 0; }
.issue-item { display: flex; align-items: center; padding: 14px 20px; background-color: #fff; border: 1px solid #d1d5da; border-top: none; cursor: pointer; }
.issue-item:hover { background-color: #f6f8fa; }
.issue-item.is-selected { background-color: #fffdef; }
.item-checkbox { margin-right: 15px; display: flex; transform: scale(1.2); align-items: center; }
.issue-main { flex: 1; min-width: 0; }
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
.empty { text-align: center; padding: 50px; color: #586069; border: 1px solid #d1d5da; border-top: none; }
.loading-trigger, .end-trigger { text-align: center; padding: 20px; font-size: 14px; color: #586069; }

/* 일괄 수정 메뉴 스타일 */
.bulk-menu-item { position: relative; display: inline-block; }

.bulk-toggle-btn {
  background: #fff;
  border: 1px solid #d1d5da;
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.bulk-toggle-btn:hover { background: #f6f8fa; }

.bulk-dropdown {
  position: absolute;
  top: calc(100% + 5px);
  left: 0;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  z-index: 1001;
  min-width: 120px;
}

/* 위치 선택기는 창이 넓으므로 오른쪽 정렬 */
.bulk-dropdown.location-picker {
  left: auto;
  right: 0;
}

.menu-item {
  padding: 10px 15px;
  font-size: 13px;
  cursor: pointer;
  border-bottom: 1px solid #f1f1f1;
  text-align: left;
}
.menu-item:hover { background: #f0f7ff; color: #0969da; }
.menu-item:last-child { border-bottom: none; }

/* 기존 필터바와 간격 맞춤 */
.sort-group { display: flex; align-items: center; gap: 8px; }


.reset-btn {
  background: #fff;
  border: 1px solid #d1d5da;
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #586069;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: 0.2s;
}

.reset-btn:hover {
  background-color: #f6f8fa;
  color: #24292e;
  border-color: #1b1f23;
}

/* 일괄 수정 모드일 때는 리셋 버튼숨기기 */
.bulk-mode .reset-btn {
display: none; 
}
</style>