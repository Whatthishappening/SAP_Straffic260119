<template>
  <div class="join-bg">
    <div class="join-card">
      <div class="logo-row">
        <img src="../assets/로고.png" class="logo-img" />
      </div>

      <div class="join-title">회원가입</div>

      <div class="input-box">
        <input v-model.trim="add_name" type="text" placeholder="이름" />
      </div>

      <div class="input-box">
        <input v-model.trim="add_id" type="text" placeholder="아이디" />
        <button @click="check_id" class="check-btn">중복확인</button>
      </div>

      <div class="email-row">
        <div class="input-box email-id">
          <input v-model.trim="email_id" type="text" placeholder="이메일" />
        </div>
        <span class="at">@</span>
        <div class="input-box email-domain">
          <input v-if="email_domain_select === 'direct'" 
                 v-model.trim="email_domain_direct" 
                 type="text" placeholder="도메인 입력" />
          <select v-else v-model="email_domain_select">
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

      <div class="input-box">
        <input v-model="add_pw" type="password" placeholder="비밀번호" />
      </div>
      <div class="input-box">
        <input v-model="check_pw" type="password" placeholder="비밀번호 확인" />
      </div>

      <div class="section-title">역 선택</div>
      <SubwaySelector @update-selection="handleSubwayChange" />

      <div class="btn-group">
        <button 
          class="join-btn" 
          :disabled="!isInputComplete"
          :class="{ 'btn-disabled': !isInputComplete }"
          @click="adduser"
        >
          가입
        </button>
        <button class="cancel-btn" @click="$emit('go-login')">취소</button>
      </div>
    </div>
  </div>
</template>

<script>
import SubwaySelector from './SubwaySelector.vue';
import axios from 'axios';

export default {
  name: "regi",
  components: { SubwaySelector },
  data() {
    return {
      add_name: '',
      add_id: '',
      email_id: '',
      email_domain_select: 'naver.com',
      email_domain_direct: '',
      add_pw: '',
      check_pw: '',
      selectedSubway: { line_name: '', station_id: '', station_name: '' },
      isIdChecked: false
    }
  },
  watch: {
    add_id() { this.isIdChecked = false; }
  },
  computed: {
    fullEmail() {
      const domain = this.email_domain_select === 'direct' 
                     ? this.email_domain_direct 
                     : this.email_domain_select;
      return this.email_id && domain ? `${this.email_id}@${domain}` : '';
    },
    isInputComplete() {
      return (
        this.add_name.trim() !== '' &&
        this.add_id.trim() !== '' &&
        this.email_id.trim() !== '' &&
        (this.email_domain_select !== 'direct' || this.email_domain_direct.trim() !== '') &&
        this.add_pw.trim() !== '' &&
        this.check_pw.trim() !== '' &&
        this.selectedSubway.station_id !== ''
      );
    }
  },
  methods: {
    handleSubwayChange(data) {
      this.selectedSubway.line_name = data.line_name;
      this.selectedSubway.station_id = data.station.station_id;
      this.selectedSubway.station_name = data.station.station_name;
    },
    check_id() {
      const cleanId = this.add_id.replace(/\s/g, '');
      this.add_id = cleanId;
      if (!cleanId) { alert('아이디를 입력해주세요'); return; }

      axios.post('http://localhost:9000/check_userid', null, { 
        params: { user_id: cleanId } 
      }).then(resp => {
        if (resp.data === "YES") {
          alert('사용 가능한 아이디입니다');
          this.isIdChecked = true; 
        } else {
          alert('중복된 아이디입니다');
          this.add_id = "";
          this.isIdChecked = false;
        }
      }).catch(err => { alert(err); });
    },
    adduser() {
      // 1. 중복 확인 체크
      if (this.isIdChecked === false) {
        alert("아이디 중복 확인을 해주세요.");
        return;
      }

      // 2. 이메일 형식 정규식 검사 (google.co 같은 형태 차단)
      // emailPattern: 끝마디가 최소 3글자 이상인 경우 (.com, .net 등)
      // isCountryCode: .2글자.2글자 형태인 경우 (.co.kr, .or.kr 등)
      const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{3,}$/;
      const isCountryCode = /\.[a-zA-Z]{2}\.[a-zA-Z]{2}$/.test(this.fullEmail);

      if (!emailPattern.test(this.fullEmail) && !isCountryCode) {
        alert("이메일 주소 형식이 잘못되었습니다.\n다시 확인해주세요.");
        return; // 값 유지하며 중단
      }

      // 3. 비밀번호 일치 확인
      if (this.add_pw !== this.check_pw) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
      }

      // 전송용 데이터 정리
      const cleanName = this.add_name.replace(/\s/g, '');
      const cleanId = this.add_id.replace(/\s/g, '');

      let param = { params: {
        user_name: cleanName,
        user_id: cleanId,
        user_email: this.fullEmail,
        user_pw: this.add_pw,
        line_name: this.selectedSubway.line_name,
        station_id: this.selectedSubway.station_id,
        station_name: this.selectedSubway.station_name
      }};

      axios.post('http://localhost:9000/add_regi', null, param)
        .then(resp => {
          if (resp.data === "YES") {
            alert('가입신청이 완료되었습니다. 승인을 기다려주세요.');
            this.$emit('go-login');
          } else { alert('회원가입 실패'); }
        }).catch(err => { alert(err); });
    }
  }
}
</script>

