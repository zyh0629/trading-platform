<template>
  <div class="map-view">
    <div v-if="loading" class="map-state">地图加载中...</div>
    <div ref="mapContainer" class="map-container"></div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import AMapLoader from '@amap/amap-jsapi-loader'
import request from '../api/request'

const router = useRouter()

const props = defineProps({
  products: {
    type: Array,
    default: () => []
  }
})

const mapContainer = ref(null)
const loading = ref(true)
const spotsLoaded = ref(false)

let map = null
let AMap = null
let spotIdToLatLng = {}
let spotNamesById = {}
let markers = []

const clearMarkers = () => {
  if (map && markers.length > 0) {
    map.remove(markers)
  }
  markers = []
}

const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, character => ({
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#39;'
}[character]))

const showProductList = (products, position) => {
  if (!map || !AMap || products.length === 0) return

  const spotNames = [...new Set(products.map(product => product.spotName).filter(Boolean))]
  const listTitle = spotNames.length === 1 ? spotNames[0] : '附近交易点商品'
  const items = products.map(product => `
    <button type="button" class="map-product-item" data-product-id="${escapeHtml(product.id)}">
      <span>
        <span>${escapeHtml(product.title || '未命名商品')}</span>
        <small>${escapeHtml(product.spotName || '')}</small>
      </span>
      <strong>¥${escapeHtml(product.price ?? '暂无')}</strong>
    </button>
  `).join('')
  const content = `
    <div class="map-product-list">
      <strong class="map-product-list-title">${escapeHtml(listTitle)}（${products.length}件）</strong>
      ${items}
    </div>
  `
  const infoWindow = new AMap.InfoWindow({
    content,
    offset: new AMap.Pixel(0, -24)
  })
  infoWindow.open(map, position)

  window.setTimeout(() => {
    document.querySelectorAll('.map-product-item[data-product-id]').forEach(button => {
      if (button.dataset.bound === 'true') return
      button.dataset.bound = 'true'
      button.addEventListener('click', () => {
        router.push(`/product/${encodeURIComponent(button.dataset.productId)}`)
      })
    })
  }, 0)
}

const renderCountMarker = (marker, count) => {
  const color = count >= 20 ? '#f56c6c' : count >= 8 ? '#e6a23c' : '#409eff'
  marker.setContent(`
    <div class="map-cluster-marker" style="background:${color}">
      ${escapeHtml(count)}
    </div>
  `)
  marker.setOffset(new AMap.Pixel(-18, -18))
}

const renderMarkers = () => {
  if (!spotsLoaded.value || !map || !AMap) return
  clearMarkers()

  const productsBySpot = new Map()
  let skipped = 0
  for (const product of props.products) {
    const spotId = String(product.spotId)
    if (!spotIdToLatLng[spotId]) {
      skipped++
      continue
    }

    const productInfo = {
      ...product,
      spotName: product.spotName || spotNamesById[spotId] || ''
    }
    if (!productsBySpot.has(spotId)) productsBySpot.set(spotId, [])
    productsBySpot.get(spotId).push(productInfo)
  }

  for (const [spotId, products] of productsBySpot) {
    const marker = new AMap.Marker({
      position: spotIdToLatLng[spotId],
      title: `${products[0].spotName || '校内交易点'}（${products.length}件商品）`,
      extData: products
    })
    renderCountMarker(marker, products.length)
    marker.on('click', () => {
      showProductList(marker.getExtData() || [], marker.getPosition())
    })
    markers.push(marker)
  }

  console.log('地图交易点标记:', {
    products: props.products.length,
    spotMarkers: markers.length,
    skippedProducts: skipped
  })

  if (markers.length > 0) {
    map.add(markers)
  }
}

onMounted(async () => {
  loading.value = true

  try {
    try {
      const result = await request.get('/campus-spot/list')
      const spots = Array.isArray(result) ? result : []
      spotIdToLatLng = Object.fromEntries(
        spots
          .filter(spot => spot.latitude != null && spot.longitude != null)
          .map(spot => {
            const latitude = Number(spot.latitude)
            const longitude = Number(spot.longitude)
            return [String(spot.id), [longitude, latitude]]
          })
          .filter(([, position]) => position.every(Number.isFinite))
      )
      spotNamesById = Object.fromEntries(spots.map(spot => [String(spot.id), spot.name]))
      console.log('🔍 交易点加载完成:', {
        spotsCount: spots.length,
        mappedCount: Object.keys(spotIdToLatLng).length
      })
    } catch (error) {
      console.warn('获取校内交易点失败，地图暂不显示商品标记', error)
      spotIdToLatLng = {}
      spotNamesById = {}
    } finally {
      spotsLoaded.value = true
    }

    window._AMapSecurityConfig = {
      securityJsCode: import.meta.env.VITE_AMAP_SECURITY_CODE
    }
    AMap = await AMapLoader.load({
      key: import.meta.env.VITE_AMAP_KEY,
      version: '2.0'
    })
    map = new AMap.Map(mapContainer.value, {
      center: [119.641780, 29.132413],
      zoom: 15
    })
    await nextTick()
    renderMarkers()
  } catch (error) {
    console.error('地图初始化失败', error)
  } finally {
    loading.value = false
  }
})

watch(() => props.products, () => {
  // 商品数据变化时，重新渲染
  renderMarkers()
}, { deep: true })

onUnmounted(() => {
  clearMarkers()
  if (map) {
    map.destroy()
    map = null
  }
})
</script>

<style scoped>
.map-view {
  position: relative;
  min-height: 600px;
}
.map-container {
  width: 100%;
  height: 600px;
  border-radius: 12px;
  overflow: hidden;
}
.map-state {
  position: absolute;
  z-index: 1;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.88);
  color: #606266;
}
:global(.map-cluster-marker) {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border: 3px solid #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
}
:global(.map-product-list) {
  max-height: 280px;
  min-width: 210px;
  overflow-y: auto;
}
:global(.map-product-list-title) {
  display: block;
  margin-bottom: 6px;
}
:global(.map-product-item) {
  width: 100%;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 4px;
  border: 0;
  border-bottom: 1px solid #eee;
  background: white;
  color: #303133;
  text-align: left;
  cursor: pointer;
}
:global(.map-product-item:hover) {
  background: #f5f7fa;
}
:global(.map-product-item strong) {
  flex-shrink: 0;
  color: #f56c6c;
}
:global(.map-product-item small) {
  display: block;
  margin-top: 2px;
  color: #909399;
}
</style>