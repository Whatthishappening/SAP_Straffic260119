<template>
  <div class="issue-create">
    <div class="main">
      <h2 class="page-title">장애/이슈 발생 보고</h2>
      <div class="field">
        <label>제목 입력 <span class="required">*</span></label>
        <input v-model="formData.incident_title" type="text" placeholder="제목을 입력하세요" required />
      </div>
      <div class="field">
        <label>내용 입력</label>
        <textarea v-model="formData.incident_content" placeholder="여기에 내용을 입력 해주세요!"></textarea>
      </div>
      <div class="actions">
        <button class="cancel" @click="$emit('change-view', 'insident')">취소</button>
        <button class="submit" @click="handleSubmit">글작성</button>
      </div>
    </div>

    <aside class="side" ref="searchContainer">
      <section class="side-section">
        <div class="side-header">
          <h4>심각도</h4>
          <span class="gear-btn" @click="toggleMenu('severity')">⚙️</span>
        </div>
        
        <div v-if="activeMenu === 'severity'" class="dropdown-menu">
          <div 
            v-for="lv in ['Critical', 'Major', 'Minor']" 
            :key="lv" 
            class="menu-item" 
            :class="lv.toLowerCase()"
            @click="selectSeverity(lv)"
          >
            {{ lv }}
          </div>
        </div>

        <span v-if="formData.incident_severity" 
              class="badge severity" 
              :class="formData.incident_severity.toLowerCase()">
          {{ formData.incident_severity }}
        </span>
      </section>

      <section class="side-section">
        <div class="side-header">
          <h4>분류 설정</h4>
          <span class="gear-btn" @click="toggleMenu('location')">⚙️</span>
        </div>
        
        <div v-if="activeMenu === 'location'" class="dropdown-menu location-picker-simple">
          <StationSimpleSelector @selected="onStationSelected" />
        </div>

        <div class="tags" v-if="formData.incident_line_name">
          <span class="tag line display-box" :class="'line-' + formData.incident_line_name">
            {{ formData.incident_line_name }}
          </span>

          <span class="tag station" v-if="formData.incident_station_name">
            {{ formData.incident_station_name }}
          </span>
        </div>
        
        <div class="reporter">작성자: {{ formData.user_name }} ({{ formData.user_id }})</div>
      </section>
    </aside>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import axios from 'axios';
import StationSimpleSelector from './StationSimpleSelector.vue'; 

const emit = defineEmits(['change-view']);
const searchContainer = ref(null);
const activeMenu = ref(null);

const formData = reactive({
  incident_title: '',
  incident_line_name: '',
  incident_station_id: '',
  incident_station_name: '',
  incident_content: '',
  incident_status: '1', // '대기' 상태 코드
  incident_severity: '', 
  user_id: '',
  user_name: ''
});

const onStationSelected = (data) => {
  formData.incident_line_name = data.line_name;
  formData.incident_station_name = data.station_name;
  formData.incident_station_id = data.station_id;
  activeMenu.value = null; 
};

const toggleMenu = (type) => {
  activeMenu.value = activeMenu.value === type ? null : type;
};

const selectSeverity = (level) => {
  formData.incident_severity = level;
  activeMenu.value = null;
};

const handleSubmit = () => {
  if (!formData.incident_title.trim()) return alert("제목을 입력해주세요.");
  if (!formData.incident_severity) return alert("심각도를 선택해주세요.");
  if (!formData.incident_line_name) return alert("장애가 발생한 노선을 선택해주세요.");

  axios.post('http://localhost:9000/create_incident', formData)
    .then(resp => {
      if (resp.data === "YES" || resp.data === 1) {
        alert('이슈 작성이 완료되었습니다.');
        emit('change-view', 'insident');
      } else {
        alert('이슈 작성에 실패했습니다.');
      }
    })
    .catch(err => {
      console.error(err);
      alert('서버 통신 중 오류가 발생했습니다.');
    });
};

const handleClickOutside = (e) => {
  if (searchContainer.value && !searchContainer.value.contains(e.target)) {
    activeMenu.value = null;
  }
};

