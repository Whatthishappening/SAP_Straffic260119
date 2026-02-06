<template>
  <div class="subway-selector">
    <select v-model="selectedLine" @change="fetchStations">
      <option value="">호선 선택</option>
      <option v-for="line in lineOptions" :key="line" :value="line">
        {{ line }}
      </option>
    </select>

    <select v-model="selectedStation" :disabled="!selectedLine" @change="emitSelection">
      <option value="">역 선택</option>
      <option v-for="st in stationOptions" :key="st.station_id" :value="st">
        {{ st.station_name }}
      </option>
    </select>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

// 부모에게 선택된 데이터를 보냄
const emit = defineEmits(['update-selection']);

const selectedLine = ref('');
const selectedStation = ref('');
const lineOptions = ['1호선', '2호선', '3호선', '4호선', '5호선', '6호선', '7호선', '8호선', '9호선'];
const stationOptions = ref([]);

// 호선 변경 시 실행: 작성하신 MyBatis 쿼리 호출
const fetchStations = async () => {
  if (!selectedLine.value) {
    stationOptions.value = [];
    return;
  }
  
  try {
    // 백엔드 search_stationname 호출
    const resp = await axios.get('http://localhost:9000/search_stationname', {
      params: { line_name: selectedLine.value }
    });
    stationOptions.value = resp.data; // [{station_name: '...', station_id: '...'}, ...]
    selectedStation.value = ''; // 호선 바뀌면 역 선택 초기화
  } catch (err) {
    console.error("역 목록 로드 실패:", err);
  }
};

// 최종 선택 값 부모에게 전달
const emitSelection = () => {
  emit('update-selection', {
    line_name: selectedLine.value,
    station: selectedStation.value // name과 id가 담긴 객체 전달
  });
};
</script>

<style scoped>
.subway-selector {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 1. 공통 셀렉트 박스 기본 (군더더기 제거) */
select {
  appearance: none;
  -webkit-appearance: none;
  height: 42px;
  border-radius: 8px;
  padding: 0 35px 0 15px;
  font-size: 14px;
  outline: none;
  border: 1px solid #dcdfe6;
  background-color: #fff;
  transition: all 0.2s ease;
  cursor: pointer;
}

/* 2. [주인공] 역 선택 셀렉트 박스 스타일 */
select:last-of-type {
  min-width: 180px;
  border: 2px solid #3f417e; /* 관리자 테마 네이비 */
  font-weight: 700;
  color: #3f417e;
  
  /* 커스텀 화살표 아이콘 (네이비색) */
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%233f417e' stroke-width='3' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 14px;
}

/* 마우스 올렸을 때 살짝 반응 */
select:last-of-type:hover:not(:disabled) {
  background-color: #f9f9ff;
  box-shadow: 0 3px 8px rgba(63, 65, 126, 0.15);
}

/* 클릭(포커스) 시 더 강조 */
select:last-of-type:focus {
  background-color: #fff;
  box-shadow: 0 0 0 3px rgba(63, 65, 126, 0.2);
}

/* 3. 비활성화(Disabled)일 때 - 힘 빠진 스타일 */
select:last-of-type:disabled {
  border: 1px solid #e4e7ed;
  background-color: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
  box-shadow: none;
  /* 화살표도 흐리게 */
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%23c0c4cc' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
}

/* 호선 선택(첫 번째)은 평범하지만 깔끔하게 */
select:first-of-type {
  min-width: 130px;
  color: #666;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%23909399' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
}
</style>