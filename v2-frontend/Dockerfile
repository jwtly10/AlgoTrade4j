FROM node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm install
COPY . .

ARG VITE_NODE_ENV=prod
ARG VITE_LOG_LEVEL=WARN
# TODO: Note this is not used anymore - we use VITE_LOG_LEVEL
ARG VITE_ENABLE_DEBUG_LOGS=false
ARG VITE_ENABLE_SIGNUP=false
ARG VITE_MAIN_API_HOST=https://api.algotrade4j.trade
ARG VITE_LIVE_API_HOST=https://live-api.algotrade4j.trade

ENV VITE_NODE_ENV=${VITE_NODE_ENV}
ENV VITE_SITE_URL=https://algotrade4j.trade
# TODO: Note this is not used anymore - we have just hardcoded the signup to be disabled
ENV VITE_LOG_LEVEL=${VITE_LOG_LEVEL}
ENV VITE_ENABLE_SIGNUP=${VITE_ENABLE_SIGNUP}
ENV VITE_MAIN_API_HOST=${VITE_MAIN_API_HOST}
ENV VITE_LIVE_API_HOST=${VITE_LIVE_API_HOST}

RUN npm run build

# Serve vite app with nginx
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html

COPY frontend-nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
