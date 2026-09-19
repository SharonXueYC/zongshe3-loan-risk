<template>
  <view class="page">
    <!-- 加载中 -->
    <view v-if="loading" class="state-box">
      <text class="state-title">产品详情加载中...</text>
      <text class="state-text">请稍候</text>
    </view>

    <!-- 加载失败 -->
    <view v-else-if="loadError" class="state-box">
      <text class="state-title">加载失败</text>
      <text class="state-text">{{ errorMessage }}</text>

      <button class="retry-btn" @click="loadDetail">
        重新加载
      </button>
    </view>

    <!-- 产品详情 -->
    <view v-else-if="product" class="content">
      <view class="main-card">
        <view class="top-row">
          <text class="product-name">
            {{ product.productName }}
          </text>

          <text class="type-tag">
            {{ getTypeName(product.productType) }}
          </text>
        </view>

        <text class="product-no">
          产品编号：{{ product.productNo || '--' }}
        </text>

        <view class="amount-title">
          贷款额度
        </view>

        <view class="amount">
          ¥{{ formatMoney(product.minAmount) }}
          ~
          ¥{{ formatMoney(product.maxAmount) }}
        </view>
      </view>

      <view class="detail-card">
        <view class="detail-row">
          <text class="label">年化利率</text>
          <text class="value">
            {{ formatRate(product.interestRate) }}
          </text>
        </view>

        <view class="detail-row">
          <text class="label">贷款期限</text>
          <text class="value">
            {{ product.minTerm }}-{{ product.maxTerm }}个月
          </text>
        </view>

        <view class="detail-row">
          <text class="label">产品类型</text>
          <text class="value">
            {{ getTypeName(product.productType) }}
          </text>
        </view>

        <view class="description">
          <text class="description-title">
            产品介绍
          </text>

          <text class="description-text">
            {{ product.productDescription || '暂无产品介绍' }}
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getProductDetail } from '@/api/product.js'

export default {
  data() {
    return {
      productId: null,
      product: null,
      loading: false,
      loadError: false,
      errorMessage: ''
    }
  },

  onLoad(options) {
    this.productId = options.id

    if (!this.productId) {
      this.loadError = true
      this.errorMessage = '缺少产品 ID'
      return
    }

    this.loadDetail()
  },

  methods: {
    async loadDetail() {
      this.loading = true
      this.loadError = false
      this.errorMessage = ''

      try {
        const res = await getProductDetail(this.productId)

        if (res && res.success && res.data) {
          this.product = res.data
        } else {
          throw new Error(
            res?.message || '获取产品详情失败'
          )
        }
      } catch (e) {
        console.error('获取产品详情失败：', e)

        this.product = null
        this.loadError = true
        this.errorMessage =
          e?.message || '产品详情加载失败，请稍后重试'
      } finally {
        this.loading = false
      }
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

.main-card,
.detail-card {
  margin-bottom: 28rpx;
  padding: 36rpx;
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 12rpx 36rpx rgba(93, 67, 210, 0.12);
}

.top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-name {
  color: #1f1f2b;
  font-size: 38rpx;
  font-weight: bold;
}

.type-tag {
  padding: 8rpx 18rpx;
  color: #5a3fca;
  font-size: 22rpx;
  background: rgba(123, 92, 242, 0.12);
  border-radius: 20rpx;
}

.product-no {
  display: block;
  margin-top: 14rpx;
  color: #9996a7;
  font-size: 23rpx;
}

.amount-title {
  margin-top: 46rpx;
  color: #8a8798;
  font-size: 24rpx;
}

.amount {
  margin-top: 10rpx;
  color: #5a3fca;
  font-size: 43rpx;
  font-weight: bold;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #eeeaf9;
}

.label {
  color: #77758a;
  font-size: 27rpx;
}

.value {
  color: #282634;
  font-size: 27rpx;
  font-weight: bold;
}

.description {
  margin-top: 34rpx;
}

.description-title {
  display: block;
  color: #282634;
  font-size: 29rpx;
  font-weight: bold;
}

.description-text {
  display: block;
  margin-top: 18rpx;
  color: #77758a;
  font-size: 26rpx;
  line-height: 1.7;
}

.state-box {
  margin-top: 180rpx;
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
