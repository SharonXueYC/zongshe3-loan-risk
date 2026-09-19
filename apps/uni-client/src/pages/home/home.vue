<template>
  <view class="page">
    <view class="welcome-card">
      <text class="title">闪借</text>
      <text class="subtitle">选择适合您的贷款产品</text>
    </view>

    <view class="menu-card" @click="goProducts">
      <view class="menu-left">
        <text class="menu-title">贷款产品</text>
        <text class="menu-desc">
          查看贷款额度、利率和期限
        </text>
      </view>

      <text class="arrow">›</text>
    </view>

    <button class="logout-btn" @click="logout">
      退出登录
    </button>
  </view>
</template>

<script>
export default {
  methods: {
    // 进入产品列表
    goProducts() {
      uni.navigateTo({
        url: '/pages/product/list'
      })
    },

    // 退出登录
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
  padding: 40rpx 32rpx;
  background: #f5f1ff;
}

.welcome-card {
  padding: 50rpx 40rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #7b5cf2, #9b7bff);
  border-radius: 30rpx;
  box-shadow: 0 14rpx 40rpx rgba(91, 63, 202, 0.2);
}

.title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
}

.subtitle {
  display: block;
  margin-top: 16rpx;
  font-size: 26rpx;
  opacity: 0.9;
}

.menu-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 36rpx;
  padding: 38rpx 34rpx;
  background: #ffffff;
  border-radius: 26rpx;
  box-shadow: 0 10rpx 30rpx rgba(93, 67, 210, 0.1);
}

.menu-left {
  display: flex;
  flex-direction: column;
}

.menu-title {
  color: #292733;
  font-size: 32rpx;
  font-weight: bold;
}

.menu-desc {
  margin-top: 12rpx;
  color: #8c8999;
  font-size: 24rpx;
}

.arrow {
  color: #7b5cf2;
  font-size: 54rpx;
}

.logout-btn {
  width: 320rpx;
  margin-top: 80rpx;
  color: #7b5cf2;
  font-size: 27rpx;
  background: #ffffff;
  border: 2rpx solid #7b5cf2;
  border-radius: 999rpx;
}
</style>
