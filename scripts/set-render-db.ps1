<#
PowerShell helper: configure Render deployment variables
Usage:
  This script provides guidance for setting environment variables on Render.com
  
  1. Go to https://render.com/dashboard
  2. Open your Web Service
  3. Click "Environment" tab
  4. Manually add or update these variables from your database credentials:
     - SPRING_DATASOURCE_URL: postgresql://user:password@host:5432/database
     - SPRING_DATASOURCE_USERNAME: your_db_user
     - SPRING_DATASOURCE_PASSWORD: your_db_password
     - SPRING_JPA_DATABASE_PLATFORM: org.hibernate.dialect.PostgreSQLDialect
     - SPRING_JPA_HBM2DDL: update
     
  4. If using Render PostgreSQL addon, click "Add from Database" for automatic setup
  
Notes:
  - For free tier databases on Render, the connection string can take ~30 seconds to appear
  - Database schema is automatically created on first deploy
  - For production, consider setting SPRING_JPA_HBM2DDL to "none" and using Flyway migrations
#>

Write-Host "Render.com Database Configuration Helper"
Write-Host "=========================================="
Write-Host ""
Write-Host "To set up your Render deployment:"
Write-Host "1. Go to: https://render.com/dashboard"
Write-Host "2. Open your Web Service (task-management-app)"
Write-Host "3. Click 'Environment' tab"
Write-Host "4. Add or update these variables:"
Write-Host ""
Write-Host "   SPRING_DATASOURCE_URL: postgresql://user:password@host:5432/database"
Write-Host "   SPRING_DATASOURCE_USERNAME: your_db_user"
Write-Host "   SPRING_DATASOURCE_PASSWORD: your_db_password"
Write-Host "   SPRING_JPA_DATABASE_PLATFORM: org.hibernate.dialect.PostgreSQLDialect"
Write-Host "   SPRING_JPA_HBM2DDL: update"
Write-Host ""
Write-Host "TIP: If you have Render PostgreSQL addon, use 'Add from Database' button"
Write-Host "     for automatic environment variable setup."
Write-Host ""
Write-Host "More info: https://render.com/docs"
