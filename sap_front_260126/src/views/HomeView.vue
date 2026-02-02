<template>
  <div class="dashboard-container">
    <header class="dashboard-header">
      <h1>{{ linetitle }}</h1>
    </header>

    <section class="main-summary-bar">
      <div class="summary-item main-info">
        <span class="item-label">운영 현황 요약</span>
        <div class="item-content">
          <p>노선 : <span class="num highlight">{{ linetotal }}</span> 개</p>
          <p>전체 역 : <span class="num highlight">{{ stationtotal }}</span> 개</p>
        </div>
      </div>

      <div class="v-divider"></div>

      <div class="summary-item">
        <span class="item-label">총 물품보관함</span>
        <div class="chart-box big-chart">
          <canvas id="totalStorageChart"></canvas>
        </div>
        <div class="chart-bottom-text big-text">{{ total_lockers.toLocaleString() }} 개</div>
      </div>

      <div class="v-divider"></div>

      <div class="summary-item">
        <span class="item-label">실시간 사용 중</span>
        <div class="chart-box big-chart">
          <canvas id="usedStorageChart"></canvas>
        </div>
        <div class="chart-bottom-text big-text used-color">{{ used_lockers.toLocaleString() }} 개</div>
      </div>

      <div class="v-divider"></div>

      <div class="summary-item">
        <span class="item-label">장애 / 이슈 현황</span>
        <div class="issue-box-vertical large">
          <img src="@/assets/경고표시.png" alt="경고" class="issue-icon-big" />
          <div class="issue-text-group">
            <span class="error-count-big">{{ incident_count }}</span>
            <span class="unit-big">건</span>
          </div>
        </div>
      </div>
      
      <div class="v-divider"></div>

      <div class="summary-item contact-section">
        <span class="item-label">비상 연락망</span>
        <ul class="contact-list-big">
          <li><strong>서울교통공사 </strong> 1577-1234</li>
          <li><strong>코레일</strong> 1544-7788</li>
          <li><strong>서울시메트로9</strong> 1544-4009</li>
          <li><strong>네오트랜스</strong> 031-8018-7777</li>
          <li><strong>공항철도</strong> 1544-7769</li>
          <li><strong>코레일(KR)</strong> 1544-7788</li>
          <li><strong>인천교통공사</strong> 032-451-2114</li>
          <li><strong>우이신설선경</strong> 02-1660-1555</li>
        </ul>
      </div>
    </section>

    <div class="middle-widgets">
      <div class="card">
        <h3>시간대별 사용률</h3>
        <div class="canvas-wrapper">
          <canvas id="hourlyChart"></canvas>
        </div>
      </div>
      
      <div class="card">
        <h3>최근 7일 장애 발생 현황</h3>
        <div class="canvas-wrapper empty-data">
          <canvas id="weeklyChart"></canvas>
          <div v-if="!hasWeeklyData" class="no-data-overlay">데이터 집계 대기 중</div>
        </div>
      </div>

      <div class="card clickable-card" @click="goTrainInfo">
        <h3>실시간 지하철 정보 <span v-if="userAuth === '2'">({{ userLine }}호선)</span><span v-else>(전체)</span></h3>
        <div class="iframe-mask image-display-container">
          <img :src="getLineImage()" alt="노선도 이미지" class="route-map-img-fixed" />
          <div class="hover-info">클릭 시 실시간 위치 페이지로 이동</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import axios from 'axios';
import Chart from 'chart.js/auto';

const dashboardData = ref(null);
const userLine = ref(""); 
const userAuth = ref("");
const hasWeeklyData = ref(false); 
let chartInstances = {};

// [이미지 경로 로직 수정] 
// assets/Line/Line1.png ~ Lineall.png 구조 대응
const getLineImage = () => {
  let fileName = "Lineall.png"; // 기본값 (마스터용)
  
  if (userAuth.value === "2" && userLine.value) {
    fileName = `Line${userLine.value}.png`;
  }
  
  return new URL(`../assets/Line/${fileName}`, import.meta.url).href;
};

const goTrainInfo = () => {
  window.open("https://smss.seoulmetro.co.kr/traininfo/traininfoUserView.do", '_blank');
};

const linetitle = computed(() => dashboardData.value?.linetitle || '운영 현황');
const linetotal = computed(() => dashboardData.value?.linetotal || 0);
const stationtotal = computed(() => dashboardData.value?.stationtotal || 0);
const total_lockers = computed(() => dashboardData.value?.total_lockers || 0);
const used_lockers = computed(() => dashboardData.value?.used_lockers || 0);
const incident_count = computed(() => dashboardData.value?.incident_count || 0);

const currentRate = computed(() => {
  if (!total_lockers.value) return 0;
  return Math.round((used_lockers.value / total_lockers.value) * 100);
});

