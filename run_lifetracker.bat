@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out

echo Compiling LifeTracker 1.1.1...
set SOURCES=
for /r SRC %%f in (*.java) do call set SOURCES=%%SOURCES%% "%%f"

javac -encoding UTF-8 -d out %SOURCES%
if errorlevel 1 (
  echo.
  echo Compilation failed. Review the errors above.
  pause
  exit /b 1
)

echo Starting LifeTracker 1.1.1...
java -cp out Main
endlocal
