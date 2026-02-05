<template>
  <div class="simple-selector" ref="selectorContainer">
    <div class="filter-item">
      <label class="section-label">역 검색</label>
      <div class="input-relative">
        <input 
          type="text" 
          v-model="tempFilters.stationSearch"
          placeholder="역명을 입력하세요 (예: 강남)" 
          class="main-search-input" 
          @focus="showResults = true"
          @input="onSearchInput"
        >
        <ul v-if="showResults && tempFilters.stationSearch.trim() && searchPreviewList.length > 0" class="auto-complete-list">
          <li 
            v-for="st in searchPreviewList" 
            :key="st.station_id + st.line_name" 
            @click="applyPreviewSelection(st)"
          >
            <span class="st-name">{{ st.station_name }}</span>
            <span class="st-line" :style="{ color: getLineColor(st.line_name), backgroundColor: getLineColor(st.line_name) + '15' }">
              {{ st.line_name }}
            </span>
          </li>
        </ul>
      </div>
    </div>

    <div class="filter-item">
      <label class="section-label">호선 선택</label>
      <div class="line-button-group">
        <button 
          v-for="line in lineOptions" :key="line"
          type="button"
          :class="['line-btn', { active: tempFilters.line === line }] "
          :style="tempFilters.line === line ? { backgroundColor: getLineColor(line), borderColor: getLineColor(line), color: '#fff' } : {}"
          @click="handleLineClick(line)"
        >
          {{ line.replace('호선','') }}
        </button>
      </div>
    </div>

    <div class="filter-item station-list-box">
      <label class="section-label">
        {{ tempFilters.line ? tempFilters.line + ' 결과' : '역 목록' }} ({{ filteredStations.length }})
      </label>
      <div class="station-button-grid">
        <button 
          v-for="st in filteredStations" 
          :key="st.station_id + st.line_name"
          type="button"
          :class="['st-select-btn', { active: String(tempFilters.station_id) === String(st.station_id) }] "
          @click="manualSelectStation(st)"
        >
          {{ st.station_name }}
        </button>
        <div v-if="filteredStations.length === 0" class="no-result">결과가 없습니다.</div>
      </div>
    </div>

    <div class="button-group">
      <button class="confirm-btn" @click="emitFinalSelection">위치 확정</button>
      <button class="reset-btn" @click="resetAll">초기화</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import axios from 'axios';

const emit = defineEmits(['selected']);
const selectorContainer = ref(null);
const allStations = ref([]);
const showResults = ref(false);

const lineOptions = ['1호선', '2호선', '3호선', '4호선', '5호선', '6호선', '7호선', '8호선', '9호선'];

const tempFilters = reactive({
  line: '',           
  stationSearch: '',  
  station_id: '',     
  station_name: ''    
});

const onSearchInput = () => {
  showResults.value = true;
  tempFilters.station_id = '';
  tempFilters.station_name = '';
};

const applyPreviewSelection = (st) => {
  tempFilters.stationSearch = st.station_name; 
  tempFilters.station_id = st.station_id;     
  tempFilters.station_name = st.station_name; 
  showResults.value = false;
};

const manualSelectStation = (st) => {
  tempFilters.stationSearch = st.station_name;
  tempFilters.station_id = st.station_id;
  tempFilters.station_name = st.station_name;
};

const handleLineClick = (line) => {
  tempFilters.line = (tempFilters.line === line) ? '' : line;
  showResults.value = false;
  tempFilters.station_id = '';
  tempFilters.station_name = '';
};

const emitFinalSelection = () => {
  const finalData = {
    line_name: tempFilters.line || '', 
    station_name: tempFilters.station_name || '',
    station_id: tempFilters.station_id || ''
  };
  emit('selected', finalData);
};

