import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5174, // On fixe le port pour éviter qu'il change
    proxy: {
      // Toutes les requêtes commençant par /api seront redirigées vers Spring Boot
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})