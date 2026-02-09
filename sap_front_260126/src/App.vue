<template>
  <div v-if="!isLoggedIn" class="login-fullscreen-container">
    <Login v-if="currentView === 'Login'" @login-success="handleLoginSuccess" @go-regi="currentView = 'Regi'" />
    <Regi v-if="currentView === 'Regi'" @go-login="currentView = 'Login'" />
  </div>

  <div v-else class="layout" :class="'line_name-' + userData.line_name">
    <header class="header">
      <div class="header-left-col" @click="handleViewChange('Home')">
        <div class="logo-wrapper">
          <img :src="logo" alt="Logo" class="company-logo" />
        </div>
      </div>
      
      <div class="header-right-col">
        <div class="header-status-row">
          <div class="user-profile-box">
            <div class="user-details-row">
              <span class="u-name">{{ userData.name }} 관리자</span>
              <span class="u-sep"> | </span>
              <span class="u-station">{{ userData.station_name.replace(/역$/, '') }}역</span>
              <span class="u-sep"> | </span>
              <button @click="logout" class="logout-small-btn">로그아웃</button>
              <span class="user-avatar" @click="handleViewChange('my')" style="cursor:pointer">
                <img :src="mypageimage" alt="mypage" class="header-mypageimage" />
              </span>
            </div>
          </div>
        </div>
        <div class="header-info-row">
          <div class="sub-nav-text">
            <span class="dot">●</span> 실시간 관리 시스템 접속 중 : {{ userData.line_name }} 모니터링
          </div>
        </div>
      </div>
    </header>

    <div class="container">
      <aside class="sidebar">
        <nav>
          <div class="nav-item" @click="handleViewChange('Home')" :class="{ active: currentView === 'Home' }">▶대시보드</div>
          <div class="nav-item" @click="handleViewChange('statusstation')" :class="{ active: currentView === 'statusstation' }">▶역별현황</div>
          <div class="nav-item" @click="handleViewChange('DataAnalysis')" :class="{ active: currentView === 'DataAnalysis' }">▶통계 분석</div>
          <div class="nav-item" @click="handleViewChange('insident')" :class="{ active: currentView === 'insident' }">▶장애/이슈</div>
          <div v-if="Number(userData.auth) === 1" class="nav-item" @click="handleViewChange('management')" :class="{ active: currentView === 'management' }">
            ▶사용자 관리
          </div>
        </nav>
      </aside>

      <main class="content">
        <component 
          :is="views[currentView]" 
          :key="currentView" 
          :data="selectedIncidentId"  
          @change-view="handleViewChange" 
          @go-create="handleViewChange('Createinsident')" 
          @go-list="handleViewChange('insident')"
        />
      </main>
    </div>

    <footer class="footer">
      <div class="footer-content">
        <img :src="logofooter" alt="footer logo" class="footer-inline-logo" />
        <span class="copyright">
          <table class="footer-info-table">
            <tbody>
              <tr>
                <td class="f-name">배영환</td>
                <td class="f-sep"> | </td>
                <td class="f-role">Project Manager</td>
                <td class="f-sep"> | </td>
                <td class="f-email">uee8351773@naver.com</td>
              </tr>
              <tr>
                <td class="f-name">김소연</td>
                <td class="f-sep"> | </td>
                <td class="f-role">Consultant</td>
                <td class="f-sep"> | </td>
                <td class="f-email">www.linkedin.com/in/souyeon-kim-735996394</td>
              </tr>
              <tr>
                <td class="f-name">송원호</td>
                <td class="f-sep"> | </td>
                <td class="f-role">Developer</td>
                <td class="f-sep"> | </td>
                <td class="f-email">dnjsghman@naver.com</td>
              </tr>
              <tr>
                <td class="f-name">오창석</td>
                <td class="f-sep"> | </td>
                <td class="f-role">Developer</td>
                <td class="f-sep"> | </td>
                <td class="f-email">dhckdtjr11@naver.com</td>
              </tr>
            </tbody>
          </table>
        </span>
      </div>
    </footer>

    <div v-if="isLoggedIn" class="virtual-scroll-viewport" ref="virtualScrollBox" @scroll="syncRealScroll">
      <div class="virtual-scroll-content" :style="{ width: virtualWidth + 'px' }"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onUnmounted } from 'vue';
import logo from './assets/로고.png'
import logofooter from './assets/로고 글자.png'
import mypageimage from './assets/마이페이지(W).png'
import Home from './views/HomeView.vue';
import statusstation from './views/statusstation.vue';
import management from './views/management.vue';
import Login from './views/LoginView.vue';
import Regi from './views/regi.vue';
import myedit from './views/myedit.vue';
import my from './views/my.vue';
import insident from './views/insident.vue';
import Createinsident from './views/Createinsident.vue';
import IssueDetail from './views/IssueDetail.vue';
import DataAnalysis from './views/DataAnalysis.vue';

