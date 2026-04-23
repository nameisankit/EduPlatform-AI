"""
Specialized Education Agents

Agents:
  1. CurriculumAgent   — Designs a learning roadmap
  2. TutorAgent        — Delivers the lesson content
  3. QuizAgent         — Generates quiz questions
  4. FeedbackAgent     — Evaluates answers and gives feedback
"""
from agents.base import BaseAgent, AgentMemory


# ─────────────────────────────────────────────────────────────────────────────
# 1. CURRICULUM AGENT
# ─────────────────────────────────────────────────────────────────────────────
class CurriculumAgent(BaseAgent):
    """Designs a structured learning roadmap for the given topic and level."""

    def __init__(self):
        super().__init__("CurriculumAgent", "Curriculum Designer")

    def run(self, memory: AgentMemory) -> AgentMemory:
        prompt = f"""
        You are an expert curriculum designer.

        Topic: {memory.topic}
        Student Level: {memory.student_level}

        Design a structured learning roadmap with:
        1. Prerequisites (what to know before)
        2. Learning Objectives (3-5 clear goals)
        3. Module Outline (4 modules with names and brief descriptions)
        4. Estimated Time (per module)
        5. Recommended Resources (types, not specific links)

        Be practical, structured, and appropriate for the level.
        """
        memory.curriculum = self._llm(prompt)
        return memory


# ─────────────────────────────────────────────────────────────────────────────
# 2. TUTOR AGENT
# ─────────────────────────────────────────────────────────────────────────────
class TutorAgent(BaseAgent):
    """Delivers the actual lesson content in an engaging, adaptive style."""

    def __init__(self):
        super().__init__("TutorAgent", "Subject Matter Expert & Tutor")

    def run(self, memory: AgentMemory) -> AgentMemory:
        prompt = f"""
        You are a world-class tutor teaching: "{memory.topic}"
        Student Level: {memory.student_level}

        Curriculum Context:
        {memory.curriculum[:500] if memory.curriculum else "Not available"}

        Deliver a rich, engaging lesson that includes:

        ## 📖 Introduction
        Hook the student with a real-world application or analogy.

        ## 🧠 Core Concepts
        Explain the 3-4 most important concepts clearly with examples.

        ## 💡 Worked Example
        Walk through a concrete, step-by-step example.

        ## 🔗 Connections
        Connect this topic to related ideas the student might already know.

        ## 📝 Summary
        Concise 3-sentence recap of what was covered.

        Use Markdown formatting. Be engaging and clear.
        """
        memory.lesson = self._llm(prompt)
        return memory

    def chat(self, memory: AgentMemory, question: str) -> str:
        """Handle follow-up questions from the student."""
        history_str = "\n".join(
            [f"{m['role'].upper()}: {m['content']}" for m in memory.chat_history[-6:]]
        )
        prompt = f"""
        You are a patient tutor. The student is learning about: "{memory.topic}"

        Conversation history:
        {history_str}

        Student question: {question}

        Answer helpfully, using examples. If off-topic, gently redirect.
        """
        answer = self._llm(prompt)
        memory.chat_history.append({"role": "student", "content": question})
        memory.chat_history.append({"role": "tutor", "content": answer})
        return answer


# ─────────────────────────────────────────────────────────────────────────────
# 3. QUIZ AGENT
# ─────────────────────────────────────────────────────────────────────────────
class QuizAgent(BaseAgent):
    """Generates adaptive quiz questions based on the lesson."""

    def __init__(self):
        super().__init__("QuizAgent", "Assessment Specialist")

    def run(self, memory: AgentMemory) -> AgentMemory:
        prompt = f"""
        You are an assessment specialist. Based on this lesson about "{memory.topic}":

        {memory.lesson[:800] if memory.lesson else memory.topic}

        Generate a quiz with exactly this JSON structure (return ONLY valid JSON):
        {{
          "questions": [
            {{
              "id": 1,
              "type": "mcq",
              "question": "...",
              "options": ["A) ...", "B) ...", "C) ...", "D) ..."],
              "correct": "A",
              "explanation": "..."
            }},
            {{
              "id": 2,
              "type": "mcq",
              "question": "...",
              "options": ["A) ...", "B) ...", "C) ...", "D) ..."],
              "correct": "B",
              "explanation": "..."
            }},
            {{
              "id": 3,
              "type": "short_answer",
              "question": "...",
              "sample_answer": "...",
              "keywords": ["keyword1", "keyword2", "keyword3"]
            }}
          ]
        }}

        Level: {memory.student_level}. Make questions test real understanding, not just memory.
        """
        import json
        import re
        raw = self._llm(prompt)
        # Strip markdown code fences if present
        raw = re.sub(r"```json|```", "", raw).strip()
        try:
            memory.quiz = json.loads(raw)
        except Exception:
            # Fallback quiz
            memory.quiz = {
                "questions": [
                    {
                        "id": 1,
                        "type": "short_answer",
                        "question": f"Explain the core concept of {memory.topic} in your own words.",
                        "sample_answer": "See lesson for reference",
                        "keywords": [memory.topic.lower()]
                    }
                ]
            }
        return memory


# ─────────────────────────────────────────────────────────────────────────────
# 4. FEEDBACK AGENT
# ─────────────────────────────────────────────────────────────────────────────
class FeedbackAgent(BaseAgent):
    """Evaluates student answers and provides constructive feedback."""

    def __init__(self):
        super().__init__("FeedbackAgent", "Learning Coach")

    def run(self, memory: AgentMemory) -> AgentMemory:
        quiz_str = str(memory.quiz)[:600]
        prompt = f"""
        You are a supportive learning coach evaluating a student's quiz response.

        Topic: {memory.topic}
        Student Level: {memory.student_level}

        Quiz Questions:
        {quiz_str}

        Student's Answer:
        {memory.student_answer}

        Provide feedback that includes:

        ## ✅ What You Got Right
        Acknowledge correct understanding.

        ## 🔧 Areas to Improve
        Identify gaps clearly but kindly.

        ## 💡 Deeper Insights
        Add 1-2 insights that extend their understanding.

        ## 🎯 Next Steps
        Suggest 2 specific things to study next.

        Score: X/10 with brief justification.
        Be encouraging and constructive.
        """
        memory.feedback = self._llm(prompt)
        return memory

    def evaluate_mcq(self, question: dict, student_choice: str) -> dict:
        """Evaluate a single MCQ answer."""
        correct = question.get("correct", "")
        is_correct = student_choice.upper().startswith(correct.upper())
        return {
            "correct": is_correct,
            "selected": student_choice,
            "correct_answer": correct,
            "explanation": question.get("explanation", "")
        }
