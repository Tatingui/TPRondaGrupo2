@echo off
setlocal enabledelayedexpansion

echo ===============================================
echo   Setup de la base de datos - Ronda Backend
echo ===============================================
echo.

cd /d "%~dp0"

REM --- 0. Verificar Java 17+ -------------------------------------
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] No se encontro Java.
    echo.
    echo Instala el JDK 17 o superior desde https://adoptium.net/
    echo Acordate de reiniciar la terminal despues de instalarlo.
    echo.
    pause
    exit /b 1
)

set "JAVA_VER="
for /f tokens^=3 %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    if not defined JAVA_VER set "JAVA_VER=%%~v"
)
for /f "delims=. tokens=1" %%m in ("!JAVA_VER!") do set "JAVA_MAJOR=%%m"

if !JAVA_MAJOR! LSS 17 (
    echo [ERROR] Se necesita Java 17 o superior. Tenes la version !JAVA_VER!.
    echo.
    echo Descarga el JDK 17+ desde https://adoptium.net/
    echo.
    pause
    exit /b 1
)

echo Java !JAVA_VER! detectado. OK.
echo.

REM --- 1. Buscar mysql.exe -----------------------------------------
set "MYSQL_EXE="

where mysql.exe >nul 2>&1
if %errorlevel%==0 (
    set "MYSQL_EXE=mysql.exe"
    goto :encontrado
)

for /d %%D in ("C:\Program Files\MySQL\MySQL Server *") do (
    if exist "%%D\bin\mysql.exe" set "MYSQL_EXE=%%D\bin\mysql.exe"
)

if not defined MYSQL_EXE (
    for /d %%D in ("C:\Program Files (x86)\MySQL\MySQL Server *") do (
        if exist "%%D\bin\mysql.exe" set "MYSQL_EXE=%%D\bin\mysql.exe"
    )
)

if not defined MYSQL_EXE (
    echo [ERROR] No se encontro mysql.exe.
    echo.
    echo Instala MySQL 8 desde https://dev.mysql.com/downloads/installer/
    echo Si ya lo tenes instalado en otra ruta, agregas su carpeta "bin" al PATH
    echo y volves a correr este script.
    echo.
    pause
    exit /b 1
)

:encontrado
echo Cliente MySQL encontrado:
echo   !MYSQL_EXE!
echo.

REM --- 2. Verificar que el servidor este corriendo ------------------
netstat -ano | findstr /r /c:"LISTENING" | findstr /c:":3306 " >nul
if errorlevel 1 (
    echo [ERROR] No hay nada escuchando en el puerto 3306.
    echo El servidor MySQL no parece estar corriendo.
    echo.
    echo Abri "Servicios" de Windows y arranca el servicio MySQL80,
    echo o ejecuta:  net start MySQL80
    echo.
    pause
    exit /b 1
)

echo Servidor MySQL detectado en el puerto 3306.
echo.

REM --- 3. Ejecutar el script ---------------------------------------
echo Se va a crear:
echo   - la base de datos  "ronda"
echo   - el usuario        "ronda" con password "ronda"
echo.
echo A continuacion se te va a pedir la password de ROOT de MySQL
echo (la que pusiste al instalarlo). No se muestra mientras la escribis.
echo.

"!MYSQL_EXE!" -u root -p -h 127.0.0.1 -P 3306 -e "source setup-db.sql"

if errorlevel 1 (
    echo.
    echo [ERROR] Fallo la ejecucion del script.
    echo.
    echo Causa mas comun: la password de root es incorrecta.
    echo Si no la recordas, se puede resetear. Consultalo con el equipo.
    echo.
    pause
    exit /b 1
)

echo.
echo ===============================================
echo   Listo.
echo ===============================================
echo.
echo Ya podes levantar el backend. Desde esta misma carpeta:
echo.
echo   .\mvnw.cmd spring-boot:run
echo.
echo Y despues abri en el navegador:  http://localhost:8081/api/health
echo.
pause
