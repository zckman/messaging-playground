FROM nginx:1.22-alpine

# Create certificates
RUN set -eux; \
    apk add --no-cache openssl && \
    openssl req -x509 -newkey rsa:4096 -keyout /etc/ssl/certs/key.pem -out /etc/ssl/certs/cert.pem -days 365 -nodes -subj '/CN=localhost' -extensions EXT -config <(printf "[dn]\nCN=localhost\n[req]\ndistinguished_name = dn\n[EXT]\nsubjectAltName=DNS:localhost\nkeyUsage=digitalSignature\nextendedKeyUsage=serverAuth") ; \
    apk del openssl ; \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* ; \
    mkdir -p /app

# COPY nginx.conf /etc/nginx/nginx.conf
# COPY ./public /app/public

EXPOSE 80 443

# Run nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
