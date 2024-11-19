FROM node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

ARG NEXT_PUBLIC_NODE_ENV=production
ARG NEXT_PUBLIC_ENABLE_DEBUG_LOGS=false
ARG NEXT_PUBLIC_ENABLE_SIGNUP=false
ARG NEXT_PUBLIC_MAIN_API_HOST=https://api.algotrade4j.trade
ARG NEXT_PUBLIC_LIVE_API_HOST=https://live-api.algotrade4j.trade

ENV NEXT_PUBLIC_NODE_ENV=${NEXT_PUBLIC_NODE_ENV}
ENV NEXT_PUBLIC_ENABLE_DEBUG_LOGS=${NEXT_PUBLIC_ENABLE_DEBUG_LOGS}
ENV NEXT_PUBLIC_ENABLE_SIGNUP=${NEXT_PUBLIC_ENABLE_SIGNUP}
ENV NEXT_PUBLIC_MAIN_API_HOST=${NEXT_PUBLIC_MAIN_API_HOST}
ENV NEXT_PUBLIC_LIVE_API_HOST=${NEXT_PUBLIC_LIVE_API_HOST}

RUN npm run build

FROM node:20-alpine
WORKDIR /app

COPY --from=builder /app/next.config.js ./
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000

# Start the application
CMD ["node", "server.js"]
