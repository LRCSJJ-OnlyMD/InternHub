# Build stage
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy source code
COPY . .

# Build argument for API URL
ARG API_URL=http://localhost:8080/api

# Replace environment variables in the code
RUN echo "export const environment = { production: true, apiUrl: '${API_URL}' };" > src/environments/environment.prod.ts

# Build the application
RUN npm run build -- --configuration production

# Runtime stage
FROM nginx:alpine

# Install envsubst for runtime environment variable substitution
RUN apk add --no-cache bash

# Copy built application
COPY --from=build /app/dist/internhub-frontend /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Create a script to inject environment variables at runtime
RUN echo '#!/bin/bash' > /docker-entrypoint.sh && \
    echo 'if [ -n "$API_URL" ]; then' >> /docker-entrypoint.sh && \
    echo '  echo "Configuring API URL: $API_URL"' >> /docker-entrypoint.sh && \
    echo '  find /usr/share/nginx/html -name "*.js" -exec sed -i "s|API_URL_PLACEHOLDER|$API_URL|g" {} \;' >> /docker-entrypoint.sh && \
    echo 'fi' >> /docker-entrypoint.sh && \
    echo 'nginx -g "daemon off;"' >> /docker-entrypoint.sh && \
    chmod +x /docker-entrypoint.sh

EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80 || exit 1

CMD ["/docker-entrypoint.sh"]
