FROM node:22-alpine AS builder

RUN apk add --no-cache git

WORKDIR /app

COPY frontend/package.json frontend/package-lock.json ./
COPY frontend/src ./src
COPY frontend/server.js ./server.js
RUN npm ci

COPY .git ./.git

RUN echo "RELEASE_NAME=$(git log -1 --format=%h)" >> .env
RUN echo "RELEASE_DATE=$(git log -1 --format=%cI)" >> .env

FROM node:22-alpine

WORKDIR /app

COPY --from=builder /app/package.json /app/package-lock.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/src ./src
COPY --from=builder /app/server.js ./server.js
COPY --from=builder /app/.env ./.env

EXPOSE 3000

CMD ["npm", "run", "start"]