onMounted(() => {
  const savedUser = sessionStorage.getItem("user_info");
  if (savedUser) {
    const user = JSON.parse(savedUser);
    formData.user_id = user.user_id || user.id;
    formData.user_name = user.user_name || user.name;
  }
  window.addEventListener('mousedown', handleClickOutside);
});

onUnmounted(() => {
  window.removeEventListener('mousedown', handleClickOutside);
});
</script>

<style scoped>
/* 1. 기본 레이아웃 */
.issue-create { display: flex; gap: 30px; max-width: 1100px; margin: 20px auto; padding: 0 20px; font-family: 'Pretendard', sans-serif; }
.main { flex: 7; }
.side { flex: 3; border-left: 1px solid #eee; padding-left: 24px; position: relative; }

/* 2. 입력 필드 */
.page-title { margin-bottom: 30px; font-size: 24px; font-weight: bold; color: #333; }
.field { margin-bottom: 25px; }
.field label { display: block; margin-bottom: 10px; font-weight: bold; font-size: 14px; color: #555; }
.required { color: #e74c3c; }
input, textarea { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px; box-sizing: border-box; outline: none; }
input:focus, textarea:focus { border-color: #2ecc71; }
textarea { min-height: 300px; resize: none; }

/* 3. 사이드바 디자인 */
.side-section { margin-bottom: 30px; position: relative; }
.side-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.side-header h4 { margin: 0; font-size: 14px; color: #888; text-transform: uppercase; }
.gear-btn { cursor: pointer; font-size: 18px; opacity: 0.6; }

/* 4. 드롭다운 메뉴 및 컬러 */
.dropdown-menu { 
  position: absolute; top: 35px; right: 0; background: #fff; 
  border: 1px solid #ddd; border-radius: 8px; 
  box-shadow: 0 10px 25px rgba(0,0,0,0.1); z-index: 100; 
  min-width: 150px; overflow: hidden; 
}
.menu-item { padding: 12px 20px; cursor: pointer; font-size: 13px; color: #333; transition: 0.2s; }
.menu-item:hover { background: #f8f9fa; }

/* 드롭다운 내 글자 색상 */
.menu-item.critical { color: #EE0000 !important; font-weight: bold; }
.menu-item.major { color: #F89B00 !important; font-weight: bold; }
.menu-item.minor { color: #17F000 !important; font-weight: bold; }

/* 5. 배지(Badge) 스타일 (리스트 페이지와 통일) */
.badge { display: inline-block; padding: 6px 14px; border-radius: 20px; font-size: 11px; font-weight: bold; color: #fff; text-transform: uppercase; }
.badge.critical { background-color: #EE0000 !important; }
.badge.major { background-color: #F89B00 !important; }
.badge.minor { background-color: #17F000 !important; }

/* 6. 호선 컬러 (요청하신 고유 컬러 적용) */
.tags { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 10px; }
.tag { padding: 6px 12px; border-radius: 6px; color: #fff; font-size: 12px; font-weight: bold; }
.tag.station { background: #000; }

.display-box.line-1호선 { background-color: #2a317c; }
.display-box.line-2호선 { background-color: #2fae35; }
.display-box.line-3호선 { background-color: #ff6000; }
.display-box.line-4호선 { background-color: #1a97dd; }
.display-box.line-5호선 { background-color: #822fe1; }
.display-box.line-6호선 { background-color: #ae4908; }
.display-box.line-7호선 { background-color: #636b10; }
.display-box.line-8호선 { background-color: #e6265b; }
.display-box.line-9호선 { background-color: #bdb092; }

/* 7. 기타 */
.actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 40px; }
.submit { background: #2ecc71; color: #fff; border: none; padding: 12px 40px; border-radius: 8px; cursor: pointer; font-weight: bold; transition: 0.2s; }
.submit:hover { background: #27ae60; }
.cancel { background: #f2f2f2; border: none; padding: 12px 30px; border-radius: 8px; cursor: pointer; color: #666; }
.reporter { margin-top: 20px; font-size: 12px; color: #aaa; font-style: italic; border-top: 1px solid #f9f9f9; padding-top: 10px; }
</style>