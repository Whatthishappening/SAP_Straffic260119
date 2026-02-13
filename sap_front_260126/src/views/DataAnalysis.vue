<template>
  <div class="analysis-container">
    <div class="analysis-wrapper">
      <div class="dashboard-minimal-info">
        <span class="live-tag">● LIVE MONITORING</span>
        <span class="report-name">지하철 장애 이슈 분석 리포트</span>
      </div>

      <div class="report-area">
        <a :href="powerBIUrl" target="_blank" rel="noopener noreferrer" class="report-link">
          <img src="@/assets/통계분석.gif" alt="통계분석 대시보드" class="report-img" />
          
          <div class="execution-overlay">
            <div class="play-btn-wrapper">
              <div class="play-icon"></div>
              <span>리포트 분석 도구 실행</span>
            </div>
          </div>
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
const powerBIUrl = "https://app.powerbi.com/view?r=YOUR_REPORT_LINK_HERE";
</script>

<style scoped>
.analysis-container {
  padding: 20px;
  background-color: transparent; /* 배경 동화 */
  display: flex;
  justify-content: center;
}

.analysis-wrapper {
  width: 100%;
  max-width: 1300px;
  /* 테두리, 그림자 삭제 */
  background: transparent;
}

.dashboard-minimal-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
  padding-left: 5px;
}

.live-tag {
  color: #2ea44f;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1px;
}

.report-name {
  color: #666;
  font-size: 14px;
}

/* 리포트 이미지 영역 - 테두리 없이 깔끔하게 */
.report-area {
  position: relative;
  overflow: hidden;
  border-radius: 8px; /* 이미지만 살짝 둥글게 */
}

.report-link {
  display: block;
  position: relative;
}

.report-img {
  width: 100%;
  height: auto;
  display: block;
  transition: transform 0.4s ease;
}

/* 마우스 올렸을 때 효과 */
.execution-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1); /* 너무 어둡지 않게 */
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(2px);
}

.play-icon {
  width: 70px;
  height: 70px;
  background: #f2c811;
  border-radius: 50%;
  position: relative;
  box-shadow: 0 8px 20px rgba(0,0,0,0.2);
}

.play-icon::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 55%;
  transform: translate(-50%, -50%);
  border-left: 20px solid #000;
  border-top: 12px solid transparent;
  border-bottom: 12px solid transparent;
}

.play-btn-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  color: #000; /* 텍스트 검정색으로 변경해서 더 깔끔하게 */
}

.play-btn-wrapper span {
  font-size: 18px;
  font-weight: 800;
  background: white;
  padding: 5px 15px;
  border-radius: 20px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.report-link:hover .execution-overlay {
  opacity: 1;
}

.report-link:hover .report-img {
  transform: scale(1.01); /* 아주 살짝 확대되는 느낌만 */
}
</style>