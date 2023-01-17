import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server : {
    watch : {
      usePolling: process.env.VITE_SERVER_WATCH_USE_POLLING === 'true'
    }
  }
})
