"""
Core LLM module - Groq (Llama3) integration

"""
import os
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

client = Groq(api_key=os.getenv("GROQ_API_KEY"))
model = "llama-3.3-70b-versatile"


def generate_content(prompt: str, system_instruction: str = "") -> str:
    """Generate text content using Groq (Llama3)."""
    messages = []
    if system_instruction:
        messages.append({"role": "system", "content": system_instruction})
    messages.append({"role": "user", "content": prompt})

    response = client.chat.completions.create(
        model=model,
        messages=messages
    )
    return response.choices[0].message.content


def generate_structured_content(topic: str) -> dict:
    """Generate structured educational content for a topic."""
    prompt = f"""
    You are an expert educator. For the topic: "{topic}", generate:

    1. EXPLANATION: A clear, structured explanation (3-4 paragraphs)
    2. KEY_POINTS: 5 bullet-point key takeaways
    3. IMAGE_PROMPT: A detailed prompt for AI image generation (describe a visual that explains this concept)
    4. QUIZ_HINT: One thought-provoking question to test understanding

    Format your response EXACTLY as:
    EXPLANATION:
    <explanation here>

    KEY_POINTS:
    - point 1
    - point 2
    - point 3
    - point 4
    - point 5

    IMAGE_PROMPT:
    <image prompt here>

    QUIZ_HINT:
    <question here>
    """
    raw = generate_content(prompt)
    result = {"explanation": "", "key_points": [], "image_prompt": "", "quiz_hint": ""}

    sections = raw.split("\n\n")
    for section in sections:
        if section.startswith("EXPLANATION:"):
            result["explanation"] = section.replace("EXPLANATION:", "").strip()
        elif section.startswith("KEY_POINTS:"):
            lines = section.replace("KEY_POINTS:", "").strip().split("\n")
            result["key_points"] = [line.lstrip("- ").strip() for line in lines if line.strip()]
        elif section.startswith("IMAGE_PROMPT:"):
            result["image_prompt"] = section.replace("IMAGE_PROMPT:", "").strip()
        elif section.startswith("QUIZ_HINT:"):
            result["quiz_hint"] = section.replace("QUIZ_HINT:", "").strip()

    return result
