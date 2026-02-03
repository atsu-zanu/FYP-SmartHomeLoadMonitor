@echo off
echo ===============================================
echo   Smart Home Load Monitor - Clean Build
echo ===============================================
echo.

echo [1/3] Cleaning old files...
if exist "bin" (
    rmdir /s /q bin
    echo Deleted bin folder
)
mkdir bin
echo.

echo [2/3] Compiling...
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

echo Compilation successful!
echo.

echo [3/3] Copying resources...
if exist "src\main\resources" (
    xcopy /E /I /Y src\main\resources bin\resources >nul 2>&1
    echo Resources copied
)
echo.

echo [4/4] Starting application...
echo ===============================================
echo.
java --module-path "C:\javafx-sdk-21.0.10\javafx-sdk-21.0.10\lib" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
     -cp bin ^
     com.smartload.SmartLoadApp

echo.
pause