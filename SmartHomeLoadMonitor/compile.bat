@echo off
echo ===============================================
echo   Smart Home Load Monitor - Compile
echo ===============================================
echo.

REM Create bin directory if it doesn't exist
if not exist "bin" mkdir bin

REM Compile all Java files
echo Compiling Java files...
javac -d bin ^
    --module-path "C:\javafx-sdk-21.0.10\javafx-sdk-21.0.10\lib" ^
    --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
    -sourcepath src\main\java ^
    src\main\java\com\smartload\*.java ^
    src\main\java\com\smartload\models\*.java ^
    src\main\java\com\smartload\services\*.java ^
    src\main\java\com\smartload\simulation\*.java ^
    src\main\java\com\smartload\controllers\*.java

if %errorlevel% equ 0 (
    echo.
    echo ===============================================
    echo   Compilation successful!
    echo ===============================================
    echo.
    echo Run 'run.bat' to start the application
) else (
    echo.
    echo ===============================================
    echo   Compilation failed! Check errors above.
    echo ===============================================
)

pause