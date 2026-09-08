@echo off
cd /d "%~dp0"
echo [1/2] Compiling...
javac -d out src/outerspace/*.java src/outerspace/core/*.java src/outerspace/combat/*.java src/outerspace/player/*.java src/outerspace/enemy/*.java src/outerspace/util/*.java
if errorlevel 1 (
    echo.
    echo *** Compile failed ***
    pause
    exit /b 1
)
echo [2/2] Launching OuterSpace...
java -cp out outerspace.Main
if errorlevel 1 pause
