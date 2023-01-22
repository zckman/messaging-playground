FROM node:19-alpine

WORKDIR /app

# COPY . .
COPY package.json package-lock.json ./
# Install dependencies and clean up
RUN set -eux ; \
    npm install && \
    npm cache --force clean && \
    rm -rf /tmp/* /var/tmp/*

CMD ["npm", "run", "dev", "--", "--https"]
