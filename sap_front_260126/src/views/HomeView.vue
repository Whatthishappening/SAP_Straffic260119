<template>
  <div class="dashboard-container">
    <header class="dashboard-header">
      <h1>{{ linetitle }}</h1>
    </header>

    <section class="main-summary-bar">
      <div class="summary-item main-info">
        <span class="item-label">운영 현황 요약</span>
        <div class="item-content">
          <template v-if="userAuth === '1'">
            <p>노선 : <span class="num highlight">{{ linetotal }}</span> 개</p>
            <p>전체 역 : <span class="num highlight">{{ stationtotal }}</span> 개</p>
          </template>
          <template v-else>
            <p>{{ userLine }}호선 역 개수 : <span class="num highlight">{{ line_stationcount }}</span> 개</p>
            <p>전체 역 개수 : <span class="num highlight">{{ stationtotal }}</span> 개</p>
          </template>
        </div>
      </div>

      <div class="v-divider"></div>

      <div class="summary-item">
        <span class="item-label">물품보관함 이용 현황</span>
        <div class="chart-box big-chart">
          <canvas id="usedStorageChart"></canvas>
        </div>
        <div class="chart-bottom-text big-text">
          <span class="used-color">{{ used_lockers.toLocaleString() }}</span>
          <span class="total-slash"> / {{ total_lockers.toLocaleString() }} 개</span>
        </div>
      </div>

      <div class="v-divider"></div>

      <div class="summary-item incident-widget" @click="goIncidentBoard">
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

      <div class="summary-item weather-widget" @click="goWeatherDetail">
        <span class="item-label">오늘의 날씨</span>
        <div class="weather-content">
          <div class="weather-top">
            <span class="weather-icon">{{ weatherIcon }}</span>
            <span class="weather-temp">{{ seoulWeather.ta }}°</span>
          </div>
          <div class="weather-middle">
            <span class="weather-desc">{{ weatherStatus }}</span>
            <span class="slash">/</span>
            <span class="feels-like">체감 {{ feelsLikeTemp }}°</span>
          </div>
          <div class="weather-bottom">
            <span>{{ windDirText }} {{ seoulWeather.ws }}m/s</span>
            <span class="dot">•</span>
            <span>습도 {{ seoulWeather.hm }}%</span>
            <span class="dot">•</span>
            <span>강수 {{ displayRain }}</span>
          </div>
          <div class="weather-time">
            서울기상관측소 | {{ formatObsTime(seoulWeather.tm) }} 관측
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
        <div class="canvas-wrapper">
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
import { useRouter } from 'vue-router'; // 이 부분 확인 필수
import axios from 'axios';
import Chart from 'chart.js/auto';
import ChartDataLabels from 'chartjs-plugin-datalabels';

const router = useRouter(); // router 초기화
const dashboardData = ref(null);
const userLine = ref(""); 
const userAuth = ref("");
const hasWeeklyData = ref(false); 
let chartInstances = {};

// 날씨 데이터 & 로직 (건드리지 않음)
const seoulWeather = ref({ tm: '202602040900', stn: 108, wd: 5, ws: 2.1, ta: 2.0, hm: 75, rn: -9.0 });
const windDirText = computed(() => {
  const dirs = ['북', '북동', '동', '남동', '남', '남서', '서', '북서', '북'];
  return dirs[Math.round(seoulWeather.value.wd / 45)] + '풍';
});
const displayRain = computed(() => seoulWeather.value.rn < 0 ? '0mm' : seoulWeather.value.rn + 'mm');
const feelsLikeTemp = computed(() => {
  const t = seoulWeather.value.ta;
  const v = seoulWeather.value.ws;
  return (13.12 + (0.6215 * t) - (11.37 * Math.pow(v, 0.16)) + (0.3965 * t * Math.pow(v, 0.16))).toFixed(1);
});
const weatherStatus = computed(() => seoulWeather.value.rn > 0 ? '비/눈' : (seoulWeather.value.hm > 70 ? '흐림' : '맑음'));
const weatherIcon = computed(() => seoulWeather.value.rn > 0 ? '☔' : (seoulWeather.value.hm > 70 ? '☁️' : '☀️'));
const formatObsTime = (tm) => tm ? `${tm.substring(8, 10)}:${tm.substring(10, 12)}` : '';

