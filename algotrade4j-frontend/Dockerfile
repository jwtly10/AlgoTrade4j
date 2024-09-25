# Build
FROM node:22 as build
WORKDIR /app
# Copy package.json and package-lock.json (if available)
COPY package*.json ./
RUN npm install
COPY . .
# Set build arguments with default values
ARG VITE_NODE_ENV=prod
ARG VITE_ENABLE_DEBUG_LOGS=false
ARG VITE_ENABLE_SIGNUP=false
ARG VITE_MAIN_API_HOST=https://api.algotrade4j.trade
ARG VITE_LIVE_API_HOST=https://live.algotrade4j.trade
# Set environment variables from build arguments
ENV VITE_NODE_ENV=${VITE_NODE_ENV}
ENV VITE_ENABLE_DEBUG_LOGS=${VITE_ENABLE_DEBUG_LOGS}
ENV VITE_ENABLE_SIGNUP=${VITE_ENABLE_SIGNUP}
ENV VITE_MAIN_API_HOST=${VITE_MAIN_API_HOST}
ENV VITE_LIVE_API_HOST=${VITE_LIVE_API_HOST}
# Build the app
RUN npm run build

# Production
FROM nginx:alpine
# Copy the built assets from the build stage
COPY --from=build /app/dist /usr/share/nginx/html
# Copy Nginx configuration
COPY frontend-nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]