const initCharts = () => {
  Object.values(chartInstances).forEach(instance => instance.destroy());
  const donutOpt = { cutout: '70%', responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } };

  const ctx1 = document.getElementById('totalStorageChart')?.getContext('2d');
  if (ctx1) chartInstances.total = new Chart(ctx1, { type: 'doughnut', data: { datasets: [{ data: [1, 0], backgroundColor: ['#4BC0C0', '#f2f2f2'], borderWidth: 0 }] }, options: donutOpt });

  const ctx2 = document.getElementById('usedStorageChart')?.getContext('2d');
  if (ctx2) {
    const t = Number(total_lockers.value) || 1;
    const u = Number(used_lockers.value) || 0;
    chartInstances.used = new Chart(ctx2, { type: 'doughnut', data: { datasets: [{ data: [u, Math.max(0, t - u)], backgroundColor: ['#36A2EB', '#f2f2f2'], borderWidth: 0 }] }, options: donutOpt });
  }

  const ctxHourly = document.getElementById('hourlyChart')?.getContext('2d');
  if (ctxHourly) {
    const rate = currentRate.value;
    const chartData = dashboardData.value?.hourlyData || [rate-5, rate-2, rate+5, rate+10, rate+2, rate];
    chartInstances.hourly = new Chart(ctxHourly, {
      type: 'line',
      data: { labels: ['08시', '11시', '14시', '17시', '20시', '23시'], datasets: [{ label: '사용률 (%)', data: chartData, borderColor: '#36A2EB', fill: true, backgroundColor: 'rgba(54, 162, 235, 0.1)', tension: 0.4 }] },
      options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true, max: 100 } } }
    });
  }
};

const fetchData = async () => {
  try {
    const loginData = sessionStorage.getItem("login");
    let s_id = "";
    if (loginData) {
      const user = JSON.parse(loginData);
      userAuth.value = String(user.auth);
      s_id = user.station_id;
      if (userAuth.value === "2" && s_id) {
        // 호선 추출 (역코드 첫자리)
        userLine.value = String(s_id).charAt(0);
      }
    }
    const res = await axios.get('http://localhost:9000/get_dashboard', { params: { auth: userAuth.value, station_id: s_id } });
    dashboardData.value = res.data;
    await nextTick();
    setTimeout(initCharts, 50);
  } catch (err) { console.error(err); }
};
onMounted(fetchData);
</script>

<style scoped>
/* 사용자님 CSS 원본 그대로 유지 */
.dashboard-container { padding: 30px; background: #f4f7fa; min-height: 100vh; font-family: 'Pretendard', sans-serif; }
.dashboard-header { margin-bottom: 25px; padding-left: 10px; }
.dashboard-header h1 { font-size: 28px; font-weight: 800; color: #1a2a3a; margin: 0; }
.main-summary-bar { display: flex; align-items: center; background: #fff; padding: 25px 25px; border-radius: 15px; box-shadow: 0 8px 25px rgba(0,0,0,0.06); gap: 10px; margin-bottom: 30px; }
.summary-item { flex: 1; display: flex; flex-direction: column; align-items: center; text-align: center; }
.item-label { font-size: 15px; font-weight: 700; color: #777; margin-bottom: 12px; }
.num.highlight { font-size: 24px; font-weight: 800; color: #1a2a3a; }
.item-content p { font-size: 16px; margin: 4px 0; color: #444; }
.chart-box.big-chart { width: 110px; height: 110px; margin-bottom: 8px; position: relative; }
.chart-bottom-text.big-text { font-size: 20px; font-weight: 800; color: #222; }
.used-color { color: #36A2EB; }
.issue-icon-big { width: 110px; height: auto; margin-bottom: 5px; }
.error-count-big { font-size: 40px; font-weight: 900; color: #d32f2f; line-height: 1; }
.unit-big { font-size: 18px; font-weight: 700; color: #d32f2f; margin-left: 3px; }
.contact-list-big { list-style: none; padding: 0; font-size: 16px; line-height: 1.6; text-align: left; }
.v-divider { width: 1.5px; height: 120px; background: #eee; margin: 0 10px; }
.middle-widgets { display: grid; grid-template-columns: repeat(3, 1fr); gap: 25px; }
.card { background: #fff; padding: 25px; border-radius: 18px; height: 480px; box-shadow: 0 5px 15px rgba(0,0,0,0.04); display: flex; flex-direction: column; }
.card h3 { font-size: 19px; font-weight: 700; margin-bottom: 20px; color: #333; border-left: 5px solid #36A2EB; padding-left: 12px; }
.canvas-wrapper { height: 360px; width: 100%; position: relative; }

/* 이미지 영역 스타일 */
.iframe-mask { flex: 1; position: relative; overflow: hidden; border-radius: 12px; border: 1px solid #eee; background: #fff; }
.image-display-container { display: flex; justify-content: center; align-items: center; cursor: pointer; }
.route-map-img-fixed { width: 100%; height: 100%; object-fit: contain; }
.hover-info { 
  position: absolute; top: 0; left: 0; width: 100%; height: 100%; 
  background: rgba(0,0,0,0.4); color: #fff; display: flex; 
  align-items: center; justify-content: center; opacity: 0; transition: 0.3s; font-weight: bold; 
}
.image-display-container:hover .hover-info { opacity: 1; }
.no-data-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: #f9f9f9; display: flex; align-items: center; justify-content: center; color: #aaa; font-size: 14px; border-radius: 12px; }
</style>