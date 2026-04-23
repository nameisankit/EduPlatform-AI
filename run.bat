@echo off
title EduPlatform AI - Setup & Run
color 0A

echo.
echo  ============================================
echo   EduPlatform AI - Auto Setup ^& Run
echo  ============================================
echo.

:: Check Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python nahi mila! Python 3.10+ install karo:
    echo  https://www.python.org/downloads/
    pause
    exit /b
)

:: Check if .env exists
if not exist .env (
    echo [SETUP] .env file bana raha hoon...
    copy .env.example .env
    echo.
    echo  *** ZAROORI: .env file kholo aur apni API keys daalo ***
    echo.
    echo  1. GEMINI_API_KEY  = https://aistudio.google.com
    echo  2. HF_TOKEN        = https://huggingface.co/settings/tokens
    echo.
    notepad .env
    echo.
    set /p confirm="Keys dal diye? (y/n): "
    if /i not "%confirm%"=="y" (
        echo Pehle keys daal phir dobara run karo!
        pause
        exit /b
    )
)

:: Create virtualenv if not exists
if not exist venv (
    echo [SETUP] Virtual environment bana raha hoon...
    python -m venv venv
)

:: Activate and install
echo [SETUP] Dependencies install ho rahi hain...
call venv\Scripts\activate
pip install -r requirements.txt -q

echo.
echo  ============================================
echo   App start ho raha hai...
echo   Browser mein khulega: http://localhost:8501
echo  ============================================
echo.

streamlit run app.py
pause
