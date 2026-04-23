"""
Image generation module - Pollinations.ai (Free, no API key required)
From: bhawsararya/Education (GenAI layer)
"""
import os
import io
import requests
from PIL import Image
from dotenv import load_dotenv

load_dotenv()

# Use Pollinations.ai - completely free, no API key needed


def generate_image(prompt: str, topic: str = "") -> Image.Image | None:
    """Generate an educational image using Pollinations.ai (free)."""
    enhanced_prompt = (
        f"Educational illustration, clean diagram, professional infographic style. "
        f"Topic: {topic}. {prompt}. "
        f"High quality, detailed, suitable for learning material."
    )
    
    # Pollinations.ai API - completely free, no API key needed
    url = f"https://image.pollinations.ai/prompt/{enhanced_prompt}?width=800&height=600&nologo=true"
    
    try:
        response = requests.get(url, timeout=60)
        if response.status_code == 200:
            return Image.open(io.BytesIO(response.content))
        else:
            print(f"Pollinations.ai error: {response.status_code}")
    except Exception as e:
        print(f"Image generation error: {e}")
    
    return None