<style scoped>
.join-bg { position: fixed; inset: 0; background: url("../assets/background.png") center / cover no-repeat; display: flex; justify-content: center; align-items: center; }
.join-card { width: 400px; background: linear-gradient(180deg, #3b5bff 0%, #2436b8 100%); border-radius: 12px; padding: 15px 25px 30px; color: #fff; box-shadow: 0 20px 40px rgba(0,0,0,0.45); }
.logo-row { text-align: center; }
.logo-img { width: 280px; height: auto; }
.join-title { text-align: center; font-size: 20px; font-weight: 700; margin-bottom: 16px; }

.input-box { background: #f4f6ff; border-radius: 8px; padding: 0 12px; margin-bottom: 10px; display: flex; align-items: center; height: 45px; }
.input-box input { border: none; outline: none; background: transparent; width: 100%; color: #333; height: 100%; font-size: 14px; }

.check-btn { 
  font-size: 11px; border: none; background: #dfe6ff; border-radius: 6px; padding: 0 10px; 
  cursor: pointer; color: #3b5bff; font-weight: 700; white-space: nowrap; height: 28px; margin-left: 8px;
}

.email-row { display: flex; align-items: center; gap: 6px; margin-bottom: 10px; }
.email-id { flex: 1; margin-bottom: 0 !important; }
.email-domain { flex: 1.3; margin-bottom: 0 !important; }
.email-domain select { width: 100%; border: none; background: transparent; outline: none; color: #333; font-size: 14px; height: 100%; }
.at { color: #fff; font-weight: bold; }
.reset-domain { background: #dfe6ff; border: none; border-radius: 4px; color: #3b5bff; cursor: pointer; padding: 5px 8px; font-size: 10px; white-space: nowrap; }

.section-title { font-size: 13px; margin: 14px 0 6px; }
.btn-group { display: flex; gap: 10px; margin-top: 18px; }
.join-btn { flex: 1; height: 42px; background: linear-gradient(180deg, #5f8bff 0%, #3f6bff 100%); border: none; border-radius: 8px; color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn-disabled { background: #aab6ff !important; cursor: not-allowed !important; opacity: 0.7; }
.cancel-btn { flex: 1; height: 42px; background: #dfe6ff; border: none; border-radius: 8px; color: #3b5bff; font-size: 14px; font-weight: 600; cursor: pointer; }
</style>