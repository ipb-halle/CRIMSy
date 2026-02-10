# ---------- Build stage ----------
FROM node:18-alpine AS builder

WORKDIR /app

# Copy only dependency files first (enables caching)
COPY package*.json tsconfig.json ./

# install dependencies
RUN npm ci --include=dev

# Copy application source
COPY public ./public
COPY src ./src

# Build production assets
RUN npm run build

# ---------- Runtime stage ----------
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d

# Copy build output
COPY --from=builder /app/build .

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
