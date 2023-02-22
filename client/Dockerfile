FROM node:19-alpine

WORKDIR /app

# Install dependencies and clean up
RUN set -eux ; \
    npm install && \
    npm cache --force clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* ;

CMD ["npm", "run", "dev", "--", "--https"]
