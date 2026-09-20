@echo off
chcp 65001 >nul
setlocal
pushd "%~dp0"
echo Compiling LifeTracker 1.1.3 Functional Touch UI...
if exist out rmdir /s /q out
mkdir out
cd SRC
javac -encoding UTF-8 -d ..\out Main.java account\*.java classes\*.java easteregg\*.java GPA\*.java grades\*.java portfolio\*.java security\*.java storage\*.java UI\*.java
if errorlevel 1 (
  echo.
  echo Compilation failed.
  pause
  popd
  exit /b 1
)
cd ..
echo Starting LifeTracker 1.1.3...
java -cp out Main
popd
endlocal
