@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME\bin;C:\apache-maven-3.9.6\bin;%PATH%

echo Starting Weibo Platform Backend...
cd /d C:\Users\Lacroix\github-classroom\hnusteasyweibo-Lacroix425\weibo-backend
start "Backend" cmd /c "mvn spring-boot:run"

timeout /t 20 /nobreak >nul

echo Starting Frontend...
cd /d C:\Users\Lacroix\github-classroom\hnusteasyweibo-Lacroix425\weibo-frontend
start "Frontend" cmd /c "D:\nodejs\npm.cmd run dev"

echo.
echo Done! http://localhost:5173
pause