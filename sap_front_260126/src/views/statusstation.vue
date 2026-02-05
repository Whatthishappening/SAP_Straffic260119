<template>
  <div class="status-container">
    <SubwaySearch @search="handleSearch" />

    <div class="category-filter-card">
      <div class="filter-header">
        <h3 class="filter-title">데이터 노출 항목 설정</h3>
        <button @click="toggleAllCategories" class="toggle-all-btn">
          {{ isAllSelected ? '전체 해제' : '전체 선택' }}
        </button>
      </div>
      <div class="checkbox-group">
        <label v-for="cat in categoryOptions" :key="cat.key" class="check-label">
          <input type="checkbox" :value="cat.key" v-model="selectedCategories">
          {{ cat.label }}
        </label>
      </div>
    </div>

    <div class="table-wrapper">
      <table class="status-table">
        <thead>
          <tr>
            <th style="width: 120px;">역명</th>
            <th style="width: 80px;">호선</th>
            <th v-if="isVisible('incident_count')">장애발생건수</th>
            <th v-if="isVisible('lockers')" style="min-width: 180px;">물품보관함 (실시간/설치/사용률)</th>
            <th v-if="isVisible('elevator')">엘리베이터</th>
            <th v-if="isVisible('parking')">환승주차장</th>
            <th v-if="isVisible('wheelchair')">휠체어리프트</th>
            <th v-if="isVisible('civil_service')">무인민원발급기</th>
            <th v-if="isVisible('currency')">환전키오스크</th>
            <th v-if="isVisible('train_ticket')">기차예매</th>
            <th v-if="isVisible('culture')">문화공간</th>
            <th v-if="isVisible('meeting')">만남의장소</th>
            <th v-if="isVisible('lactation')">유아 수유방</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="displayList.length === 0">
            <td colspan="13" class="no-data">조회된 데이터가 없습니다. 역명 또는 호선을 선택해 주세요.</td>
          </tr>
          <tr v-for="item in displayList" :key="item.station_id + item.line_name">
            <td class="bold">{{ item.station_name }}</td>
            <td class="line-cell">
              <span class="line-badge" :style="{ backgroundColor: getLineColor(item.line_name) }">
                {{ item.line_name }}
              </span>
            </td>
            <td v-if="isVisible('incident_count')" :class="{ 'text-danger': item.incident_count > 0 }">
              {{ item.incident_count }}개
            </td>
            <td v-if="isVisible('lockers')">
              <span class="used-cnt">{{ item.used_lockers }}</span> / {{ item.total_lockers }}개 
              <span class="usage-text">({{ calculateUsageRate(item.used_lockers, item.total_lockers) }})</span>
            </td>
            <td v-if="isVisible('elevator')">{{ formatStatus(item.elevator) }}</td>
            <td v-if="isVisible('parking')">{{ formatStatus(item.parking) }}</td>
            <td v-if="isVisible('wheelchair')">{{ formatStatus(item.wheelchairlift) }}</td>
            <td v-if="isVisible('civil_service')">{{ formatStatus(item.complaint) }}</td>
            <td v-if="isVisible('currency')">{{ formatStatus(item.exchange) }}</td>
            <td v-if="isVisible('train_ticket')">{{ formatStatus(item.trainreservation) }}</td>
            <td v-if="isVisible('culture')">{{ formatStatus(item.culturalspace) }}</td>
            <td v-if="isVisible('meeting')">{{ formatStatus(item.meeting) }}</td>
            <td v-if="isVisible('lactation')">{{ formatStatus(item.lactation) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import axios from 'axios';
import SubwaySearch from './SubwaySearch.vue';

const categoryOptions = [
  { key: 'incident_count', label: '장애발생건수' }, 
  { key: 'lockers', label: '물품보관함' },
  { key: 'elevator', label: '엘리베이터' },
  { key: 'parking', label: '환승주차장' }, 
  { key: 'wheelchair', label: '휠체어리프트' },
  { key: 'civil_service', label: '무인민원발급기' }, 
  { key: 'currency', label: '환전키오스크' },
  { key: 'train_ticket', label: '기차예매역' }, 
  { key: 'culture', label: '문화공간' },
  { key: 'meeting', label: '만남의장소' }, 
  { key: 'lactation', label: '유아 수유방' }
];

const selectedCategories = ref(categoryOptions.map(c => c.key));
const displayList = ref([]);
const isAllSelected = computed(() => selectedCategories.value.length === categoryOptions.length);
const toggleAllCategories = () => { selectedCategories.value = isAllSelected.value ? [] : categoryOptions.map(c => c.key); };

const getLineColor = (line) => {
  const colors = { '1호선': '#0052A4', '2호선': '#00A84D', '3호선': '#EF7C1C', '4호선': '#00A5DE', '5호선': '#996CAC', '6호선': '#CD7C2F', '7호선': '#747F28', '8호선': '#E6186C', '9호선': '#BDB092' };
  return colors[line] || '#333';
};

const formatStatus = (val) => (val === 'Y' || val === 'O' ? 'O' : 'X');
const isVisible = (key) => selectedCategories.value.includes(key);

// 사용률 계산 (한 줄 표현용)
const calculateUsageRate = (used, total) => {
  if (!total || total === 0) return '0%';
  const rate = Math.floor((used / total) * 100);
  return `${rate}%`;
};

const handleSearch = async (searchData) => {
  try {
    const res = await axios.get('http://localhost:9000/get_status');
    let allData = res.data;
    const searchKeyword = searchData.keyword || '';

    if (searchData.type === 'station') {
      displayList.value = allData.filter(s => s.station_id === searchData.station_id);
    } 
    else if (searchData.type === 'line') {
      displayList.value = allData.filter(s => {
        const matchLine = s.line_name === searchData.line_name;
        return searchKeyword ? (matchLine && s.station_name.includes(searchKeyword)) : matchLine;
      });
    } 
    else if (searchData.type === 'keyword') {
      displayList.value = allData.filter(s => s.station_name.includes(searchKeyword));
    }
    else {
      displayList.value = allData;
    }

    // [정렬] 역명 가나다순 -> 역명이 같으면 호선 숫자 순
    displayList.value.sort((a, b) => {
      const nameSort = a.station_name.localeCompare(b.station_name, 'ko');
      if (nameSort !== 0) return nameSort;

      const getLineNum = (str) => parseInt(str.replace(/[^0-9]/g, "")) || 999;
      return getLineNum(a.line_name) - getLineNum(b.line_name);
    });

  } catch (err) { 
    console.error("데이터 로드 실패:", err); 
  }
};
</script>

<style scoped>
.status-container { padding: 20px; }
.category-filter-card { background: #fff; border: 1px solid #eee; border-radius: 12px; padding: 20px; margin-bottom: 20px; }
.filter-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.filter-title { font-weight: bold; font-size: 18px; color: #333; margin: 0; }
.toggle-all-btn { padding: 5px 12px; font-size: 12px; cursor: pointer; background: #f4f4f4; border: 1px solid #ddd; border-radius: 4px; }
.checkbox-group { display: flex; flex-wrap: wrap; gap: 15px; }
.table-wrapper { background: #fff; border-radius: 12px; overflow-x: auto; border: 1px solid #eee; }
.status-table { 
  width: 100%; 
  border-collapse: collapse; 
  text-align: center; 
  min-width: 1200px; 
  table-layout: auto; /* fixed에서 auto로 변경 */
}

/* 2. 헤더(th) 설정 수정 */
.status-table th { 
  background: #f8f9fa; 
  border-bottom: 2px solid #eee; 
  font-weight: bold;
  padding: 10px 5px;
  font-size: 13px; /* 글자가 너무 크면 살짝 줄임 */
  line-height: 1.2;
  vertical-align: middle;
  /* white-space: nowrap;  <-- 이 줄을 삭제하거나 아래처럼 변경 */
  white-space: normal;  /* 긴 제목은 두 줄로 나올 수 있게 허용 */
  word-break: keep-all; /* 단어 단위로 줄바꿈되어 깔끔하게 유지 */
}

/* 3. 데이터 셀(td) 설정 */
.status-table td { 
  padding: 12px 8px; 
  border-bottom: 1px solid #f0f0f0; 
  font-size: 14px; 
  white-space: nowrap; /* 데이터는 한 줄로 유지 */
  /* overflow, text-overflow 관련 설정 삭제 */
}
.line-cell { width: 100px; }
.line-badge { 
  display: inline-block;
  color: white; 
  padding: 4px 12px; 
  border-radius: 20px; 
  font-size: 12px; 
  font-weight: bold; 
  white-space: nowrap;
  flex-shrink: 0;
}
.used-cnt { color: #007bff; font-weight: bold; }
.bold { font-weight: bold; }
.text-danger { color: #ff0000; font-weight: bold; }
.no-data { padding: 40px; color: #999; text-align: center; }
</style>