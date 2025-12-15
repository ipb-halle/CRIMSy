# Stage 1: Build the React app
FROM node:18 AS build

# Set the working directory in the container
WORKDIR /app

# Copy package.json and package-lock.json to install dependencies
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the React app files
COPY . .

# Build the app for production
RUN npm run build

# Stage 2: Serve the React app using Nginx
FROM nginx:alpine

# Copy the build files from the previous stage to Nginx's public folder
COPY --from=build /app/build /usr/share/nginx/html

# Expose port 80 to allow access to the app
EXPOSE 80

# Start Nginx in the foreground to serve the app
CMD ["nginx", "-g", "daemon off;"]
