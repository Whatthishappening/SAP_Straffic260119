<template>
  <div class="analysis-container">
    <div class="analysis-card">
      <div class="analysis-header">
        <div class="header-left">
          <h2>지하철 운영현황 통계분석</h2>
          <span class="guide-text">※ 이미지를 클릭하면 실시간 Power BI 대시보드로 이동합니다.</span>
        </div>
      </div>
      
      <div class="image-wrapper">
        <a :href="powerBIUrl" target="_blank" rel="noopener noreferrer" class="analysis-link">
          <img src="@/assets/통계분석.png" alt="통계분석 대시보드" class="analysis-img" />
          <div class="hover-overlay">
            <div class="overlay-content">
              <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                <polyline points="15 3 21 3 21 9"></polyline>
                <line x1="10" y1="14" x2="21" y2="3"></line>
              </svg>
              <span>Power BI 리포트 열기</span>
            </div>
          </div>
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * Power BI 게시 후 생성된 '웹에 게시' 또는 '보고서 링크' URL을 아래에 입력하세요.
 */
const powerBIUrl = "https://app.powerbi.com/view?r=YOUR_REPORT_LINK_HERE";
</script>

<style scoped>
.analysis-container {
  padding: 30px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 60px);
}

.analysis-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  max-width: 1200px;
  margin: 0 auto;
}

.analysis-header {
  padding: 25px 35px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left h2 {
  margin: 0;
  font-size: 22px;
  color: #1a1a1a;
  letter-spacing: -0.5px;
}

.guide-text {
  font-size: 14px;
  color: #888;
  margin-top: 5px;
  display: block;
}

.image-wrapper {
  padding: 30px;
  display: flex;
  justify-content: center;
  background-color: #ffffff;
}

.analysis-link {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eaeaea;
  transition: all 0.3s ease;
}

.analysis-img {
  display: block;
  max-width: 100%;
  height: auto;
  transition: transform 0.5s ease;
}

.hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.overlay-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  color: #fff;
}

.overlay-content span {
  font-size: 18px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.analysis-link:hover .hover-overlay {
  opacity: 1;
}

.analysis-link:hover .analysis-img {
  transform: scale(1.03);
}

.analysis-link:hover {
  box-shadow: 0 10px 25px rgba(0,0,0,0.15);
  border-color: #f2c811;
}
</style>