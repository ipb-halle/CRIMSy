# ---------- Build stage ----------
FROM node:18-alpine AS builder
WORKDIR /app

# Copy only dependency files first (enables caching)
COPY package*.json ./
RUN npm ci

# Copy application source
COPY . .

# Build production assets
RUN npm run build

# ---------- Runtime stage ----------
FROM nginx:alpine

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d

# Copy build output
COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
