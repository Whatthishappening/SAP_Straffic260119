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
              <span class="u-station">{{ userData.station_name }}역</span>
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
          <div class="nav-item" @click="handleViewChange('analysis')" :class="{ active: currentView === 'analysis' }">▶통계 분석</div>
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
            <td class="f-sep">|</td>
            <td class="f-role">Project Manager</td>
            <td class="f-sep">|</td>
            <td class="f-email">uee8351773@naver.com</td>
          </tr>
          <tr>
            <td class="f-name">김소연</td>
            <td class="f-sep">|</td>
            <td class="f-role">Consultant</td>
            <td class="f-sep">|</td>
            <td class="f-email">www.linkedin.com/in/souyeon-kim-735996394</td>
          </tr>
          <tr>
            <td class="f-name">송원호</td>
            <td class="f-sep">|</td>
            <td class="f-role">Developer</td>
            <td class="f-sep">|</td>
            <td class="f-email">dnjsghman@naver.com</td>
          </tr>
          <tr>
            <td class="f-name">오창석</td>
            <td class="f-sep">|</td>
            <td class="f-role">Developer</td>
            <td class="f-sep">|</td>
            <td class="f-email">dhckdtjr11@naver.com</td>
          </tr>
        </tbody>
      </table>
    </span>
  </div>
</footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'; // ★ nextTick 추가됨!
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

const views = { Home, statusstation, management, Login, Regi, myedit, my, insident, Createinsident, IssueDetail };
const currentView = ref('Login');
const isLoggedIn = ref(false);
const selectedIncidentId = ref(null);

const userData = reactive({
  id: '',
  name: '',
  line_name: 'default',
  station_name: '',
  auth: null
});

// 뷰 변경 핸들러
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
};

// [수정] 성공 여부를 반환하도록 개선
const applyUserData = (payload) => {
  if (!payload || (!payload.id && !payload.user_id)) return false;
  
  // 데이터부터 싹 담기
  userData.id = payload.user_id || payload.id || '';
  userData.name = payload.user_name || payload.name || '';
  userData.line_name = payload.line_name || 'default';
  userData.station_name = payload.station_name || '';
  userData.auth = payload.auth; // 권한 담기
  
  // 그 다음 로그인 처리
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

  window.addEventListener('popstate', (event) => {
    if (event.state) {
      currentView.value = event.state.view;
      selectedIncidentId.value = event.state.id;
    }
  });
});

// [핵심 수정] 로그인 성공 로직
const handleLoginSuccess = async (payload) => {
  console.log("로그인 데이터 수신:", payload); 
  
  const success = applyUserData(payload);
  
  if (success) {
    sessionStorage.setItem("user_info", JSON.stringify(payload));
    sessionStorage.setItem("last_view", "Home");
    
    // isLoggedIn이 true로 바뀐 후 DOM이 업데이트될 때까지 기다림
    await nextTick();
    currentView.value = 'Home';
    history.replaceState({ view: 'Home', id: null }, '');
  } else {
    alert("로그인 정보가 올바르지 않습니다.");
  }
};

const logout = () => {
  isLoggedIn.value = false;
  sessionStorage.clear();
  currentView.value = 'Login';
  location.reload(); // 안전하게 새로고침으로 상태 초기화
};
</script>

<style>

/* 1. html, body 수정 */
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
  /* 가로 스크롤을 허용으로 변경! */
  overflow-x: auto !important; 
  overflow-y: auto;
}

/* --- 기본 리셋 --- */
* { margin: 0; padding: 0; box-sizing: border-box; }
body { overflow-y: auto; 
  overflow-x: hidden;  
  min-height: 100vh; }

/* 2. 전체 레이아웃 */
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-width: 1200px; 
  width: 100%;
  position: relative;
}
/* 3. 중간 영역 (사이드바 + 컨텐츠) */
.container {
  display: flex;
  flex: 1; 
  width: 100%;
  height: auto; 
}
/* --- 헤더 스타일 --- */
.header {
  height: 160px;
  display: flex;
  background-color: #2c3e50;
  color: white;
  border-bottom: 2px solid rgba(0,0,0,0.1);
  flex-shrink: 0;
}
/* 3. 헤더와 컨테이너도 너비를 따라가게 설정 */
.header, .container, .footer {
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

/* --- 사이드바 스타일 --- */
.sidebar {
  width: 240px;
  background-color: #f8f9fa;
  border-right: 1px solid #ddd;
  padding: 20px;
  flex-shrink: 0;
}
.nav-item { padding: 12px; margin-bottom: 5px; cursor: pointer; border-radius: 8px; color: #555; transition: 0.2s; }

/* 4. 실제 내용이 들어가는 영역 */
.content {
  flex: 1;
  padding: 30px;
  background-color: #fcfcfc;
  /* 컨텐츠가 길어지면 부모인 .layout을 밀어내며 길어지게 합니다 */
  min-height: 100%; 
}
/* --- 푸터 --- */
/* 1. 푸터 전체 상자의 높이와 여백 조절 */
.footer {
  width: 100%;
  min-height: 140px;      /* 높이를 넉넉하게 늘림 */
  background-color: #eee;
  display: flex;
  align-items: center;    /* 수직 가운데 정렬 */
  justify-content: center;
  padding: 10px 0;        /* 위아래에 40px씩 넉넉하게 마진(패딩) 추가! */
}
.footer-content {
  display: flex;
  align-items: center;  
  justify-content: center; 
  gap: 30px; 
  width: 100%; 
}
/* 2. 테이블을 감싸는 span 태그 설정 */
.footer .copyright {
  display: flex;
  justify-content: flex-start; 
}
/* 3. 테이블 스타일 (폰트와 정렬) */
.footer .copyright table {
  color: #666;
  font-size: 0.85rem;
  border-collapse: collapse;
  font-family: 'Pretendard', 'Courier New', monospace; 
  line-height: 1.6;
  text-align: left; 
}
.footer .copyright td {
  
  white-space: pre; 
  padding: 2px 0;
}

/* 호선 테마 적용 시 텍스트 흰색 유지 */
.layout[class*="line_name-"] .footer .copyright table {
  color: white !important;
}

/* 추가 꿀팁: 구분선(|)만 살짝 흐리게 하고 싶다면 텍스트 색상을 조절하세요 */
.footer-content { display: flex; align-items: center; gap: 20px; }
.footer-inline-logo { height: 60px; }
.footer .copyright table { color: #666; font-size: 0.8rem; }

/* --- 호선별 테마 --- */
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