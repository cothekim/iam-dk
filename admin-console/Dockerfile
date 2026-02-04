# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# Build arguments for React environment variables
ARG REACT_APP_API_URL
ENV REACT_APP_API_URL=$REACT_APP_API_URL

COPY admin-console/package.json ./
RUN npm install

COPY admin-console/public ./public
COPY admin-console/src ./src

RUN npm run build

# Production stage
FROM nginx:alpine

COPY --from=build /app/build /usr/share/nginx/html

COPY admin-console/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
