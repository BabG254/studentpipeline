@echo off
REM Script to create data processing directory on Windows

SET "DATA_DIR=C:\var\log\applications\API\dataprocessing"

echo Creating data directory: %DATA_DIR%

if not exist "%DATA_DIR%" (
    mkdir "%DATA_DIR%" 2>nul
    if exist "%DATA_DIR%" (
        echo Successfully created directory: %DATA_DIR%
    ) else (
        echo Failed to create directory: %DATA_DIR%
        echo Please run as administrator or use a different path
        exit /b 1
    )
) else (
    echo Directory already exists: %DATA_DIR%
)

echo Setting permissions...
icacls "%DATA_DIR%" /grant Everyone:(F) 2>nul

echo Data directory setup complete!
echo.
echo You can now start the Student Data Pipeline backend.
echo Set environment variable DATAPATH_BASE to use a different path.

pause