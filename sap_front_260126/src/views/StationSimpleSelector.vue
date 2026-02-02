<template>
  <div class="simple-selector" ref="selectorContainer">
    <div class="filter-item">
      <label class="section-label">역 검색</label>
      <div class="input-relative">
        <input 
          type="text" 
          :value="tempFilters.stationSearch"
          placeholder="역명을 입력하세요 (예: 강남)" 
          class="main-search-input" 
          @input="onInputSync" 
          @focus="showResults = true"
        >
        <ul v-if="showResults && tempFilters.stationSearch.trim() && searchPreviewList.length > 0" class="auto-complete-list">
          <li v-for="st in searchPreviewList" :key="st.station_id + st.line_name" @mousedown="selectStation(st)">
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
          :class="['line-btn', { active: tempFilters.line === line }]"
          :style="tempFilters.line === line ? { backgroundColor: getLineColor(line), borderColor: getLineColor(line), color: '#fff' } : {}"
          @click="handleLineClick(line)"
        >
          {{ line.replace('호선','') }}
        </button>
      </div>
    </div>

    <div class="filter-item station-list-box" v-if="tempFilters.line">
      <label class="section-label">{{ tempFilters.line }} 역 목록</label>
      <div class="station-button-grid">
        <button 
          v-for="st in stationsInSelectedLine" 
          :key="st.station_id"
          type="button"
          :class="['st-select-btn', { active: tempFilters.station_id === st.station_id }]"
          @click="selectStation(st)"
        >
          {{ st.station_name }}
        </button>
      </div>
    </div>

    <div class="button-group">
      <button class="confirm-btn" @click="confirmSelection" :disabled="!isReady">
        <span class="btn-text">위치 확정</span>
      </button>
      <button class="reset-btn" @click="resetLocalFilters">초기화</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import axios from 'axios';

const emit = defineEmits(['selected']);
const selectorContainer = ref(null);
const allStationData = ref([]);
const showResults = ref(false);

const lineOptions = ['1호선', '2호선', '3호선', '4호선', '5호선', '6호선', '7호선', '8호선', '9호선'];
const tempFilters = reactive({
  line: '',
  stationSearch: '',
  station_id: '',
  station_name: '' 
});

// 호선만 있어도 OK, 역까지 있으면 더 OK
const isReady = computed(() => tempFilters.line !== '');

const onInputSync = (e) => {
  tempFilters.stationSearch = e.target.value;
  showResults.value = true;
  tempFilters.line = ''; 
  tempFilters.station_id = '';
  tempFilters.station_name = '';
};

// 역 선택 시 (내부 데이터만 업데이트하고 메뉴는 닫지 않음)
const selectStation = (st) => {
  tempFilters.stationSearch = st.station_name;
  tempFilters.station_name = st.station_name;
  tempFilters.line = st.line_name;
  tempFilters.station_id = st.station_id;
  showResults.value = false; // 자동완성 리스트만 닫음
  // sendToParent(); <--- 이 부분을 지웠습니다!
};

// 호선 클릭 시 (내부 데이터만 업데이트)
const handleLineClick = (line) => {
  showResults.value = false;
  if (tempFilters.line === line) {
    tempFilters.line = '';
    tempFilters.station_id = '';
    tempFilters.station_name = '';
  } else {
    tempFilters.line = line;
    tempFilters.stationSearch = ''; 
    tempFilters.station_id = '';    
    tempFilters.station_name = '';
  }
  // sendToParent(); <--- 이 부분도 지웠습니다!
};

const resetLocalFilters = () => {
  tempFilters.line = '';
  tempFilters.stationSearch = '';
  tempFilters.station_id = '';
  tempFilters.station_name = '';
  showResults.value = false;
};

// 오직 이 함수가 호출될 때만 부모에게 데이터가 날아갑니다!
const confirmSelection = () => {
  emit('selected', {
    line_name: tempFilters.line,
    station_name: tempFilters.station_name || '', 
    station_id: tempFilters.station_id || ''      
  });
};
const getLineColor = (line) => {
  const colors = { 
    '1호선': '#0052A4', '2호선': '#00A84D', '3호선': '#EF7C1C', '4호선': '#00A5DE', 
    '5호선': '#996CAC', '6호선': '#CD7C2F', '7호선': '#747F28', '8호선': '#E6186C', '9호선': '#BDB092' 
  };
  return colors[line] || '#333';
};

const stationsInSelectedLine = computed(() => {
  if (!tempFilters.line) return [];
  return allStationData.value.filter(s => s.line_name === tempFilters.line);
});

onMounted(async () => {
  try {
    const res = await axios.get('http://localhost:9000/get_allstations');
    allStationData.value = res.data;
  } catch (err) { console.error(err); }
  
  window.addEventListener('mousedown', (e) => {
    if (selectorContainer.value && !selectorContainer.value.contains(e.target)) showResults.value = false;
  });
});
</script>

<style scoped>
/* 스타일은 그대로 유지 */
.simple-selector { padding: 18px; background: #fff; width: 310px; display: flex; flex-direction: column; gap: 15px; }
.input-relative { position: relative; }
.main-search-input { width: 100%; padding: 12px; border: 2px solid #f0f0f0; border-radius: 8px; outline: none; box-sizing: border-box; }
.auto-complete-list { position: absolute; top: 100%; left: 0; width: 100%; background: #fff; border: 1px solid #ddd; z-index: 9999; max-height: 200px; overflow-y: auto; padding: 0; margin-top: 5px; list-style: none; box-shadow: 0 10px 20px rgba(0,0,0,0.1); }
.auto-complete-list li { padding: 12px 15px; cursor: pointer; display: flex; justify-content: space-between; border-bottom: 1px solid #f9f9f9; }
.line-button-group { display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; }
.line-btn { padding: 10px 0; border: 1px solid #eee; background: #fff; border-radius: 6px; cursor: pointer; font-size: 11px; font-weight: bold; }
.station-button-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 4px; max-height: 150px; overflow-y: auto; padding: 8px; background: #fcfcfc; border: 1px solid #eee; border-radius: 8px; }
.st-select-btn { padding: 8px 4px; border: 1px solid #eee; border-radius: 4px; font-size: 11px; }
.st-select-btn.active { background: #333; color: #fff; }
.button-group { display: flex; gap: 8px; margin-top: 10px; }
.confirm-btn { flex: 1; height: 48px; background: #2ecc71; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: bold; }
</style>