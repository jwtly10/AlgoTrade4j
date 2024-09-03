import {defineConfig} from 'vite'
import path from 'path'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: [
            {
                find: /^~(.+)/,
                replacement: path.resolve(__dirname, 'node_modules/$1'),
            },
            {
                find: /^src(.+)/,
                replacement: path.resolve(__dirname, 'src/$1'),
            },
            {
                find: '@',
                replacement: path.resolve(__dirname, 'src')
            }
        ],
    },
    server: {
        port: 5173,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, '')
            }
        }
    },
    preview: {
        port: 5173,
    },
    define: {
        'process.env': {}
    }
})