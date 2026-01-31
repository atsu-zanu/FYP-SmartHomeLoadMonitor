@echo off
echo ===============================================
echo   Smart Home Load Monitor
echo   Compile and Run
echo ===============================================
echo.

REM Create bin directory if it doesn't exist
if not exist "bin" mkdir bin

REM Compile
echo [1/2] Compiling...
javac -d bin ^
    --module-path "C:\javafx-sdk-21.0.10\javafx-sdk-21.0.10\lib" ^
    --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
    -sourcepath src\main\java ^
    src\main\java\com\smartload\*.java ^
    src\main\java\com\smartload\models\*.java ^
    src\main\java\com\smartload\services\*.java ^
    src\main\java\com\smartload\simulation\*.java ^
    src\main\java\com\smartload\controllers\*.java

if %errorlevel% neq 0 (
    echo.
    echo *** Compilation failed! ***
    pause
    exit /b 1
)

echo [1/2] Compilation successful!
echo.

REM Copy resources
if exist "src\main\resources" (
    xcopy /E /I /Y src\main\resources bin\resources >nul 2>&1
)

REM Run
echo [2/2] Starting application...
echo ===============================================
echo.
java --module-path "C:\javafx-sdk-21.0.10\javafx-sdk-21.0.10\lib" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
     -cp bin ^
     com.smartload.SmartLoadApp

echo.
pause