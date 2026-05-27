# ---- Build Stage ----
FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---- Run Stage ----
FROM node:22-alpine

WORKDIR /app

# Install a simple static file server
RUN npm install -g serve

COPY --from=build /app/dist/frontend/browser ./dist

EXPOSE 4200

CMD ["serve:ssr:frontend"]
