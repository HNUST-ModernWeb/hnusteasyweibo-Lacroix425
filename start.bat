@echo off
cd /d C:\Users\Lacroix\github-classroom\hnusteasyweibo-Lacroix425
echo Starting Weibo Platform...

echo [1] Starting Backend...
start cmd /c "set JAVA_HOME=C:\Program Files\Java\jdk-17 && cd /d C:\Users\Lacroix\github-classroom\hnusteasyweibo-Lacroix425\weibo-backend && mvn spring-boot:run"

timeout /t 18 /nobreak >nul

echo [2] Starting Frontend...
start cmd /c "cd /d C:\Users\Lacroix\github-classroom\hnusteasyweibo-Lacroix425\weibo-frontend && D:\nodejs\npm.cmd run dev"

echo.
echo Done! http://localhost:5173
pause