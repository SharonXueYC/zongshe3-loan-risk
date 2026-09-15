<template>
  <view class="page">
    <view class="success">登录成功</view>
    <view class="tip">第一周验收用首页</view>

    <button class="logout-btn" @click="logout">
      退出登录
    </button>
  </view>
</template>

<script>
export default {
  methods: {
    logout() {
      uni.removeStorageSync('token')
      uni.removeStorageSync('accessToken')
      uni.removeStorageSync('userInfo')

      uni.reLaunch({
        url: '/pages/login/login'
      })
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #f5f1ff;
  padding: 40rpx;
}

.success {
  font-size: 48rpx;
  font-weight: 700;
  color: #333333;
}

.tip {
  margin-top: 20rpx;
  font-size: 26rpx;
  color: #999999;
}

.logout-btn {
  width: 320rpx;
  margin-top: 60rpx;
  background: #7b5cf2;
  color: #ffffff;
  border-radius: 999rpx;
}
</style>
