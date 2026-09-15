<template>
  <view class="page">
    <view class="card">
      <view class="title">用户注册</view>
      <view class="subtitle">创建借款端账号</view>

      <!-- 手机号 -->
      <view class="form-item">
        <view class="label">手机号</view>
        <input
          v-model="phoneNumber"
          class="input"
          type="number"
          maxlength="11"
          placeholder="请输入手机号"
        />
      </view>

      <!-- 密码 -->
      <view class="form-item">
        <view class="label">密码</view>
        <input
          v-model="password"
          class="input"
          password
          maxlength="64"
          placeholder="请输入密码（至少8位）"
        />
      </view>

      <!-- 确认密码 -->
      <view class="form-item">
        <view class="label">确认密码</view>
        <input
          v-model="confirmPassword"
          class="input"
          password
          maxlength="64"
          placeholder="请再次输入密码"
        />
      </view>

      <!-- 渠道码：第一周只预留，不校验 -->
      <view class="form-item">
        <view class="label">
          渠道码
          <text class="optional">（选填）</text>
        </view>

        <input
          v-model="channelId"
          class="input"
          placeholder="请输入渠道码，可不填写"
        />
      </view>

      <!-- 注册按钮 -->
      <button
        class="register-btn"
        :loading="loading"
        :disabled="loading"
        @click="handleRegister"
      >
        {{ loading ? '注册中...' : '注册' }}
      </button>

      <!-- 返回登录 -->
      <view class="login-link" @click="goLogin">
        已有账号？去登录
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { register } from '@/api/auth.js'

const phoneNumber = ref('')
const password = ref('')
const confirmPassword = ref('')
const channelId = ref('')
const loading = ref(false)

async function handleRegister() {
  // 1. 检查手机号
  if (!/^1\d{10}$/.test(phoneNumber.value)) {
    uni.showToast({
      title: '请输入正确的11位手机号',
      icon: 'none'
    })
    return
  }

  // 2. 检查密码
  if (!password.value) {
    uni.showToast({
      title: '请输入密码',
      icon: 'none'
    })
    return
  }

  if (password.value.length < 8) {
    uni.showToast({
      title: '密码至少需要8位',
      icon: 'none'
    })
    return
  }

  // 3. 检查两次密码
  if (password.value !== confirmPassword.value) {
    uni.showToast({
      title: '两次输入的密码不一致',
      icon: 'none'
    })
    return
  }

  // 渠道码本周不进行校验

  loading.value = true

  try {
    await register({
      phoneNumber: phoneNumber.value,
      password: password.value,

      // 沿用原注册接口需要的 userName
      userName: '用户' + phoneNumber.value.slice(-4),

      // 不填写时传 null
      channelId: channelId.value.trim() || null
    })

    uni.showToast({
      title: '注册成功',
      icon: 'success'
    })

    // 注册成功后返回登录页
    setTimeout(() => {
      uni.redirectTo({
        url: '/pages/login/login'
      })
    }, 800)
  } catch (error) {
    console.error('注册失败：', error)

    uni.showToast({
      title: error.message || '注册失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

function goLogin() {
  uni.redirectTo({
    url: '/pages/login/login'
  })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 100rpx 40rpx;
  background: #f5f3ff;
}

.card {
  padding: 50rpx 36rpx;
  background: #ffffff;
  border-radius: 30rpx;
}

.title {
  font-size: 48rpx;
  font-weight: 700;
  color: #333333;
}

.subtitle {
  margin-top: 12rpx;
  margin-bottom: 50rpx;
  font-size: 26rpx;
  color: #999999;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  margin-bottom: 14rpx;
  font-size: 28rpx;
  color: #333333;
}

.optional {
  font-size: 24rpx;
  color: #999999;
}

.input {
  width: 100%;
  height: 96rpx;
  box-sizing: border-box;
  padding: 0 28rpx;
  background: #f7f7f7;
  border-radius: 18rpx;
  font-size: 28rpx;
}

.register-btn {
  margin-top: 50rpx;
  background: #7b5cf2;
  color: #ffffff;
  border-radius: 48rpx;
  font-size: 30rpx;
}

.register-btn[disabled] {
  opacity: 0.6;
}

.login-link {
  margin-top: 40rpx;
  text-align: center;
  font-size: 28rpx;
  color: #7b5cf2;
}
</style>