// 이동 관련 함수들
const goIncidentBoard = () => router.push('/incident'); // 장애 게시판 이동 로직
const goTrainInfo = () => window.open("https://smss.seoulmetro.co.kr/traininfo/traininfoUserView.do", '_blank');
const goWeatherDetail = () => window.open("https://weather.naver.com/map/", '_blank');

const getLineImage = () => {
  let fileName = "Lineall.png";
  if (userAuth.value === "2" && userLine.value) fileName = `Line${userLine.value}.png`;
  return new URL(`../assets/Line/${fileName}`, import.meta.url).href;
};

// 데이터 바인딩 및 차트 로직 (기존과 동일)
const linetitle = computed(() => dashboardData.value?.linetitle || '운영 현황');
const linetotal = computed(() => dashboardData.value?.linetotal || 0);
const stationtotal = computed(() => dashboardData.value?.stationtotal || 0);
const line_stationcount = computed(() => dashboardData.value?.line_stationcount || 0);
const total_lockers = computed(() => dashboardData.value?.total_lockers || 0);
const used_lockers = computed(() => dashboardData.value?.used_lockers || 0);
const incident_count = computed(() => dashboardData.value?.incident_count || 0);

const currentRate = computed(() => total_lockers.value ? Math.round((used_lockers.value / total_lockers.value) * 100) : 0);

const initCharts = () => {
  Object.values(chartInstances).forEach(instance => instance.destroy());
  
  const ctxUsed = document.getElementById('usedStorageChart')?.getContext('2d');
  if (ctxUsed) {
    chartInstances.used = new Chart(ctxUsed, { 
      type: 'doughnut', 
      data: { 
        labels: ['사용 중', '여유'],
        datasets: [{ 
          data: [used_lockers.value, Math.max(0, total_lockers.value - used_lockers.value)], 
          backgroundColor: ['#36A2EB', '#f2f2f2'], 
          borderWidth: 0 
        }] 
      }, 
      options: { cutout: '70%', responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false }, datalabels: { display: false } } } 
    });
  }

  const ctxHourly = document.getElementById('hourlyChart')?.getContext('2d');
  if (ctxHourly) {
    const chartData = dashboardData.value?.hourlyData || [currentRate.value-5, currentRate.value-2, currentRate.value, currentRate.value+3, currentRate.value+1, currentRate.value];
    chartInstances.hourly = new Chart(ctxHourly, {
      type: 'line',
      plugins: [ChartDataLabels],
      data: { 
        labels: ['08시', '11시', '14시', '17시', '20시', '23시'], 
        datasets: [{ 
          data: chartData, 
          borderColor: '#36A2EB', 
          fill: true, 
          backgroundColor: 'rgba(54, 162, 235, 0.1)', 
          tension: 0.4,
          pointRadius: 5
        }] 
      },
      options: { 
        responsive: true, maintainAspectRatio: false,
        scales: { y: { beginAtZero: true, max: 100, ticks: { callback: (v) => v + '%' } } },
        plugins: { legend: { display: false }, datalabels: { display: true, align: 'top', formatter: (v) => v + '%' } }
      }
    });
  }

  const ctxWeekly = document.getElementById('weeklyChart')?.getContext('2d');
  const weeklyData = dashboardData.value?.weekly_issue;

  if (ctxWeekly && weeklyData && weeklyData.length > 0) {
    hasWeeklyData.value = weeklyData.some(d => d.count > 0);
    chartInstances.weekly = new Chart(ctxWeekly, {
      type: 'bar',
      plugins: [ChartDataLabels],
      data: {
        labels: weeklyData.map(d => d.date), 
        datasets: [{
          data: weeklyData.map(d => d.count), 
          backgroundColor: '#FF6384',
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: { 
          y: { beginAtZero: true, ticks: { stepSize: 1, precision: 0 } },
          x: { grid: { display: false } }
        },
        plugins: {
          legend: { display: false },
          datalabels: {
            display: true, anchor: 'end', align: 'top', color: '#FF6384',
            font: { weight: 'bold' }, formatter: (v) => v > 0 ? v + '건' : '0'
          }
        }
      }
    });
  } else {
    hasWeeklyData.value = false;
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
      if (userAuth.value === "2" && s_id) userLine.value = String(s_id).charAt(0);
    }
    const res = await axios.get('http://localhost:9000/get_dashboard', { params: { auth: userAuth.value, station_id: s_id } });
    dashboardData.value = res.data;
    await nextTick();
    setTimeout(initCharts, 100); 
  } catch (err) { console.error(err); }
};
onMounted(fetchData);
</script>

