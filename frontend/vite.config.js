import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  optimizeDeps: {
    esbuildOptions: {
      charset: 'utf8'
    }
  },
  build: {
    rollupOptions: {},
    target: 'esnext',
    charset: 'utf8'
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})