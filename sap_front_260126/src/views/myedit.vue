<template>
  <div class="mypage-wrapper">
    <div class="mypage-header">
      <h2 class="title">정보 수정</h2>
      <div class="btn-group">
        <button class="save-btn" @click="update_user">저장</button>
        <button class="cancel-btn" @click="$emit('change-view', 'my')">취소</button>
      </div>
    </div>

    <div class="info-section">
      <h3 class="section-subtitle">- 직원 정보 수정</h3>
      <div class="info-container">
        <ul class="user-info-list">
          <li>
            <span class="bullet">●</span>
            <span class="label">이름 :</span>
            <input type="text" class="edit-input readonly" v-model="user_name" readonly />
          </li>
          <li>
            <span class="bullet">●</span>
            <span class="label">ID :</span>
            <input type="text" class="edit-input readonly" v-model="user_id" readonly />
          </li>
          <li>
            <span class="bullet">●</span>
            <span class="label" style="color: #e74c3c;">현재 PW 확인 :</span>
            <input type="password" class="edit-input" v-model="current_pw" placeholder="변경 시에만 입력하세요" />
        
           </li>
          <li v-if="current_pw.trim() !== ''">
  <span class="bullet">●</span>
  <span class="label">새 PW :</span>
  <div class="pw-input-wrapper">
    <input type="password" class="edit-input" v-model="user_pw" placeholder="본인 확인용 비밀번호 입력" />
    <p class="pw-guide">
      * 8~16자 이내, 영문/숫자/특수문자 조합 (ID와 동일 불가)
    </p>
  </div>
</li>
      <li>
  <span class="bullet">●</span>
  <span class="label">E-mail :</span>
  <div class="email-row">
    <div class="email-id-box">
      <input v-model.trim="email_id" type="text" class="edit-input email-input" />
    </div>
    <span class="at">@</span>
    <div class="email-domain-box">
      <input v-if="email_domain_select === 'direct'" 
             v-model.trim="email_domain_direct" 
             type="text" class="edit-input email-input" placeholder="직접 입력" />
      <select v-else v-model="email_domain_select" class="edit-input email-select">
        <option value="naver.com">naver.com</option>
        <option value="gmail.com">gmail.com</option>
        <option value="daum.net">daum.net</option>
        <option value="direct">직접 입력</option>
      </select>
    </div>
    <button v-if="email_domain_select === 'direct'" 
            @click="email_domain_select = 'naver.com'" 
            class="reset-domain">X</button>
  </div>
</li>
          <li class="route-container">
            <div class="route-header">
              <span class="bullet">●</span>
              <span class="label">담당 노선 정보</span>
            </div>
            <div class="section-title">역 선택 (현재: {{ selectedSubway.station_name || '미선택' }})</div>
            <SubwaySelector @update-selection="handleSubwayChange" />
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import SubwaySelector from './SubwaySelector.vue';

