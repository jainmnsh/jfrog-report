#docker file use node 10.x version
FROM node:10

# setting up the node js working folder
WORKDIR /usr/src/app

# copy just package json to build image
COPY ./app/package*.json ./
RUN npm install

# copy the source code - idea is to not have module copied in final image
COPY ./app ./

EXPOSE 8080

CMD ["node", "index.js"]
