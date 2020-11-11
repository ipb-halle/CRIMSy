FROM cypress/base:10
WORKDIR /app

# dependencies will be installed only if the package files change
COPY cypress.json .
COPY package.json .
COPY package-lock.json .

# setting CI environment varialbe silences install messages on startup
ENV CI=1
RUN npm ci

# verify correct Cypress installation
RUN npx cypress verify