const filteredStations = computed(() => {
  if (!allStations.value.length) return [];
  
  let list = [...allStations.value];
  const q = tempFilters.stationSearch.trim();

  // 1. 이름 검색 적용
  if (q) {
    list = list.filter(s => s.station_name.includes(q));
  }

  // 2. 호선 필터 적용 (호선을 선택했을 때는 해당 호선 역만 나와야 함)
  if (tempFilters.line) {
    list = list.filter(s => s.line_name === tempFilters.line);
  }

  // ★ 3. [핵심] 이름 중복 제거 로직 추가
  // 호선 선택이 안 된 상태(전체 검색)일 때, 이름이 같은 역은 하나만 남깁니다.
  if (!tempFilters.line) {
    const seen = new Set();
    list = list.filter(s => {
      const isDuplicate = seen.has(s.station_name);
      seen.add(s.station_name);
      return !isDuplicate; // 처음 보는 이름만 남김
    });
  }

  // 4. 선택된 역이 목록에 없으면 추가
  if (tempFilters.station_id && !list.some(s => s.station_id === tempFilters.station_id)) {
    const selected = allStations.value.find(s => s.station_id === tempFilters.station_id);
    if (selected) list.unshift(selected);
  }

  return list.sort((a, b) => a.station_name.localeCompare(b.station_name, 'ko'));
});

const searchPreviewList = computed(() => {
  const q = tempFilters.stationSearch.trim();
  
  // 검색어가 없거나 이미 선택된 역이면 리스트 숨김
  if (!q || q === tempFilters.station_name) return [];

  // 1. 먼저 검색어로 필터링
  let list = allStations.value.filter(s => s.station_name.includes(q));

  // 2. 호선 선택이 안 된 상태라면 이름 중복 제거
  if (!tempFilters.line) {
    const seen = new Set();
    list = list.filter(s => {
      const isDuplicate = seen.has(s.station_name);
      seen.add(s.station_name);
      return !isDuplicate;
    });
  }

  // 3. 최대 10개만 반환
  return list.slice(0, 10);
});
const resetAll = () => {
  tempFilters.line = '';
  tempFilters.stationSearch = '';
  tempFilters.station_id = '';
  tempFilters.station_name = '';
  showResults.value = false;
};

const getLineColor = (line) => {
  const colors = { '1호선': '#0052A4', '2호선': '#00A84D', '3호선': '#EF7C1C', '4호선': '#00A5DE', '5호선': '#996CAC', '6호선': '#CD7C2F', '7호선': '#747F28', '8호선': '#E6186C', '9호선': '#BDB092' };
  return colors[line] || '#333';
};

const clickOutside = (e) => {
  if (selectorContainer.value && !selectorContainer.value.contains(e.target)) {
    showResults.value = false;
  }
};

onMounted(async () => {
  try {
    const res = await axios.get('http://localhost:9000/get_allstations');
    allStations.value = res.data;
  } catch (e) { console.error(e); }
  window.addEventListener('mousedown', clickOutside);
});
onUnmounted(() => window.removeEventListener('mousedown', clickOutside));
</script>

<style scoped>
.simple-selector {
  padding: 18px;
  background: #fff;
  width: 100%;
  max-width: 320px;
  min-width: 280px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  box-sizing: border-box;
  overflow-x: hidden;
}

.input-relative {
  position: relative;
  width: 100%;
}

.main-search-input {
  width: 100%;
  padding: 12px;
  border: 2px solid #f0f0f0;
  border-radius: 8px;
  outline: none;
  box-sizing: border-box;
  transition: 0.2s;
  font-size: 14px;
}

.auto-complete-list {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: #fff;
  border: 1px solid #ddd;
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
  padding: 0;
  margin-top: 5px;
  list-style: none;
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  border-radius: 8px;
}

.auto-complete-list li {
  padding: 12px 15px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #f9f9f9;
  font-size: 13px;
}

.line-button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.line-btn {
  flex: 1 1 30%;
  padding: 10px 0;
  border: 1px solid #eee;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 11px;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.station-button-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
  max-height: 180px;
  overflow-y: auto;
  padding: 8px;
  background: #fcfcfc;
  border: 1px solid #eee;
  border-radius: 8px;
  box-sizing: border-box;
}

.st-select-btn {
  padding: 8px 4px;
  border: 1px solid #eee;
  border-radius: 4px;
  font-size: 11px;
  background: #fff;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.st-select-btn.active {
  background: #333 !important;
  color: #fff !important;
  border-color: #333;
  font-weight: bold;
}

.no-result {
  grid-column: span 3;
  text-align: center;
  padding: 20px;
  font-size: 12px;
  color: #999;
}

.button-group {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.confirm-btn {
  flex: 2;
  height: 48px;
  background: #2ecc71;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
}

.reset-btn {
  flex: 1;
  background: #f0f0f0;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
}

.section-label {
  font-size: 13px;
  font-weight: bold;
  margin-bottom: 8px;
  display: block;
  color: #555;
}
</style>
