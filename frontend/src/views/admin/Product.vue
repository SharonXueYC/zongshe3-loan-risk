<template>
  <section class="product-page">
    <div class="page-header">
      <div>
        <h2>产品管理</h2>
        <p>维护贷款产品的额度、期限、利率和上架状态。</p>
      </div>
      <button class="primary-btn" type="button" @click="openCreateForm">新增产品</button>
    </div>

    <div class="table-card">
      <div v-if="loading" class="state-message">正在加载产品...</div>
      <div v-else-if="loadError" class="state-message error-message">
        <span>{{ loadError }}</span>
        <button type="button" class="text-btn" @click="loadProducts">重新加载</button>
      </div>
      <div v-else class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>产品名称</th>
              <th>产品类型</th>
              <th>额度范围</th>
              <th>利率</th>
              <th>期限</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in products" :key="product.id">
              <td>{{ product.id }}</td>
              <td>{{ product.productName }}</td>
              <td>{{ productTypeText(product.productType) }}</td>
              <td>{{ formatMoney(product.minAmount) }} - {{ formatMoney(product.maxAmount) }}</td>
              <td>{{ product.interestRate }}%</td>
              <td>{{ product.minTerm }} - {{ product.maxTerm }} 个月</td>
              <td>
                <span :class="['status-tag', product.status === 'active' ? 'enabled' : 'disabled']">
                  {{ product.status === 'active' ? '上架' : '下架' }}
                </span>
              </td>
              <td class="actions">
                <button type="button" class="text-btn" @click="openEditForm(product)">编辑</button>
                <button type="button" class="text-btn danger" @click="removeProduct(product)">删除</button>
              </td>
            </tr>
            <tr v-if="products.length === 0">
              <td colspan="8" class="empty-cell">暂无产品数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
      <div class="modal" role="dialog" aria-modal="true" :aria-labelledby="formTitleId">
        <div class="modal-header">
          <h3 :id="formTitleId">{{ editingId === null ? '新增产品' : '编辑产品' }}</h3>
          <button class="close-btn" type="button" aria-label="关闭" @click="closeForm">×</button>
        </div>

        <form @submit.prevent="submitProduct">
          <div class="form-grid">
            <label class="form-field full-width">
              <span>产品名称</span>
              <input v-model.trim="form.productName" type="text" required maxlength="100">
            </label>

            <label class="form-field full-width">
              <span>产品类型</span>
              <select v-model="form.productType" required>
                <option value="GENERAL">普通贷</option>
                <option value="CONSUME">消费贷</option>
              </select>
            </label>

            <label class="form-field">
              <span>最低额度（元）</span>
              <input v-model.number="form.minAmount" type="number" min="0" step="0.01" required>
            </label>

            <label class="form-field">
              <span>最高额度（元）</span>
              <input v-model.number="form.maxAmount" type="number" min="0" step="0.01" required>
            </label>

            <label class="form-field">
              <span>最短期限（月）</span>
              <input v-model.number="form.minTerm" type="number" min="1" step="1" required>
            </label>

            <label class="form-field">
              <span>最长期限（月）</span>
              <input v-model.number="form.maxTerm" type="number" min="1" step="1" required>
            </label>

            <label class="form-field">
              <span>年利率（%）</span>
              <input v-model.number="form.interestRate" type="number" min="0" step="0.01" required>
            </label>

            <label class="form-field">
              <span>状态</span>
              <select v-model="form.status" required>
                <option value="active">上架</option>
                <option value="inactive">下架</option>
              </select>
            </label>
          </div>

          <p v-if="formError" class="form-error">{{ formError }}</p>

          <div class="modal-footer">
            <button type="button" class="secondary-btn" :disabled="saving" @click="closeForm">取消</button>
            <button type="submit" class="primary-btn" :disabled="saving">
              {{ saving ? '提交中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script>
import {
  fetchProducts,
  createProduct,
  updateProduct,
  deleteProduct
} from '../../api/admin'

function createEmptyForm() {
  return {
    productName: '',
    productType: 'GENERAL',
    minAmount: 1000,
    maxAmount: 100000,
    minTerm: 1,
    maxTerm: 12,
    interestRate: 8.5,
    status: 'active'
  }
}

export default {
  name: 'AdminProduct',
  data() {
    return {
      products: [],
      loading: false,
      loadError: '',
      showForm: false,
      editingId: null,
      form: createEmptyForm(),
      formError: '',
      saving: false,
      formTitleId: 'product-form-title'
    }
  },
  mounted() {
    this.loadProducts()
  },
  methods: {
    async loadProducts() {
      this.loading = true
      this.loadError = ''
      try {
        this.products = await fetchProducts()
      } catch (error) {
        this.loadError = error.message || '产品列表加载失败'
      } finally {
        this.loading = false
      }
    },
    openCreateForm() {
      this.editingId = null
      this.form = createEmptyForm()
      this.formError = ''
      this.showForm = true
    },
    openEditForm(product) {
      this.editingId = product.id
      this.form = {
        productName: product.productName || '',
        productType: this.normalizeProductType(product.productType),
        minAmount: Number(product.minAmount),
        maxAmount: Number(product.maxAmount),
        minTerm: Number(product.minTerm),
        maxTerm: Number(product.maxTerm),
        interestRate: Number(product.interestRate),
        status: product.status || 'active'
      }
      this.formError = ''
      this.showForm = true
    },
    closeForm() {
      if (this.saving) return
      this.showForm = false
      this.formError = ''
    },
    validateForm() {
      if (this.form.minAmount > this.form.maxAmount) {
        return '最低额度不能大于最高额度'
      }
      if (this.form.minTerm > this.form.maxTerm) {
        return '最短期限不能大于最长期限'
      }
      return ''
    },
    async submitProduct() {
      this.formError = this.validateForm()
      if (this.formError) return

      this.saving = true
      try {
        const payload = { ...this.form }
        if (this.editingId === null) {
          await createProduct(payload)
        } else {
          await updateProduct(this.editingId, payload)
        }
        await this.loadProducts()
        this.showForm = false
      } catch (error) {
        this.formError = error.message || '产品保存失败'
      } finally {
        this.saving = false
      }
    },
    async removeProduct(product) {
      if (!window.confirm(`确定删除产品“${product.productName}”吗？`)) return
      try {
        await deleteProduct(product.id)
        await this.loadProducts()
      } catch (error) {
        window.alert(error.message || '产品删除失败')
      }
    },
    productTypeText(type) {
      const labels = {
        GENERAL: '普通贷',
        CONSUME: '消费贷',
        个人消费贷款: '消费贷',
        个人消费贷: '消费贷'
      }
      return labels[type] || type || '-'
    },
    normalizeProductType(type) {
      if (type === 'CONSUME' || String(type || '').includes('消费')) {
        return 'CONSUME'
      }
      return 'GENERAL'
    },
    formatMoney(value) {
      return `¥${Number(value || 0).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })}`
    }
  }
}
</script>

<style scoped>
.product-page {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 6px;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #777;
  font-size: 14px;
}

.table-card {
  overflow: hidden;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.table-scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 14px 16px;
  text-align: left;
  white-space: nowrap;
  border-bottom: 1px solid #eee;
}

th {
  color: #555;
  font-size: 14px;
  background-color: #fafafa;
}

tbody tr:hover {
  background-color: #fcfaff;
}

.actions {
  display: flex;
  gap: 12px;
}

.status-tag {
  display: inline-block;
  padding: 4px 10px;
  font-size: 13px;
  border-radius: 999px;
}

.status-tag.enabled {
  color: #237804;
  background: #f6ffed;
}

.status-tag.disabled {
  color: #8c8c8c;
  background: #f5f5f5;
}

.primary-btn,
.secondary-btn,
.text-btn,
.close-btn {
  border: 0;
  cursor: pointer;
}

.primary-btn,
.secondary-btn {
  padding: 9px 18px;
  border-radius: 4px;
}

.primary-btn {
  color: #fff;
  background-color: #a855f7;
}

.primary-btn:hover:not(:disabled) {
  background-color: #9333ea;
}

.secondary-btn {
  color: #555;
  background-color: #f0f0f0;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.text-btn {
  padding: 0;
  color: #7e22ce;
  background: transparent;
}

.text-btn.danger {
  color: #dc2626;
}

.state-message,
.empty-cell {
  padding: 40px 20px;
  color: #777;
  text-align: center;
}

.error-message {
  display: flex;
  justify-content: center;
  gap: 12px;
  color: #b91c1c;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(0, 0, 0, 0.45);
}

.modal {
  width: min(620px, 100%);
  max-height: 90vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
}

.modal-header,
.modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  color: #777;
  font-size: 26px;
  line-height: 1;
  background: transparent;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  padding: 22px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #444;
  font-size: 14px;
}

.form-field.full-width {
  grid-column: 1 / -1;
}

.form-field input,
.form-field select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  outline: none;
}

.form-field input:focus,
.form-field select:focus {
  border-color: #a855f7;
  box-shadow: 0 0 0 2px rgba(168, 85, 247, 0.12);
}

.form-error {
  margin: -8px 22px 16px;
  color: #dc2626;
  font-size: 14px;
}

.modal-footer {
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #eee;
  border-bottom: 0;
}

@media (max-width: 640px) {
  .product-page {
    padding: 16px;
  }

  .page-header {
    align-items: flex-start;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-field.full-width {
    grid-column: auto;
  }
}
</style>