<style scoped>
/* 기존 스타일 그대로 유지 */
.dashboard-container { padding: 30px; background: #f4f7fa; min-height: 100vh; font-family: 'Pretendard', sans-serif; }
.dashboard-header { margin-bottom: 25px; padding-left: 10px; }
.dashboard-header h1 { font-size: 28px; font-weight: 800; color: #1a2a3a; margin: 0; }
.main-summary-bar { display: flex; align-items: center; background: #fff; padding: 25px; border-radius: 15px; box-shadow: 0 8px 25px rgba(0,0,0,0.06); gap: 5px; margin-bottom: 30px; }
.summary-item { flex: 1; display: flex; flex-direction: column; align-items: center; text-align: center; }

/* 클릭 위젯 효과 추가 */
.incident-widget, .weather-widget { cursor: pointer; transition: transform 0.2s; }
.incident-widget:hover, .weather-widget:hover { transform: translateY(-3px); }

.weather-content { display: flex; flex-direction: column; align-items: center; }
.weather-top { display: flex; align-items: center; gap: 8px; }
.weather-icon { font-size: 32px; }
.weather-temp { font-size: 36px; font-weight: 800; color: #222; }
.weather-middle { margin-top: 2px; font-size: 15px; color: #444; font-weight: 600; display: flex; align-items: center; gap: 6px; }
.feels-like { color: #36A2EB; }
.slash { color: #ddd; }
.weather-bottom { margin-top: 8px; font-size: 12.5px; color: #777; display: flex; align-items: center; gap: 4px; }
.dot { font-size: 8px; color: #ccc; }
.weather-time { margin-top: 6px; font-size: 11px; color: #999; font-weight: 500; }
.item-label { font-size: 18px; font-weight: 700; color: #1a2a3a; margin-bottom: 12px; }
.num.highlight { font-size: 24px; font-weight: 800; color: #1a2a3a; }
.item-content p { font-size: 16px; margin: 4px 0; color: #444; }
.chart-box.big-chart { width: 110px; height: 110px; margin-bottom: 8px; position: relative; }
.chart-bottom-text.big-text { font-size: 20px; font-weight: 800; color: #222; }
.used-color { color: #36A2EB; font-size: 22px; }
.total-slash { font-size: 16px; color: #888; font-weight: 500; }
.issue-icon-big { width: 120px; height: auto; margin-bottom: 12px; }
.error-count-big { font-size: 30px; font-weight: 900; color: #d32f2f; line-height: 1; padding-top: 4px; }
.unit-big { font-size: 20px; font-weight: 700; color: #d32f2f; margin-left: 3px;}
.contact-list-big { list-style: none; padding: 0; font-size: 13.5px; line-height: 1.5; text-align: left; }
.v-divider { width: 1.5px; height: 130px; background: #eee; margin: 0 5px; }
.middle-widgets { display: grid; grid-template-columns: repeat(3, 1fr); gap: 25px; }
.card { background: #fff; padding: 25px; border-radius: 18px; height: 480px; box-shadow: 0 5px 15px rgba(0,0,0,0.04); display: flex; flex-direction: column; }
.card h3 { font-size: 19px; font-weight: 700; margin-bottom: 20px; color: #333; border-left: 5px solid #36A2EB; padding-left: 12px; }
.canvas-wrapper { height: 360px; width: 100%; position: relative; }
.image-display-container { display: flex; justify-content: center; align-items: center; cursor: pointer; flex: 1; position: relative; overflow: hidden; border-radius: 12px; border: 1px solid #eee; }
.route-map-img-fixed { width: 100%; height: 100%; object-fit: cover; }
.hover-info { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); color: #fff; display: flex; align-items: center; justify-content: center; opacity: 0; transition: 0.3s; font-weight: bold; }
.image-display-container:hover .hover-info { opacity: 1; }
.no-data-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(249, 249, 249, 0.9); display: flex; align-items: center; justify-content: center; color: #aaa; font-size: 14px; border-radius: 12px; z-index: 5; }
</style>