import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { fileURLToPath } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    // dev 서버를 백엔드(:8080)와 same-origin 으로 묶어 JSESSIONID 세션 쿠키가 그대로 흐르게 한다.
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/h2-console': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
