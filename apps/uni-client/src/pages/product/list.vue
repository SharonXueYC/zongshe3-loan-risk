
<template>
  <view class="page">
    <view class="header">
      <text class="title">贷款产品</text>
      <text class="subtitle">选择适合您的贷款产品</text>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="state-box">
      <text class="state-title">产品加载中...</text>
      <text class="state-text">请稍候</text>
    </view>

    <!-- 加载失败 -->
    <view v-else-if="loadError" class="state-box">
      <text class="state-title">加载失败</text>
      <text class="state-text">{{ errorMessage }}</text>
      <button class="retry-btn" @click="loadProducts">
        重新加载
      </button>
    </view>

    <!-- 空列表 -->
    <view v-else-if="products.length === 0" class="state-box">
      <text class="state-title">暂无贷款产品</text>
      <text class="state-text">
        当前暂时没有可申请的产品
      </text>
    </view>

    <!-- 产品列表 -->
    <view v-else class="product-list">
      <view
        v-for="product in products"
        :key="product.id"
        class="product-card"
        @click="goDetail(product.id)"
      >
        <view class="card-top">
          <text class="product-name">
            {{ product.productName }}
          </text>

          <text class="type-tag">
            {{ getTypeName(product.productType) }}
          </text>
        </view>

        <view class="amount">
          ¥{{ formatMoney(product.minAmount) }}
          ~
          ¥{{ formatMoney(product.maxAmount) }}
        </view>

        <text class="amount-label">额度范围</text>

        <view class="info-row">
          <view class="info-item">
            <text class="info-value">
              {{ formatRate(product.interestRate) }}
            </text>
            <text class="info-label">年化利率</text>
          </view>

          <view class="info-item">
            <text class="info-value">
              {{ product.minTerm }}-{{ product.maxTerm }}个月
            </text>
            <text class="info-label">贷款期限</text>
          </view>
        </view>

        <view class="detail-link">
          查看详情 →
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getProductList } from '@/api/product.js'

export default {
  data() {
    return {
      products: [],
      loading: false,
      loadError: false,
      errorMessage: ''
    }
  },

  onLoad() {
    this.loadProducts()
  },

  methods: {
    async loadProducts() {
      this.loading = true
      this.loadError = false
      this.errorMessage = ''

      try {
        const res = await getProductList({
          status: 'active'
        })

        if (res && res.success) {
          this.products = Array.isArray(res.data)
            ? res.data
            : []
        } else {
          throw new Error(
            res?.message || '获取产品列表失败'
          )
        }
      } catch (e) {
        console.error('获取产品列表失败：', e)

        this.products = []
        this.loadError = true
        this.errorMessage =
          e?.message || '产品加载失败，请稍后重试'
      } finally {
        this.loading = false
      }
    },

    goDetail(id) {
      uni.navigateTo({
        url: `/pages/product/detail?id=${id}`
      })
    },

    formatMoney(value) {
      const number = Number(value)

      if (Number.isNaN(number)) {
        return '--'
      }

      return number.toLocaleString()
    },

    formatRate(value) {
      if (value === null || value === undefined) {
        return '--'
      }

      return `${value}%`
    },

    getTypeName(type) {
      if (!type) {
        return '普通贷'
      }

      const value = String(type).toUpperCase()

      if (value === 'CONSUME') {
        return '消费贷'
      }

      if (value === 'GENERAL') {
        return '普通贷'
      }

      return type
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 32rpx;
  background: #f5f3ff;
}

.header {
  margin-bottom: 32rpx;
}

.title {
  display: block;
  color: #1f1f2b;
  font-size: 44rpx;
  font-weight: bold;
}

.subtitle {
  display: block;
  margin-top: 10rpx;
  color: #77758a;
  font-size: 26rpx;
}

.product-card {
  box-sizing: border-box;
  margin-bottom: 28rpx;
  padding: 34rpx;
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 12rpx 36rpx rgba(93, 67, 210, 0.12);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-name {
  color: #1f1f2b;
  font-size: 34rpx;
  font-weight: bold;
}

.type-tag {
  padding: 8rpx 18rpx;
  color: #5a3fca;
  font-size: 22rpx;
  background: rgba(123, 92, 242, 0.12);
  border-radius: 20rpx;
}

.amount {
  margin-top: 34rpx;
  color: #5a3fca;
  font-size: 40rpx;
  font-weight: bold;
}

.amount-label {
  display: block;
  margin-top: 8rpx;
  color: #8a8798;
  font-size: 23rpx;
}

.info-row {
  display: flex;
  margin-top: 34rpx;
  padding-top: 28rpx;
  border-top: 1rpx solid #eeeaf9;
}

.info-item {
  flex: 1;
}

.info-value {
  display: block;
  color: #282634;
  font-size: 29rpx;
  font-weight: bold;
}

.info-label {
  display: block;
  margin-top: 8rpx;
  color: #8a8798;
  font-size: 22rpx;
}

.detail-link {
  margin-top: 30rpx;
  color: #7b5cf2;
  font-size: 26rpx;
  text-align: right;
}

.state-box {
  margin-top: 160rpx;
  padding: 60rpx 30rpx;
  text-align: center;
}

.state-title {
  display: block;
  color: #3a3748;
  font-size: 32rpx;
  font-weight: bold;
}

.state-text {
  display: block;
  margin-top: 16rpx;
  color: #8a8798;
  font-size: 25rpx;
}

.retry-btn {
  width: 260rpx;
  margin-top: 34rpx;
  color: #ffffff;
  font-size: 26rpx;
  background: #7b5cf2;
  border-radius: 40rpx;
}
</style>
