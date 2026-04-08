FROM node:24-alpine

WORKDIR /app

COPY ./TinyFarm-G486/frontend .
RUN npm i

EXPOSE 3000

CMD ["node", "server.js"]