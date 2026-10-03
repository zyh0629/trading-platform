<template>
  <el-dialog
    :model-value="modelValue"
    title="新增我的交易点"
    width="680px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @closed="destroyMap"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" maxlength="50" placeholder="例如：16号楼宿舍门口" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="2"
          maxlength="200"
          placeholder="选填，补充位置说明"
        />
      </el-form-item>
      <el-form-item label="地图选点" required>
        <div class="spot-map">
          <div v-if="mapLoading" class="map-loading">地图加载中...</div>
          <div ref="mapContainer" class="map-container"></div>
        </div>
        <div class="coordinate-text">
          当前坐标：{{ latitude.toFixed(6) }}, {{ longitude.toFixed(6) }}
        </div>
        <div v-if="mapError" class="map-error">{{ mapError }}</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" :disabled="Boolean(mapError)" @click="submit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { nextTick, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AMapLoader from '@amap/amap-jsapi-loader'
import request from '../api/request'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['update:modelValue', 'created'])

const formRef = ref(null)
const mapContainer = ref(null)
const mapLoading = ref(false)
const mapError = ref('')
const saving = ref(false)
const latitude = ref(29.132413)
const longitude = ref(119.641780)
const form = reactive({ name: '', description: '' })
const rules = {
  name: [
    { required: true, message: '请输入交易点名称', trigger: 'blur' },
    { max: 50, message: '名称不能超过 50 个字', trigger: 'blur' }
  ],
  description: [{ max: 200, message: '描述不能超过 200 个字', trigger: 'blur' }]
}

let map = null
let marker = null
let AMap = null
let loadGeneration = 0

const updateCoordinates = (lng, lat) => {
  longitude.value = Number(Number(lng).toFixed(6))
  latitude.value = Number(Number(lat).toFixed(6))
}

const destroyMap = () => {
  if (map) {
    map.destroy()
    map = null
  }
  marker = null
  AMap = null
}

const initializeMap = async () => {
  const generation = ++loadGeneration
  mapLoading.value = true
  mapError.value = ''
  await nextTick()
  try {
    window._AMapSecurityConfig = {
      securityJsCode: import.meta.env.VITE_AMAP_SECURITY_CODE
    }
    AMap = await AMapLoader.load({
      key: import.meta.env.VITE_AMAP_KEY,
      version: '2.0'
    })
    if (generation !== loadGeneration || !props.modelValue || !mapContainer.value) return

    const center = [longitude.value, latitude.value]
    map = new AMap.Map(mapContainer.value, { center, zoom: 16 })
    marker = new AMap.Marker({ position: center, draggable: true })
    map.add(marker)
    marker.on('dragend', event => {
      const position = event.target.getPosition()
      updateCoordinates(position.getLng(), position.getLat())
    })
    map.on('click', event => {
      marker.setPosition(event.lnglat)
      updateCoordinates(event.lnglat.getLng(), event.lnglat.getLat())
    })
  } catch (error) {
    if (generation === loadGeneration) {
      mapError.value = error.message || '地图加载失败，请检查高德地图配置'
    }
  } finally {
    if (generation === loadGeneration) {
      mapLoading.value = false
    }
  }
}

const submit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !marker) {
    if (!marker) ElMessage.warning('请等待地图加载并确认位置')
    return
  }
  saving.value = true
  try {
    const spot = await request.post('/campus-spot/my', {
      name: form.name.trim(),
      description: form.description.trim(),
      latitude: latitude.value,
      longitude: longitude.value
    })
    emit('created', spot)
    emit('update:modelValue', false)
    form.name = ''
    form.description = ''
  } catch (error) {
    ElMessage.error(error.message || '新增私有交易点失败')
  } finally {
    saving.value = false
  }
}

watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    initializeMap()
  } else {
    loadGeneration++
    destroyMap()
  }
})

onUnmounted(() => {
  loadGeneration++
  destroyMap()
})
</script>

<style scoped>
.spot-map {
  position: relative;
  width: 100%;
  height: 400px;
}
.map-container {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
}
.map-loading {
  position: absolute;
  z-index: 1;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.85);
}
.coordinate-text {
  width: 100%;
  margin-top: 8px;
  color: #606266;
  font-size: 13px;
}
.map-error {
  color: #f56c6c;
}
</style>
