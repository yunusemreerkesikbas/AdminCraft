FROM node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ARG BUILD_ENV=production
ARG NEXT_OUTPUT=standalone
ARG GOOGLE_SITE_VERIFICATION
ENV NEXT_OUTPUT=$NEXT_OUTPUT
ENV GOOGLE_SITE_VERIFICATION=$GOOGLE_SITE_VERIFICATION

RUN npm run build:${BUILD_ENV}

FROM node:20-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production

COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

RUN addgroup -g 1001 -S nodejs && adduser -S -u 1001 -G nodejs nextjs
USER nextjs

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:3000/api/health || exit 1

CMD ["node", "server.js"]