const views = { Home, statusstation, management, Login, Regi, myedit, my, insident, Createinsident, IssueDetail, DataAnalysis};
const currentView = ref('Login');
const isLoggedIn = ref(false);
const selectedIncidentId = ref(null);

// 가상 스크롤바 관련 상태
const virtualScrollBox = ref(null);
const virtualWidth = ref(1200);

const userData = reactive({
  id: '',
  name: '',
  line_name: 'default',
  station_name: '',
  auth: null
});

// 가상 스크롤바 제어 로직
const syncRealScroll = (e) => {
  document.documentElement.scrollLeft = e.target.scrollLeft;
  document.body.scrollLeft = e.target.scrollLeft;
};

const updateVirtualWidth = () => {
  const layoutEl = document.querySelector('.layout');
  if (layoutEl) {
    virtualWidth.value = layoutEl.scrollWidth;
  }
};

const handleWindowScroll = () => {
  if (virtualScrollBox.value) {
    virtualScrollBox.value.scrollLeft = window.scrollX;
  }
};

const handleViewChange = (viewName, payload = null) => {
  currentView.value = viewName;
  selectedIncidentId.value = payload;
  history.pushState({ view: viewName, id: payload }, '');
  sessionStorage.setItem("last_view", viewName);
  if (payload) {
    sessionStorage.setItem("last_view_data", JSON.stringify(payload));
  } else {
    sessionStorage.removeItem("last_view_data");
  }
  // 뷰 변경 시 페이지 너비가 바뀔 수 있으므로 업데이트
  nextTick(updateVirtualWidth);
};

const applyUserData = (payload) => {
  if (!payload || (!payload.id && !payload.user_id)) return false;
  userData.id = payload.user_id || payload.id || '';
  userData.name = payload.user_name || payload.name || '';
  userData.line_name = payload.line_name || 'default';
  userData.station_name = payload.station_name || '';
  userData.auth = payload.auth;
  isLoggedIn.value = true;
  return true;
};

onMounted(() => {
  const savedUser = sessionStorage.getItem("user_info");
  const lastView = sessionStorage.getItem("last_view");
  const lastData = sessionStorage.getItem("last_view_data");

  if (savedUser) {
    try {
      const payload = JSON.parse(savedUser);
      if (applyUserData(payload)) {
        if (lastView && lastView !== 'Login' && lastView !== 'Regi') {
          currentView.value = lastView;
          selectedIncidentId.value = lastData ? JSON.parse(lastData) : null;
        } else {
          currentView.value = 'Home';
        }
      }
    } catch (e) {
      sessionStorage.removeItem("user_info");
    }
  }

  // 가상 스크롤바 초기화 및 이벤트 등록
  nextTick(() => {
    updateVirtualWidth();
    window.addEventListener('resize', updateVirtualWidth);
    window.addEventListener('scroll', handleWindowScroll);
  });

  window.addEventListener('popstate', (event) => {
    if (event.state) {
      currentView.value = event.state.view;
      selectedIncidentId.value = event.state.id;
    }
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', updateVirtualWidth);
  window.removeEventListener('scroll', handleWindowScroll);
});

const handleLoginSuccess = async (payload) => {
  const success = applyUserData(payload);
  if (success) {
    sessionStorage.setItem("user_info", JSON.stringify(payload));
    sessionStorage.setItem("last_view", "Home");
    await nextTick();
    currentView.value = 'Home';
    history.replaceState({ view: 'Home', id: null }, '');
    updateVirtualWidth();
  } else {
    alert("로그인 정보가 올바르지 않습니다.");
  }
};

const logout = () => {
  isLoggedIn.value = false;
  sessionStorage.clear();
  currentView.value = 'Login';
  location.reload();
};
</script>

<style>
/* 1. 기본 스타일 */
* { margin: 0; padding: 0; box-sizing: border-box; }

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
  /* 가로 스크롤은 가상 바가 담당하므로 기본 바는 숨김 */
  overflow-x: hidden; 
  overflow-y: auto !important; 
}

body {
  min-height: 100vh;
}

/* 2. 전체 레이아웃 */
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-width: 1600px; 
  width: 100%;
}

.container {
  display: flex;
  flex: 1; 
  width: 100%;
}
.header, .footer {
  width: 100%;
  min-width: 1600px;
}
/* 3. 헤더 */
.header {
  height: 160px;
  display: flex;
  background-color: #2c3e50;
  color: white;
  border-bottom: 2px solid rgba(0,0,0,0.1);
  flex-shrink: 0;
  width: 100%;
  min-width: 1200px;
}

.header-left-col {
  width: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid rgba(255,255,255,0.1);
  cursor: pointer;
}

.company-logo { width: 180px; height: auto; }
.header-right-col { flex: 1; display: flex; flex-direction: column; }
.header-status-row { flex: 1.2; display: flex; align-items: center; justify-content: flex-end; padding: 0 30px; }
.header-info-row {
  flex: 0.8;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 30px;
  background-color: rgba(0, 0, 0, 0.1);
}

