<template>
  <div class="search-container" ref="searchContainer">
    <div class="filter-box">
      <div class="filter-item line-section">
        <label class="filter-label">호선 선택</label>
        <div class="line-button-group">
          <button 
            v-for="line in lineOptions" :key="line"
            type="button"
            :class="['line-btn', { active: filters.line === line }]"
            :style="filters.line === line ? { backgroundColor: getLineColor(line), borderColor: getLineColor(line), color: '#fff' } : {}"
            @click="handleLineClick(line)"
          >
            {{ line }}
          </button>
        </div>
      </div>

      <div class="filter-item search-input-wrapper">
        <label class="filter-label">역 검색</label>
        <div class="search-flex-row">
          <div class="input-relative">
            <input 
              ref="stationInput"
              type="text" 
              :value="filters.stationSearch" 
              placeholder="역명 입력 (예: 약수)" 
              @input="onInput"
              @focus="onFocus"
              @keyup.enter="executeSearch"
            >
            <ul v-if="showResults && filteredStations.length > 0" class="auto-complete-list">
              <li v-for="st in filteredStations" :key="st.station_id + st.line_name" @click="selectStation(st)">
                <span class="st-name">{{ st.station_name }}</span>
                <span class="st-line" :style="{ color: getLineColor(st.line_name), backgroundColor: getLineColor(st.line_name) + '15' }">
                  {{ st.line_name }}
                </span>
              </li>
            </ul>
          </div>

          <div class="button-group">
            <button class="icon-search-btn" @click="executeSearch" title="현황 조회">
              <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </button>
            <button class="icon-reset-btn" @click="resetFilters" title="검색 초기화">
              <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#333" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M23 4v6h-6"></path>
                <path d="M1 20v-6h6"></path>
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, defineEmits, onMounted, onUnmounted } from 'vue';
import axios from 'axios';

const emit = defineEmits(['search']);
const lineOptions = ['1호선', '2호선', '3호선', '4호선', '5호선', '6호선', '7호선', '8호선', '9호선'];
const searchContainer = ref(null);

const getLineColor = (line) => {
  const colors = {
    '1호선': '#0052A4', '2호선': '#00A84D', '3호선': '#EF7C1C', '4호선': '#00A5DE',
    '5호선': '#996CAC', '6호선': '#CD7C2F', '7호선': '#747F28', '8호선': '#E6186C', '9호선': '#BDB092'
  };
  return colors[line] || '#007bff';
};

const allStationData = ref([]);
const filters = reactive({ line: '', stationSearch: '' });
const showResults = ref(false);
const selectedStation = ref(null);

const fetchAllStations = async () => {
  try {
    const res = await axios.get('http://localhost:9000/get_allstations');
    allStationData.value = res.data;
  } catch (err) { console.error(err); }
};

const handleClickOutside = (event) => {
  if (searchContainer.value && !searchContainer.value.contains(event.target)) {
    showResults.value = false;
  }
};

onMounted(() => {
  fetchAllStations();
  window.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  window.removeEventListener('click', handleClickOutside);
});

const filteredStations = computed(() => {
  const keyword = filters.stationSearch.trim();
  if (!keyword && !filters.line) return [];

  let result = [];
  if (filters.line) {
    result = allStationData.value.filter(s => s.line_name === filters.line && (keyword === '' || s.station_name.includes(keyword)));
  } else {
    result = allStationData.value.filter(s => s.station_name.includes(keyword));
  }

  return result.sort((a, b) => {
    // 1. 검색어와 정확히 일치하는 역을 최상단으로 (예: "동대문" 검색 시 "동대문" 우선)
    const exactA = a.station_name === keyword ? 0 : 1;
    const exactB = b.station_name === keyword ? 0 : 1;
    if (exactA !== exactB) return exactA - exactB;

    // 2. 검색어로 시작하는 역을 다음 우선순위로 (예: "동대문역" > "신동대문")
    const startsA = a.station_name.startsWith(keyword) ? 0 : 1;
    const startsB = b.station_name.startsWith(keyword) ? 0 : 1;
    if (startsA !== startsB) return startsA - startsB;

    // 3. 그 다음은 글자 길이순 (짧은 역명이 위로)
    if (a.station_name.length !== b.station_name.length) {
      return a.station_name.length - b.station_name.length;
    }

    // 4. 마지막으로 가나다순
    return a.station_name.localeCompare(b.station_name);
  });
});

const handleLineClick = (line) => {
  if (filters.line !== line) {
    filters.stationSearch = ''; 
    selectedStation.value = null; 
  }
  filters.line = filters.line === line ? '' : line;
  showResults.value = false;
};

const onFocus = () => { showResults.value = true; };
const onInput = (e) => { 
  filters.stationSearch = e.target.value; 
  selectedStation.value = null; 
  showResults.value = true; 
};

const selectStation = (st) => {
  filters.stationSearch = st.station_name;
  filters.line = st.line_name;
  selectedStation.value = st;
  showResults.value = false;
  executeSearch(); 
};

const executeSearch = () => {
  showResults.value = false;
  const keyword = filters.stationSearch.trim();
  
  if (!filters.line && !keyword) {
    emit('search', { type: 'all' });
  } else if (selectedStation.value) {
    emit('search', { type: 'station', station_id: selectedStation.value.station_id });
  } else if (keyword && !filters.line) {
    emit('search', { type: 'keyword', keyword: keyword });
  } else if (filters.line) {
    emit('search', { type: 'line', line_name: filters.line, keyword: keyword });
  }
};

const resetFilters = () => {
  filters.line = '';
  filters.stationSearch = '';
  selectedStation.value = null;
  showResults.value = false;
};
</script>

<style scoped>
.search-container { 
  padding: 30px; 
  background: #fff; 
  border-radius: 15px; 
  box-shadow: 0 4px 20px rgba(0,0,0,0.08); 
  margin-bottom: 25px; 
}
.filter-box { display: flex; align-items: flex-end; justify-content: flex-start; gap: 50px; }
.filter-item { display: flex; flex-direction: column; gap: 15px; }
.filter-label { font-weight: bold; font-size: 18px; color: #333; margin: 0; }
.line-section { flex-shrink: 0; }
.search-input-wrapper { flex-grow: 1; max-width: 350px; }
.line-button-group { display: flex; gap: 8px; }
.line-btn { padding: 10px 18px; border: 1px solid #eee; background: #fff; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 700; transition: all 0.2s; color: #444; }
.line-btn.active { color: white; border-color: transparent; }
.search-flex-row { display: flex; align-items: center; gap: 12px; }
.input-relative { position: relative; flex-grow: 1; }
.search-input-wrapper input { width: 100%; padding: 14px 20px; border: 2px solid #eee; border-radius: 10px; outline: none; font-size: 15px; }
.button-group { display: flex; gap: 8px; }
.icon-search-btn, .icon-reset-btn { width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; border-radius: 10px; cursor: pointer; }
.icon-search-btn { background: #1a2a3a; color: white; border: none; }
.icon-reset-btn { background: #ffffff; color: #333; border: 2px solid #eee; }
.auto-complete-list { position: absolute; top: calc(100% + 8px); left: 0; width: 100%; background: white; border: 1px solid #ddd; border-radius: 10px; z-index: 1000; max-height: 250px; overflow-y: auto; list-style: none; padding: 0; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.auto-complete-list li { padding: 12px 18px; cursor: pointer; border-bottom: 1px solid #f0f0f0; display: flex; justify-content: space-between; align-items: center; }
.st-line { padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; }
</style>```