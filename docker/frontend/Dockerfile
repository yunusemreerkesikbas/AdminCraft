# Craftive Frontend - Multi-stage Dockerfile
# Builds Angular app and serves via Nginx

FROM node:20-alpine AS build
WORKDIR /app

# Build argument for environment (prod, stage, dev)
ARG BUILD_ENV=prod

# Copy package files for dependency caching
COPY storefront/package.json storefront/package-lock.json ./

# Install dependencies
RUN npm ci --legacy-peer-deps

# Copy source files
COPY storefront/ .

# Build for specified environment
RUN npm run build:${BUILD_ENV}

# Production stage with Nginx
FROM nginx:alpine

# Copy custom Nginx config
COPY docker/frontend/nginx.conf /etc/nginx/nginx.conf

# Copy built Angular app
# Angular 19 outputs to dist/craftive/browser for SSR-ready builds
COPY --from=build /app/dist/craftive/browser /usr/share/nginx/html

# Add non-root user for security
RUN addgroup -g 1001 appgroup && adduser -u 1001 -G appgroup -D appuser && \
    chown -R appuser:appgroup /usr/share/nginx/html && \
    chown -R appuser:appgroup /var/cache/nginx && \
    chown -R appuser:appgroup /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R appuser:appgroup /var/run/nginx.pid

USER appuser

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