export default {
  components: { SubwaySelector },
  data() {
    return {
      user_id: "",
      user_name: "",
      current_pw: "",
      user_pw: "",
      email_id: "",
      email_domain_select: "naver.com",
      email_domain_direct: "",
      selectedSubway: { line_name: '', station_id: '', station_name: '' },
      originData: null // 수정 여부 판단용 원본 저장소
    };
  },
  watch: {
    current_pw(newVal) {
      if (newVal.trim() === "") {
        this.user_pw = "";
      }
    }
  },

  computed: {
    fullEmail() {
      const domain = this.email_domain_select === 'direct' 
                     ? this.email_domain_direct 
                     : this.email_domain_select;
      return this.email_id && domain ? `${this.email_id}@${domain}` : '';
    }
  },

  mounted() {
    const sessionData = sessionStorage.getItem("user_info") || sessionStorage.getItem("login");
    const login = JSON.parse(sessionData);

    if (!login) {
      alert("로그인이 필요합니다.");
      this.$emit('change-view', 'Home');
      return;
    }

    // 1. 먼저 데이터를 화면 변수에 싹 채웁니다.
    this.user_id = login.user_id || login.id || '';
    this.user_name = login.user_name || login.name || '';
    
    const fullEmail = login.user_email || login.email || '';
    if (fullEmail.includes('@')) {
      const parts = fullEmail.split('@');
      this.email_id = parts[0];
      const domain = parts[1];
      const commonDomains = ['naver.com', 'gmail.com', 'daum.net'];
      if (commonDomains.includes(domain)) {
        this.email_domain_select = domain;
      } else {
        this.email_domain_select = 'direct';
        this.email_domain_direct = domain;
      }
    }

    this.selectedSubway.line_name = login.line_name || ''; 
    this.selectedSubway.station_name = login.station_name || '';
    this.selectedSubway.station_id = login.station_id || '';

    // 2. ★데이터가 다 채워진 '후'에 현재 상태를 원본으로 딱 저장합니다!
    this.saveOrigin();
  },

  methods: {
    handleSubwayChange(data) {
      this.selectedSubway.line_name = data.line_name;
      this.selectedSubway.station_id = data.station.station_id;
      this.selectedSubway.station_name = data.station.station_name;
    },

    // 원본 데이터를 캡처하는 함수
    saveOrigin() {
      this.originData = JSON.stringify({
        email_id: this.email_id,
        email_domain_select: this.email_domain_select,
        email_domain_direct: this.email_domain_direct,
        line_name: this.selectedSubway.line_name,
        station_id: this.selectedSubway.station_id
      });
    },

    async update_user() {
      // 1. 수정 내용이 있는지 체크 (비번 입력 X + 다른 정보 원본과 동일)
      const currentData = JSON.stringify({
        email_id: this.email_id,
        email_domain_select: this.email_domain_select,
        email_domain_direct: this.email_domain_direct,
        line_name: this.selectedSubway.line_name,
        station_id: this.selectedSubway.station_id
      });

      if (this.user_pw.trim() === "" && this.originData === currentData) {
        alert("수정된 내용이 없습니다.");
        return;
      }

      // 2. 비밀번호 유효성 검사 함수 정의
      const validatePassword = (pw, id, currentPw) => {
        if (pw.length < 8 || pw.length > 16) {
          alert("비밀번호는 8자 이상 16자 이하로 설정해주세요.");
          return false;
        }
        if (pw === currentPw) {
          alert("이전 비밀번호와 똑같은 건 비밀번호 변경불가입니다.");
          return false;
        }
        if (pw === id) {
          alert("아이디와 동일한 비밀번호는 사용할 수 없습니다.");
          return false;
        }
        const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
        if (!passwordRegex.test(pw)) {
          alert("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.");
          return false;
        }
        return true;
      };

      // 3. 필수 입력값 체크
      if (!this.selectedSubway.line_name || this.selectedSubway.line_name === "미선택") {
        alert("노선을 선택해주세요.");
        return;
      }
      if (!this.selectedSubway.station_name || this.selectedSubway.station_name === "미선택") {
        alert("역을 선택해주세요.");
        return;
      }

      const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{3,}$/;
      if (!emailPattern.test(this.fullEmail)) {
        alert("이메일 주소 형식이 잘못되었습니다.");
        return; 
      }

      // 4. 전송 데이터 구성
      const updateData = {
        user_id: this.user_id,
        user_email: this.fullEmail,
        line_name: this.selectedSubway.line_name,
        station_id: this.selectedSubway.station_id,
        station_name: this.selectedSubway.station_name,
        passwordChange: false
      };
   
      // 5. 비번 변경 시 시도
      if (this.user_pw.trim() !== "") {
        if (!this.current_pw.trim()) {
          alert("현재 비밀번호를 입력해주세요.");
          return;
        }
        if (!validatePassword(this.user_pw, this.user_id, this.current_pw)) {
          return;
        }
        updateData.user_pw = this.user_pw;
        updateData.current_pw = this.current_pw;
        updateData.passwordChange = true;
      }

      // 6. 서버 전송
      try {
        const resp = await axios.post('http://localhost:9000/update_user', updateData);
        if (resp.data === "PW_ERROR") {
          alert('현재 비밀번호가 틀렸습니다.');
          this.current_pw = "";
        } else if (resp.data === "YES" || resp.data === 1) {
          alert('수정 완료!');
          if (updateData.passwordChange) {
            sessionStorage.clear();
            window.location.href = "/";
          } else {
            this.updateSession(updateData);
            this.$emit('change-view', 'my');
            setTimeout(() => { window.location.reload(); }, 100);
          }
        }
      } catch (err) {
        console.error(err);
        alert('통신 오류 발생!');
      }
    },
    updateSession(data) {
      const key = sessionStorage.getItem("user_info") ? "user_info" : "login";
      const old = JSON.parse(sessionStorage.getItem(key));
      const updated = { ...old, ...data };
      sessionStorage.setItem(key, JSON.stringify(updated));
    }
  }
}
</script>

<style scoped>
/* 기존 스타일 그대로 유지 */
.section-title { font-size: 13px; margin: 14px 0 6px; color: #666; }
.mypage-wrapper { width: 100%; background-color: #ffffff; color: #333; }
.mypage-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 25px; background-color: #eeeeee; border-bottom: 1px solid #ddd; }
.title { font-size: 20px; font-weight: 700; }
.btn-group { display: flex; gap: 8px; }
.save-btn { background-color: #3f417e; color: #fff; border: none; padding: 5px 22px; border-radius: 20px; cursor: pointer; }
.cancel-btn { background-color: #999; color: #fff; border: none; padding: 5px 22px; border-radius: 20px; cursor: pointer; }
.info-section { padding: 30px; }
.section-subtitle { font-size: 18px; font-weight: 700; margin-bottom: 25px; }
.user-info-list { list-style: none; padding: 0; }
.user-info-list li { 
  display: flex; 
  align-items: center; 
  margin-bottom: 28px; 
  font-size: 16px; 
}


.pw-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 10px; 
}
.pw-guide {
  font-size: 12px;
  color: #e74c3c;
  margin: 0;
  padding-left: 2px;
}


.email-row {
  display: flex;
  align-items: center;
  gap: 10px; 
}


.route-container { 
  flex-direction: column; 
  align-items: flex-start !important; 
  margin-top: 35px; 
  border-top: 1px solid #f5f5f5; 
  padding-top: 20px;
}
.bullet { font-size: 8px; margin-right: 12px; }
.label { 
  font-weight: 700; 
  min-width: 130px; 
  margin-right: 15px; 
}
.edit-input { padding: 5px 10px; border: 1px solid #ccc; border-radius: 4px; width: 250px; font-size: 15px; }
.edit-input.readonly { background-color: #f5f5f5; border: 1px solid #eee; color: #888; cursor: not-allowed; }
.route-container { flex-direction: column; align-items: flex-start !important; margin-top: 10px; }
.route-header { display: flex; align-items: center; margin-bottom: 10px; }
</style>