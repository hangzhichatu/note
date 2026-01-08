<!-- ProductListWithCustomMenu.vue -->
<template>
  <div class="product-list" @contextmenu="hideMenu">
    <h2>商品列表（右键试试）</h2>
    
    <div 
      v-for="product in products" 
      :key="product.id"
      class="product-card"
      @click="selectForCompare(product, 'A')"
      @contextmenu.prevent="openContextMenu($event, product)" <!-- 关键！ -->
    >
      <h3>{{ product.name }}</h3>
      <p>¥{{ product.price }}</p>
    </div>

    <!-- 自定义右键菜单（Teleport 到 body） -->
    <Teleport to="body">
      <div 
        v-if="contextMenu.visible"
        class="custom-context-menu"
        :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }"
        @contextmenu.prevent
      >
        <button @click="handleMenuAction('compare')">加入对比 (B)</button>
        <button @click="handleMenuAction('favorite')">收藏</button>
        <button @click="handleMenuAction('share')">分享</button>
        <button @click="hideMenu">取消</button>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const products = [
  { id: 1, name: 'MacBook Pro', price: 12999 },
  { id: 2, name: 'Dell XPS 13', price: 8999 },
  { id: 3, name: 'ThinkPad X1', price: 9999 }
]

// 对比状态（简化）
const compareStore = reactive({
  productA: null,
  productB: null
})

// 自定义菜单状态
const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  targetProduct: null
})

// 打开自定义菜单
function openContextMenu(event, product) {
  // 阻止默认右键菜单（已在模板用 .prevent）
  event.preventDefault()
  
  // 记录点击位置（相对于视口）
  contextMenu.x = event.clientX
  contextMenu.y = event.clientY
  
  // 记录目标商品
  contextMenu.targetProduct = product
  contextMenu.visible = true
}

// 处理菜单项点击
function handleMenuAction(action) {
  const product = contextMenu.targetProduct
  switch (action) {
    case 'compare':
      compareStore.productB = product // 右键选 B
      break
    case 'favorite':
      alert(`已收藏: ${product.name}`)
      break
    case 'share':
      navigator.clipboard?.writeText(`快来看: ${product.name} - ¥${product.price}`)
        .then(() => alert('链接已复制'))
      break
  }
  hideMenu()
}

// 隐藏菜单
function hideMenu() {
  contextMenu.visible = false
  contextMenu.targetProduct = null
}

// 点击页面其他地方也关闭菜单
document.addEventListener('click', hideMenu)
onUnmounted(() => {
  document.removeEventListener('click', hideMenu)
})
</script>

<style scoped>
/* 商品卡片 */
.product-card {
  border: 1px solid #eee;
  padding: 16px;
  margin: 10px 0;
  cursor: pointer;
  border-radius: 6px;
}
.product-card:hover {
  background: #f5f5f5;
}

/* 自定义右键菜单 */
.custom-context-menu {
  position: fixed;
  background: white;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 10000;
  min-width: 160px;
  overflow: hidden;
}
.custom-context-menu button {
  display: block;
  width: 100%;
  padding: 8px 12px;
  border: none;
  background: none;
  text-align: left;
  cursor: pointer;
  font-size: 14px;
}
.custom-context-menu button:hover {
  background: #f0f0f0;
}
.custom-context-menu button:last-child {
  color: #d32f2f;
}
</style>