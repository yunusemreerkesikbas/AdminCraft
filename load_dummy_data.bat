@echo off
echo ========================================
echo AdminCraft Sprint 1 - Dummy Data Loader
echo ========================================
echo.
echo This script will load test data into your AdminCraft database.
echo Make sure your Spring Boot application is stopped before running this.
echo.
pause

echo Loading dummy data into MySQL database...
echo.

REM Using Docker MySQL container
docker exec -i admincraft-mysql mysql -u root -p1234 admincraft-db < dummy_data_insert.sql

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo ✅ SUCCESS! Dummy data loaded successfully
    echo ========================================
    echo.
    echo Test data created:
    echo - 5 Tenants (various industries and languages^)
    echo - 7 Users (different roles and permissions^)
    echo - 5 Content Types (news, tutorials, reviews^)
    echo - 5 Contents (published, draft, scheduled^)
    echo - 6 Media Files (images, logos, screenshots^)
    echo.
    echo You can now:
    echo 1. Start your Spring Boot application: mvn spring-boot:run
    echo 2. Import the Postman collection: AdminCraft_Complete_Postman_Collection.json
    echo 3. Test the APIs with realistic data!
    echo.
) else (
    echo.
    echo ========================================
    echo ❌ ERROR! Failed to load dummy data
    echo ========================================
    echo.
    echo Please check:
    echo 1. Docker container is running: docker ps
    echo 2. Database credentials are correct
    echo 3. MySQL container name is 'admincraft-mysql'
    echo 4. Database name is 'admincraft-db'
    echo.
)

echo.
pause