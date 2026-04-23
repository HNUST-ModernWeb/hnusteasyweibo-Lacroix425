@echo off
echo Starting Weibo Platform...
echo.

echo [1/4] Starting Backend...
start "Backend" cmd /c "cd /d C:\Users\Lacroix\github-classroom\hnusteasyweibo-Lacroix425\weibo-backend && set JAVA_HOME=C:\Program Files\Java\jdk-17 && C:\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run"

echo [2/4] Waiting for backend to start...
timeout /t 20 /nobreak >nul

echo [3/4] Starting Frontend...
start "Frontend" cmd /c "cd /d C:\Users\Lacroix\github-classroom\hnusteasyweibo-Lacroix425\weibo-frontend && D:\nodejs\npm.cmd run dev"

echo.
echo ========================================
echo Platform Started!
echo   Backend: http://localhost:8080
echo   Frontend: http://localhost:5173
echo ========================================
echo.
echo Press any key to open in browser...
pause >nul

start http://localhost:5173