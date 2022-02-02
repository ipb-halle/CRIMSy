FROM cypress/included:9.4.1
WORKDIR /app

# dependencies will be installed only if the package files change
COPY cypress.json .
COPY package.json .

# setting CI environment varialbe silences install messages on startup
ENV CI=1
RUN npm install
RUN npm ci

# verify correct Cypress installation
RUN npx cypress verify

CMD npx cypress run 
