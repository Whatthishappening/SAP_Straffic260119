<template>
  <div class="login-bg">
    <div class="login-card">

      <div class="logo-row">
        <img src="../assets/로고.png" class="logo-img" />
      </div>

      <div class="login-title">로그인</div>

      <div class="input-box">
        <span class="icon">👤</span>
        <input v-model.trim="user_id" placeholder="아이디" />
      </div>

      <div class="input-box">
        <span class="icon">🔒</span>
        <input v-model="user_pw" type="password" placeholder="비밀번호" @keyup.enter="login" />
      </div>

      <div class="save-id">
        <input type="checkbox" v-model="save_id" @click="saveId"/>
        <span>ID 저장</span>
      </div>

      <button class="login-btn" @click="login">로그인</button>

      <div class="join-text">
        <p @click="$emit('go-regi')" style="cursor:pointer">회원가입</p>
      </div>

    </div>
  </div>
</template>

<script>
import { useCookies } from 'vue3-cookies';
const { cookies } = useCookies();
import axios from 'axios'

export default {
  data() {
    return {
      user_id: '',
      user_pw: '',
      save_id: false,
      user_email: ''
    }
  },
  mounted() {
    let userId = cookies.get('userId');
    if (userId !== null) {
      this.save_id = true;
      this.user_id = userId;
    } else {
      this.save_id = false;
      this.user_id = "";
    }
  },
  methods: {
    saveId() {
      // 저장 시에도 공백이 제거된 상태로 저장되도록 처리
      const cleanId = this.user_id.replace(/\s/g, '');
      if (this.save_id === false && cleanId !== "") {
        cookies.set("userId", cleanId, "7d");
      } else {
        cookies.remove("userId");
      }
    },

    login() {
      // 아이디에서 모든 공백 제거 (중간 공백 포함)
      const cleanId = this.user_id.replace(/\s/g, '');
      
      if (!cleanId || !this.user_pw) {
        alert("아이디와 비밀번호를 입력해주세요.");
        return;
      }

      let param = {
        params: {
          user_id: cleanId, 
          user_pw: this.user_pw,
        }
      }
      
      axios.post('http://localhost:9000/check_login', null, param)
        .then(resp => {
          let user = resp.data;

          if (!user || user.user_id === undefined) {
            alert('아이디 또는 비밀번호가 틀렸습니다');
            return;
          }

          // [성공 시 아이디 쿠키 갱신] 공백 제거된 버전으로 저장
          if (this.save_id) {
            cookies.set("userId", cleanId, "7d");
          }

          if (user.auth === '3') {
            alert('대기중 계정입니다. 관리자에게 문의하세요.');
            return;
          }
          if (user.auth === '4') {
            alert('비활성화된 계정입니다. 관리자에게 문의하세요.');
            return;
          }

          sessionStorage.setItem("login", JSON.stringify(user));

          this.$emit('login-success', {
            id: user.user_id,          
            name: user.user_name,      
            line_name: user.line_name,
            station_name: user.station_name,
            user_email: user.user_email
          });

          this.$router.push('home');
        })
        .catch(err => {
          console.error(err);
          alert('서버 연결에 실패했습니다.');
        })
    },
  },
}
</script>

<style scoped>
.login-bg { position: fixed; inset: 0; background: url("../assets/background.png") center / cover no-repeat; display: flex; justify-content: center; align-items: center; }
.login-card { width: 350px; background: linear-gradient(180deg, #3b5bff 0%, #2436b8 100%); border-radius: 12px; padding: 5px 20px 26px; box-shadow: 0 20px 40px rgba(0,0,0,0.45); color: #fff; }
.logo-row { text-align: center; }
.logo-img { width: 300px; height: auto; }
.login-title { text-align: center; font-size: 20px; font-weight: 700; margin-bottom: 10px; }
.input-box { display: flex; align-items: center; gap: 8px; background: #f4f6ff; border-radius: 8px; padding: 10px 12px; margin-bottom: 10px; }
.input-box input { border: none; outline: none; background: transparent; width: 100%; font-size: 13px; color: #333; }
.save-id { display: flex; align-items: center; gap: 6px; font-size: 15px; margin: 8px 0 14px; }
.login-btn { width: 100%; height: 40px; background: linear-gradient(180deg, #5f8bff 0%, #3f6bff 100%); border: none; border-radius: 8px; color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; }
.join-text { margin-top: 12px; text-align: center; font-size: 12px; opacity: 0.85; }
</style>