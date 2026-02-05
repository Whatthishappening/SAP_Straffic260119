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
            <input type="password" class="edit-input" v-model="user_pw" placeholder="본인 확인용 비밀번호 입력" />
          </li>
          <li>
            <span class="bullet">●</span>
            <span class="label">E-mail :</span>
            <input type="text" class="edit-input" v-model="user_email" />
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
      user_email: "",
      selectedSubway: { line_name: '', station_id: '', station_name: '' }
    };
  },
  watch: {
    current_pw(newVal) {
      if (newVal.trim() === "") {
        this.user_pw = "";
      }
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
    this.user_id = login.user_id || login.id || '';
    this.user_name = login.user_name || login.name || '';
    this.user_email = login.user_email || login.email || '';
    this.selectedSubway.line_name = ''; 
    this.selectedSubway.station_name = '';
    this.selectedSubway.station_id = '';

    // ★ 기존 담당 정보를 미리 넣어둠 (비밀번호만 바꿀 때 유용)
    this.selectedSubway.line_name = login.line_name || ''; 
    this.selectedSubway.station_name = login.station_name || '';
    this.selectedSubway.station_id = login.station_id || '';
 
  },
 

  
  methods: {
    handleSubwayChange(data) {
      this.selectedSubway.line_name = data.line_name;
      this.selectedSubway.station_id = data.station.station_id;
      this.selectedSubway.station_name = data.station.station_name;
    },

    
    async update_user() {
      if (!this.selectedSubway.line_name || this.selectedSubway.line_name.trim() === "" || this.selectedSubway.line_name === "미선택") {
        alert("노선을 선택해주세요.");
        return;
      }

      if (!this.selectedSubway.station_name || this.selectedSubway.station_name.trim() === "" || this.selectedSubway.station_name === "미선택") {
        alert("역을 선택해주세요.");
        return;
      }
      const updateData = {
        user_id: this.user_id,
        user_email: this.user_email,
        line_name: this.selectedSubway.line_name,
        station_id: this.selectedSubway.station_id,
        station_name: this.selectedSubway.station_name,
        passwordChange: false
      };
   
      if (this.user_pw.trim() !== "") {
        if (!this.current_pw.trim()) {
          alert("현재 비밀번호를 입력해주세요.");
          return;
        }
        updateData.user_pw = this.user_pw;
        updateData.current_pw = this.current_pw;
        updateData.passwordChange = true;
      }

      try {
        const resp = await axios.post('http://localhost:9000/update_user', updateData);
        if (resp.data === "PW_ERROR") {
          alert('비밀번호가 틀렸습니다.');
          this.current_pw = "";
        } else if (resp.data === "YES" || resp.data === 1) {
          alert('수정 완료!');
          if (updateData.passwordChange) {
            sessionStorage.clear();
            window.location.href = "/";
          } else {
            this.updateSession(updateData);
            this.$emit('change-view', 'my');
          // 약간의 시간차를 두고 새로고침 실행 (이동이 확실히 인지되도록)
            setTimeout(() => {
              window.location.reload();
            }, 100);
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
.user-info-list li { display: flex; align-items: center; margin-bottom: 18px; font-size: 16px; }
.bullet { font-size: 8px; margin-right: 12px; }
.label { font-weight: 700; min-width: 110px; }
.edit-input { padding: 5px 10px; border: 1px solid #ccc; border-radius: 4px; width: 250px; font-size: 15px; }
.edit-input.readonly { background-color: #f5f5f5; border: 1px solid #eee; color: #888; cursor: not-allowed; }
.route-container { flex-direction: column; align-items: flex-start !important; margin-top: 10px; }
.route-header { display: flex; align-items: center; margin-bottom: 10px; }
</style>