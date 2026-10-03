import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/design-tokens.css'
import './styles/element-override.css'

createApp(App).use(router).use(ElementPlus).mount('#app')