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
        ],
    },
    server: {
        port: 5173,
    },
    preview: {
        port: 5173,
    },
    define: {
        'process.env': {}
    }
})