# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# Set API URL from build argument (default to localhost)
ARG API_URL=http://directory-service:8081/api/admin

# Copy package files
COPY admin-console/package.json ./

# Install dependencies
RUN npm install

# Copy source files
COPY admin-console/public ./public
COPY admin-console/src ./src

# Build with API URL
RUN echo "REACT_APP_API_URL=${API_URL}" > .env && npm run build

# Production stage
FROM nginx:alpine

WORKDIR /app

# Set API URL from build argument (default to localhost)
ARG API_URL=http://directory-service:8081/api/admin

# Copy built files
COPY --from=build /app/build /usr/share/nginx/html

# Copy nginx config
COPY admin-console/nginx.conf /usr/share/nginx/html/nginx.conf

# Substitute API_URL in nginx.conf using envsubst
RUN apk add --no-cache gettext && \
    envsubst "$API_URL" < /usr/share/nginx/html/nginx.conf > /tmp/nginx.conf && \
    mv /tmp/nginx.conf /usr/share/nginx/html/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
