# Deployment Notes

## Render.com Configuration

This application is now deployed on Render.com instead of Heroku.

### Cost Savings
- Previous (Heroku): $11.63/month
- Current (Render.com Free Tier): $0/month
- Savings: ~$140/year

### Render.com Setup

The application uses:
1. **Web Service** (Free Tier): Auto-deploys from GitHub on every push
2. **PostgreSQL Database** (Free Tier): 256MB storage, up to 5GB backup

### Deployment Flow

1. Push to `main` branch on GitHub
2. Render.com automatically triggers deployment
3. Docker image is built and deployed
4. New version is live within 2-3 minutes

### Database Connection

Environment variables are automatically configured when using Render's PostgreSQL addon:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### Environment Variables

If manually setting up database, configure:
```
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
SPRING_JPA_HBM2DDL=update
JWT_SECRET=<your-secret-key>
```

### Monitoring

Access application logs via Render.com dashboard:
1. Open Web Service
2. Click "Logs" tab
3. View real-time deployment and runtime logs

### Local Development

Continue using Docker Compose for local development:
```bash
docker-compose up -d
```

## Migration from Heroku

Old Heroku-specific scripts have been archived:
- `scripts/set-heroku-db.ps1.bak` - Legacy Heroku database configuration helper
- Not needed for Render.com deployment

## Future Improvements

If needing to scale beyond Free Tier:
- Upgrade Web Service to Starter Plan: $7/month (includes uptime SLA)
- Upgrade Database if exceeding 256MB storage
