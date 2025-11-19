@echo off
REM Script to build Windows EXE for HealBot Parser using GraalVM

echo Setting up environment...
REM Assuming GraalVM is installed and JAVA_HOME is set
REM If not, install GraalVM from https://www.graalvm.org/downloads/

echo Building with Maven...
call mvn clean package

if %errorlevel% neq 0 (
    echo Maven build failed!
    exit /b 1
)

echo Building native image...
call native-image --no-fallback -jar target\healbot-parser-0.1.0-SNAPSHOT.jar healbot-parser

if %errorlevel% neq 0 (
    echo Native image build failed!
    exit /b 1
)

echo EXE created: healbot-parser.exe
echo Done!