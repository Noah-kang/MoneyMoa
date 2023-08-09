<template>
  <v-card class="mx-auto" max-width="344">
    <v-img
      src="https://cdn.vuetifyjs.com/images/cards/sunshine.jpg"
      class="align-end text-white"
      height="200"
      cover
    >
      <v-card-title> {{ challenge.title }} </v-card-title>
    </v-img>

    <v-card-actions>
      <v-btn color="orange-lighten-2" variant="text" @click="show = !show"> 상세보기 </v-btn>

      <v-spacer></v-spacer>
      <v-dialog v-model="dialog" width="auto">
        <template v-slot:activator="{ props }">
          <v-btn icon="mdi-pencil" v-bind="props"></v-btn>
        </template>
        <v-card>
          <v-card-title class="text-center mt-4">
            <span class="text-h5">챌린지 수정하기</span>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="title"
                    label="챌린지 이름을 설정하세요"
                    required
                    variant="solo-filled"
                  ></v-text-field>
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="goalAmount"
                    label="목표 금액을 입력하세요"
                    required
                    variant="solo-filled"
                  ></v-text-field>
                </v-col>
                <v-col cols="12">
                  <v-textarea
                    v-model="content"
                    clearable
                    label="내용을 입력하세요"
                    variant="solo-filled"
                  ></v-textarea>
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="period"
                    label="기간을 입력하세요"
                    variant="solo-filled"
                  ></v-text-field>
                </v-col>
                <v-col cols="12">
                  <!-- 파일 업로드를 위한 input 요소 추가 -->
                  <v-file-input
                    prepend-icon="mdi-camera"
                    label="이미지를 선택하세요"
                    variant="solo-filled"
                    @change="handleFileUpload"
                    class="input-btn"
                  ></v-file-input>
                </v-col>
                <v-col cols="12">
                  <!-- 사진 미리보기를 위한 img 요소 추가 -->
                  <img
                    v-if="previewImageUrl"
                    :src="previewImageUrl"
                    alt="Uploaded Image"
                    style="max-width: 100%; max-height: 400px; margin: 20px auto; display: block"
                  />
                </v-col>
              </v-row>
            </v-container>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="blue-darken-1" variant="text" @click="dialog = false"> 닫기 </v-btn>
            <v-btn
              color="blue-darken-1"
              variant="text"
              @click=";(dialog = false), updateChallenge()"
            >
              만들기
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
      <v-btn icon="mdi-delete" @click="deleteChallenge()"></v-btn>
    </v-card-actions>

    <v-expand-transition>
      <div v-show="show">
        <v-divider></v-divider>
        <v-card-text> 챌린지 내용: {{ challenge.content }} </v-card-text>
        <v-divider></v-divider>
        <v-card-text> 챌린지 종료 날짜: {{ challenge.period }} </v-card-text>
        <v-card-text> 목표 금액: {{ challenge.goalAmount }} </v-card-text>
        <v-card-text> 챌린지 시작 날짜: {{ challenge.startDate }} </v-card-text>
      </div>
    </v-expand-transition>
  </v-card>
</template>
<script setup>
import { ref } from 'vue'
import functions from '@/api/challenge.js'

const props = defineProps({
  challenge: Object
})

// 챌린지 생성을 위한 변수
const dialog = ref(false)

// 챌린지ID 저장
const challengeId = ref('')

// 챌린지 상세보기 위한 변수
const show = ref(false)

// 챌린지 삭제 API 호출
const deleteChallenge = () => {
  challengeId.value = props.challenge.id
  const res = functions.deleteChallenge(challengeId.value)
}

// 챌린지 생성 위한 변수
const title = ref('')
const content = ref('')
const period = ref('')
const goalAmount = ref('')

// 챌린지 수정 API 호출
const updateChallenge = () => {
  // 전달할 data 객체 업데이트
  const data = {
    title: title.value,
    content: content.value,
    goalAmount: parseInt(goalAmount.value),
    period: period.value
  }

  // 챌린지 수정 API 호출
  const updateChallenge = functions.updateChallenge
  challengeId.value = props.challenge.id
  updateChallenge(challengeId.value, data).then((response) => {
    console.log(response) // 응답 확인
  })
}

// 파일 미리보기를 위한 URL
const previewImageUrl = ref('')

// 파일 업로드를 처리하는 메서드
const handleFileUpload = (event) => {
  //event.target.files[0]를 통해 업로드한 파일 정보를 가져옵니다.
  const file = event.target.files[0]

  //FileReader 객체를 생성하여 파일을 읽습니다.
  const reader = new FileReader()

  //파일이 읽혔을 때 발생하는 onload 이벤트를 이용하여,
  //파일의 미리보기를 위한 base64 형태의 URL을 생성하고, previewImageUrl에 저장합니다.
  reader.onload = () => {
    previewImageUrl.value = reader.result
  }

  if (file) {
    // 파일을 읽어오고, onload 이벤트가 발생하여 미리보기를 업데이트합니다.
    reader.readAsDataURL(file)
  }
}
</script>
<style></style>