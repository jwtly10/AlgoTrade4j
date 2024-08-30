FROM nginx:alpine
COPY ./algotrade4j-frontend/dist /usr/share/nginx/html
COPY ./infra-config/frontend-nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]