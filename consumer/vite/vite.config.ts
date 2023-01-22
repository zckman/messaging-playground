import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import basicSsl from '@vitejs/plugin-basic-ssl'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(), basicSsl()],
  server : {
    watch : {
      usePolling: process.env.VITE_SERVER_WATCH_USE_POLLING === 'true'
    }
  }
})
