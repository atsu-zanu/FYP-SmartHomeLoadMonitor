@echo off
echo ===============================================
echo   Smart Home Load Monitor - Starting...
echo ===============================================
echo.

REM Check if compiled
if not exist "bin\com\smartload\SmartLoadApp.class" (
    echo Error: Application not compiled!
    echo Please run 'compile.bat' first.
    echo.
    pause
    exit /b 1
)

REM Copy resources to bin directory
if exist "src\main\resources" (
    echo Copying resources...
    xcopy /E /I /Y src\main\resources bin\resources >nul 2>&1
)

REM Run the application
echo Starting Smart Home Load Monitor...
echo.
java --module-path "C:\javafx-sdk-21.0.10\javafx-sdk-21.0.10\lib" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
     -cp bin ^
     com.smartload.SmartLoadApp

echo.
echo ===============================================
echo   Application closed
echo ===============================================
pause