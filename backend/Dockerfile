FROM node:20-alpine

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install production dependencies
RUN npm install --omit=dev

# Copy application source code
COPY . .

# Expose backend port
EXPOSE 5000

# Set environment
ENV NODE_ENV=production
ENV PORT=5000

# Start server
CMD ["node", "src/server.js"]
