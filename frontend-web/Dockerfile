# Frontend do DocFlow (React + Vite), servido pelo nginx
FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# A API fica na mesma origem, atrás do proxy do nginx (/api -> backend)
ARG VITE_API_URL=/api
ARG VITE_APP_NAME=DocFlow
ENV VITE_API_URL=$VITE_API_URL VITE_APP_NAME=$VITE_APP_NAME
RUN npm run build

FROM nginx:1.30-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
