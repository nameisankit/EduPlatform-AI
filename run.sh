#!/bin/bash

echo ""
echo "============================================"
echo "  EduPlatform AI - Auto Setup & Run"
echo "============================================"
echo ""

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "[ERROR] Python3 nahi mila! Install karo:"
    echo "  Mac:   brew install python3"
    echo "  Linux: sudo apt install python3 python3-pip"
    exit 1
fi

# .env check
if [ ! -f .env ]; then
    echo "[SETUP] .env file bana raha hoon..."
    cp .env.example .env
    echo ""
    echo "*** ZAROORI: .env file mein apni API keys daalo ***"
    echo ""
    echo "  1. GEMINI_API_KEY  = https://aistudio.google.com"
    echo "  2. HF_TOKEN        = https://huggingface.co/settings/tokens"
    echo ""
    echo "  .env file kholo aur keys daalo, phir Enter dabaao..."
    read -p "Press Enter when done: "
fi

# Virtual environment
if [ ! -d "venv" ]; then
    echo "[SETUP] Virtual environment bana raha hoon..."
    python3 -m venv venv
fi

# Activate
source venv/bin/activate

# Install deps
echo "[SETUP] Dependencies install ho rahi hain..."
pip install -r requirements.txt -q

echo ""
echo "============================================"
echo "  App start ho raha hai!"
echo "  Browser mein kholo: http://localhost:8501"
echo "  Band karne ke liye: Ctrl+C"
echo "============================================"
echo ""

streamlit run app.py
