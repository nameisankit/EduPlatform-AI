"""
Agent Pipeline Orchestrator
From: bhawsararya/Muti-Agent-Education-System (Agentic AI layer)

Coordinates all agents in sequence: Curriculum → Tutor → Quiz → Feedback
"""
from agents.base import AgentMemory
from agents.education_agents import (
    CurriculumAgent,
    TutorAgent,
    QuizAgent,
    FeedbackAgent,
)
from core.llm import generate_structured_content
from core.image_gen import generate_image


class EducationPipeline:
    """
    Full multi-agent education pipeline.

    Flow:
        1. GenAI Content Generation (structured_content + image)
        2. CurriculumAgent → builds roadmap
        3. TutorAgent      → delivers lesson
        4. QuizAgent       → creates quiz
        5. FeedbackAgent   → evaluates answer (on demand)
    """

    def __init__(self):
        self.curriculum_agent = CurriculumAgent()
        self.tutor_agent = TutorAgent()
        self.quiz_agent = QuizAgent()
        self.feedback_agent = FeedbackAgent()

    def run_full_pipeline(
        self, topic: str, student_level: str = "intermediate"
    ) -> AgentMemory:
        """Run the complete pipeline for a topic."""
        memory = AgentMemory(topic=topic, student_level=student_level)

        # Step 1: GenAI — structured content + image prompt
        print(f"[Pipeline] 🧠 Generating structured content for: {topic}")
        memory.structured_content = generate_structured_content(topic)
        memory.image_prompt = memory.structured_content.get("image_prompt", topic)

        # Step 2: Curriculum Agent
        print("[Pipeline] 📋 CurriculumAgent: Building roadmap...")
        memory = self.curriculum_agent.run(memory)

        # Step 3: Tutor Agent
        print("[Pipeline] 📖 TutorAgent: Delivering lesson...")
        memory = self.tutor_agent.run(memory)

        # Step 4: Quiz Agent
        print("[Pipeline] ❓ QuizAgent: Generating quiz...")
        memory = self.quiz_agent.run(memory)

        print("[Pipeline] ✅ Pipeline complete!")
        return memory

    def generate_image(self, memory: AgentMemory):
        """Generate visual for the topic (separate — can be slow)."""
        return generate_image(memory.image_prompt, topic=memory.topic)

    def get_feedback(self, memory: AgentMemory, student_answer: str) -> AgentMemory:
        """Run feedback agent on student's answer."""
        memory.student_answer = student_answer
        return self.feedback_agent.run(memory)

    def tutor_chat(self, memory: AgentMemory, question: str) -> str:
        """Chat with the tutor agent."""
        return self.tutor_agent.chat(memory, question)
