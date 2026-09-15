<template>
  <view class="page">
    <view class="hero">
      <text class="logo">闪借</text>
      <text class="sub">互联网个人贷款 · 安卓客户端</text>
    </view>

    <view class="card">
      <text class="label">手机号</text>

      <input
        v-model="phone"
        class="input"
        type="number"
        maxlength="11"
        placeholder="11位手机号"
      />

      <text class="label">密码</text>

      <input
        v-model="password"
        class="input"
        password
        placeholder="登录密码"
      />

      <button
        class="btn"
        :loading="loading"
        :disabled="loading"
        @click="login"
      >
        登录
      </button>

      <button
        class="btn ghost"
        @click="fillDemo"
      >
        演示账号
      </button>

      <view
        class="register-link"
        @click="goRegister"
      >
        没有账号？去注册
      </view>
    </view>
  </view>
</template>

<script>
import request, { setToken } from '@/utils/api.js'

export default {
  data() {
    return {
      phone: '',
      password: '',
      loading: false
    }
  },

  onLoad() {
    const token = uni.getStorageSync('token')

    if (token) {
      uni.reLaunch({
        url: '/pages/home/home'
      })
    }
  },

  methods: {
    fillDemo() {
      this.phone = '13800138000'
      this.password = '123456'
    },

    goRegister() {
      uni.navigateTo({
        url: '/pages/register/register'
      })
    },

    async login() {
      if (!this.phone || !this.password) {
        uni.showToast({
          title: '请填写账号密码',
          icon: 'none'
        })
        return
      }

      if (!/^1\d{10}$/.test(this.phone)) {
        uni.showToast({
          title: '请输入正确的11位手机号',
          icon: 'none'
        })
        return
      }

      this.loading = true

      try {
        const data = await request({
          url: '/api/users/login',
          method: 'POST',

          data: {
            phoneNumber: this.phone,
            password: this.password
          },

          // 登录接口本身不携带 token
          auth: false
        })

        const result = data?.data || data

        const token =
          result?.token ||
          result?.accessToken ||
          result?.access_token

        if (!token) {
          throw new Error('登录成功，但后端未返回 token')
        }

        setToken(token)

        if (result?.userInfo) {
          uni.setStorageSync(
            'userInfo',
            result.userInfo
          )
        }

        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })

        setTimeout(() => {
          uni.reLaunch({
            url: '/pages/home/home'
          })
        }, 400)

      } catch (e) {
        console.error('登录失败：', e)

        uni.showToast({
          title: e.message || '登录失败',
          icon: 'none',
          duration: 3000
        })

      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 48rpx 32rpx;
  box-sizing: border-box;
  background: linear-gradient(
    180deg,
    #7b5cf2 0%,
    #f5f1ff 45%
  );
}

.hero {
  text-align: center;
  padding: 60rpx 0 40rpx;
  color: #fff;
}

.logo {
  font-size: 64rpx;
  font-weight: 700;
  display: block;
}

.sub {
  font-size: 26rpx;
  opacity: 0.9;
  margin-top: 12rpx;
  display: block;
}

.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(88, 58, 196, 0.12);
}

.label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
  margin-top: 16rpx;
}

.label:first-child {
  margin-top: 0;
}

.input {
  background: #f8f6ff;
  border-radius: 16rpx;
  padding: 20rpx;
  font-size: 28rpx;
}

.btn {
  margin-top: 32rpx;
  background: linear-gradient(135deg, #7b5cf2, #9b7bff);
  color: #fff;
  border-radius: 999rpx;
  font-size: 30rpx;
}

.btn.ghost {
  background: #f0ebff;
  color: #7b5cf2;
  margin-top: 16rpx;
}

.register-link {
  margin-top: 32rpx;
  text-align: center;
  color: #7b5cf2;
  font-size: 28rpx;
}
</style>
