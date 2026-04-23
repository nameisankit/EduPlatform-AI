"""
Agent base class and shared utilities
From: bhawsararya/Muti-Agent-Education-System (Agentic AI layer)
"""
from dataclasses import dataclass, field
from typing import Any
from core.llm import generate_content


@dataclass
class AgentMemory:
    """Shared memory store between agents in the pipeline."""
    topic: str = ""
    student_level: str = "intermediate"
    structured_content: dict = field(default_factory=dict)
    curriculum: str = ""
    lesson: str = ""
    quiz: dict = field(default_factory=dict)
    student_answer: str = ""
    feedback: str = ""
    image_prompt: str = ""
    chat_history: list = field(default_factory=list)


class BaseAgent:
    """Base class for all education agents."""

    def __init__(self, name: str, role: str):
        self.name = name
        self.role = role

    def run(self, memory: AgentMemory) -> AgentMemory:
        raise NotImplementedError

    def _llm(self, prompt: str, system: str = "") -> str:
        return generate_content(prompt, system_instruction=system)

    def __repr__(self):
        return f"<Agent: {self.name}>"
