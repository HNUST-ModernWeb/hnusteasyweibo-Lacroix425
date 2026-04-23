@echo off
echo Starting Weibo Platform...
echo.

echo [1] Starting Backend...
start cmd /c "cd /d weibo-backend && set JAVA_HOME=C:\Program Files\Java\jdk-17 && mvn spring-boot:run"

echo [2] Waiting...
timeout /t 15 /nobreak >nul

echo [3] Starting Frontend...
start cmd /c "cd /d weibo-frontend && npm run dev"

echo.
echo Done! Open http://localhost:5173
pause