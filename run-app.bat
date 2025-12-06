@echo off
echo Starting TaskManagementApp...
echo.
echo Note: This script assumes you have Java 17 and Maven installed and configured in your PATH.
echo If you encounter any issues, please ensure Java 17 and Maven are properly installed.
echo.

cd backend
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Build failed. Please check the error messages above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Build successful! Starting the application...
echo.
call mvn spring-boot:run
pause