.sub-nav-text { font-size: 1.2rem; font-weight: 500; }

/* 4. 사이드바 */
.sidebar {
  width: 240px;
  background-color: #f8f9fa;
  border-right: 1px solid #ddd;
  padding: 20px;
  flex-shrink: 0;
  font-size: 2.5ch;
}

.nav-item { padding: 12px; margin-bottom: 5px; cursor: pointer; border-radius: 8px; color: #555; transition: 0.2s; }

/* 5. 컨텐츠 */
.content {
  flex: 1;
  padding: 30px;
  background-color: #fcfcfc;
  min-height: 100%; 
}

/* 6. 푸터 */
.footer {
  width: 100%;
  min-width: 1200px;
  min-height: 140px;
  background-color: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  width: 100%;
}

.footer .copyright table {
  color: #666;
  font-size: 0.85rem;
  border-collapse: collapse;
  font-family: 'Pretendard', sans-serif;
  line-height: 1.6;
}

.footer .copyright td { white-space: pre; padding: 2px 0; }
.footer-inline-logo { height: 60px; }

.virtual-scroll-viewport {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 14px; 
  overflow-x: auto;
  overflow-y: hidden;
  z-index: 10000;
  background: #e0e0e0;
  border-top: 1px solid #ccc;
}
.virtual-scroll-viewport::-webkit-scrollbar-thumb {
  background: #3f417e; /* 기본 포인트 컬러 */
  border-radius: 10px;
}

html::-webkit-scrollbar { 
  width: 12px !important; /* 다시 살리기 */
  height: 12px !important; 
}
.virtual-scroll-content {
  height: 1px;
}

/* 가상 스크롤바 디자인 */
.virtual-scroll-viewport::-webkit-scrollbar {
  width: 12px;
  height: 12px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1; 
}

::-webkit-scrollbar-thumb {
  background: #888; 
  border-radius: 6px;
  border: 3px solid #f1f1f1; /* 트랙이랑 겹쳐 보이지 않게 테두리 추가 */
}

::-webkit-scrollbar-thumb:hover {
  background: #555; 
}
.virtual-scroll-viewport::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 10px;
}
.virtual-scroll-viewport:hover::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.5);
}

/* 브라우저 기본 하단 가로바는 가리기 */
html::-webkit-scrollbar { height: 0px; }

/* 8. 호선 테마 및 기타 */
.layout.line_name-1호선 .header, .layout.line_name-1호선 .footer { background-color: #2a317c !important; }
.layout.line_name-1호선 .nav-item.active { background-color: #2a317c !important; color: white !important; }

.layout.line_name-2호선 .header, .layout.line_name-2호선 .footer { background-color: #2fae35 !important; }
.layout.line_name-2호선 .nav-item.active { background-color: #2fae35 !important; color: white !important; }

.layout.line_name-3호선 .header, .layout.line_name-3호선 .footer { background-color: #ff6000 !important; }
.layout.line_name-3호선 .nav-item.active { background-color: #ff6000 !important; color: white !important; }

.layout.line_name-4호선 .header, .layout.line_name-4호선 .footer { background-color: #1a97dd !important; }
.layout.line_name-4호선 .nav-item.active { background-color: #1a97dd !important; color: white !important; }

.layout.line_name-5호선 .header, .layout.line_name-5호선 .footer { background-color: #822fe1 !important; }
.layout.line_name-5호선 .nav-item.active { background-color: #822fe1 !important; color: white !important; }

.layout.line_name-6호선 .header, .layout.line_name-6호선 .footer { background-color: #ae4908 !important; }
.layout.line_name-6호선 .nav-item.active { background-color: #ae4908 !important; color: white !important; }

.layout.line_name-7호선 .header, .layout.line_name-7호선 .footer { background-color: #636b10 !important; }
.layout.line_name-7호선 .nav-item.active { background-color: #636b10 !important; color: white !important; }

.layout.line_name-8호선 .header, .layout.line_name-8호선 .footer { background-color: #e6265b !important; }
.layout.line_name-8호선 .nav-item.active { background-color: #e6265b !important; color: white !important; }

.layout.line_name-9호선 .header, .layout.line_name-9호선 .footer { background-color: #bdb092 !important; }
.layout.line_name-9호선 .nav-item.active { background-color: #bdb092 !important; color: white !important; }

.layout[class*="line_name-"] .footer .copyright table { color: white !important; }

.login-fullscreen-container {
  width: 100vw; height: 100vh;
  display: flex; justify-content: center; align-items: center;
  background: linear-gradient(135deg, #2c3e50 0%, #000000 100%);
}

.logout-small-btn {
  background: transparent; border: 1px solid rgba(255, 255, 255, 0.5); color: white;
  padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 13px;
}

.header-mypageimage { width: 32px; height: 32px; border-radius: 50%; margin-left: 10px; vertical-align: middle; }
</style>