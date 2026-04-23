"""
🌐 EduPlatform — Integrated AI Education System
Combines:
  • GenAI (Groq + Pollinations image gen)    [bhawsararya/Education]
  • Multi-Agent AI Pipeline                   [bhawsararya/Muti-Agent-Education-System]
  • DevOps (Docker + CI/CD + AWS)             [nameisankit/education-app]
"""
import streamlit as st

# ── Page config ────────────────────────────────────────────────────────────
st.set_page_config(
    page_title="EduPlatform AI",
    page_icon="🎓",
    layout="wide",
    initial_sidebar_state="expanded",
)

# ── Custom CSS ──────────────────────────────────────────────────────────────
st.markdown("""
<style>
    .main-header {
        background: linear-gradient(135deg, #1e3a5f 0%, #2d6a9f 50%, #1a8c6e 100%);
        padding: 2rem; border-radius: 12px; color: white; margin-bottom: 1.5rem;
        text-align: center;
    }
    .agent-badge {
        display: inline-block; padding: 4px 12px; border-radius: 20px;
        font-size: 0.8em; font-weight: 600; margin: 2px;
    }
    .badge-curriculum { background: #e3f2fd; color: #1565c0; }
    .badge-tutor      { background: #e8f5e9; color: #2e7d32; }
    .badge-quiz       { background: #fff3e0; color: #e65100; }
    .badge-feedback   { background: #f3e5f5; color: #6a1b9a; }
    .badge-genai      { background: #fce4ec; color: #880e4f; }
    .pipeline-step {
        border-left: 4px solid #2d6a9f; padding: 0.5rem 1rem;
        margin: 0.5rem 0; border-radius: 0 8px 8px 0; background: #f8f9fa;
    }
    .stAlert { border-radius: 8px; }
</style>
""", unsafe_allow_html=True)

# ── Header ──────────────────────────────────────────────────────────────────
st.markdown("""
<div class="main-header">
    <h1>🎓 EduPlatform AI</h1>
    <p style="font-size:1.1em; opacity:0.9;">
        Multimodal Learning · Multi-Agent Pipeline · Cloud-Deployed
    </p>
    <div>
        <span class="agent-badge badge-genai">🤖 GenAI (Groq + Pollinations)</span>
        <span class="agent-badge badge-curriculum">📋 Curriculum Agent</span>
        <span class="agent-badge badge-tutor">📖 Tutor Agent</span>
        <span class="agent-badge badge-quiz">❓ Quiz Agent</span>
        <span class="agent-badge badge-feedback">✅ Feedback Agent</span>
    </div>
</div>
""", unsafe_allow_html=True)

# ── Session state ────────────────────────────────────────────────────────────
for key, default in {
    "memory": None,
    "pipeline": None,
    "image": None,
    "tab": "learn",
    "mcq_answers": {},
    "quiz_submitted": False,
    "feedback": "",
    "chat_messages": [],
}.items():
    if key not in st.session_state:
        st.session_state[key] = default


# ── Lazy pipeline init ────────────────────────────────────────────────────────
@st.cache_resource
def get_pipeline():
    from agents.pipeline import EducationPipeline
    return EducationPipeline()


# ── Sidebar ──────────────────────────────────────────────────────────────────
with st.sidebar:
    st.header("⚙️ Learning Settings")

    topic = st.text_input(
        "📚 Enter a Topic",
        placeholder="e.g. Neural Networks, Photosynthesis, Blockchain...",
        help="Any educational topic you want to learn about"
    )

    level = st.select_slider(
        "🎯 Student Level",
        options=["beginner", "intermediate", "advanced"],
        value="intermediate"
    )

    generate_img = st.toggle("🖼️ Generate Visual", value=True)

    st.divider()

    if st.button("🚀 Start Learning!", type="primary", use_container_width=True):
        if not topic.strip():
            st.warning("Please enter a topic first.")
        else:
            pipeline = get_pipeline()
            st.session_state.pipeline = pipeline
            st.session_state.mcq_answers = {}
            st.session_state.quiz_submitted = False
            st.session_state.feedback = ""
            st.session_state.chat_messages = []

            # Run pipeline with progress
            progress = st.progress(0, text="Initializing agents...")

            with st.spinner("🧠 Running multi-agent pipeline..."):
                try:
                    progress.progress(20, "GenAI: Generating structured content...")
                    memory = pipeline.run_full_pipeline(topic, level)
                    progress.progress(80, "Pipeline complete!")

                    if generate_img:
                        progress.progress(90, "🎨 Generating visual...")
                        st.session_state.image = pipeline.generate_image(memory)

                    st.session_state.memory = memory
                    progress.progress(100, "✅ Done!")
                    st.success("Pipeline complete!")
                except Exception as e:
                    st.error(f"Error: {e}")
                    progress.empty()

    st.divider()

    # Pipeline architecture diagram
    st.subheader("🔄 Agent Pipeline")
    for step in [
        ("🤖", "GenAI", "Groq + Pollinations"),
        ("📋", "Curriculum", "Builds roadmap"),
        ("📖", "Tutor", "Delivers lesson"),
        ("❓", "Quiz", "Tests knowledge"),
        ("✅", "Feedback", "Evaluates answers"),
    ]:
        st.markdown(
            f'<div class="pipeline-step"><b>{step[0]} {step[1]}</b><br>'
            f'<small style="color:#555">{step[2]}</small></div>',
            unsafe_allow_html=True,
        )

    st.divider()
    st.caption("🐳 Deployed via Docker + GitHub Actions + AWS EC2")


# ── Main content ──────────────────────────────────────────────────────────────
if st.session_state.memory is None:
    # Landing state
    col1, col2, col3 = st.columns(3)
    with col1:
        st.info(
            "**🤖 GenAI Layer**\n\n"
            "Groq (Llama3) generates structured explanations "
            "and Pollinations.ai creates visual diagrams for every topic."
        )
    with col2:
        st.info(
            "**🤝 Multi-Agent Layer**\n\n"
            "Four specialized AI agents collaborate: "
            "Curriculum Designer → Tutor → Quiz Master → Learning Coach."
        )
    with col3:
        st.info(
            "**🚀 DevOps Layer**\n\n"
            "Dockerized, CI/CD via GitHub Actions, "
            "auto-deployed to AWS EC2 on every push to main."
        )

    st.markdown("---")
    st.markdown("### 👈 Enter a topic in the sidebar and click **Start Learning!**")

else:
    memory = st.session_state.memory

    # Tabs
    tab1, tab2, tab3, tab4, tab5 = st.tabs([
        "📋 Curriculum", "📖 Lesson", "🖼️ Visual", "❓ Quiz", "💬 Ask Tutor"
    ])

    # ── TAB 1: Curriculum ─────────────────────────────────────────────────────
    with tab1:
        st.subheader(f"📋 Learning Roadmap: {memory.topic}")
        col_a, col_b = st.columns([2, 1])
        with col_a:
            st.markdown(memory.curriculum)
        with col_b:
            if memory.structured_content:
                st.subheader("🎯 Key Points")
                for pt in memory.structured_content.get("key_points", []):
                    st.markdown(f"• {pt}")

    # ── TAB 2: Lesson ─────────────────────────────────────────────────────────
    with tab2:
        st.subheader(f"📖 Lesson: {memory.topic}")
        if memory.structured_content.get("explanation"):
            with st.expander("🤖 GenAI Quick Overview", expanded=False):
                st.write(memory.structured_content["explanation"])
        st.markdown("---")
        st.markdown(memory.lesson)

    # ── TAB 3: Visual ─────────────────────────────────────────────────────────
    with tab3:
        st.subheader("🖼️ AI-Generated Visual")
        if st.session_state.image is not None:
            st.image(st.session_state.image, caption=f"Visual: {memory.topic}", use_container_width=True)
        else:
            st.info("Enable '🖼️ Generate Visual' in the sidebar and re-run to get an AI image.")

        if memory.image_prompt:
            with st.expander("📝 Image Prompt Used"):
                st.code(memory.image_prompt)

    # ── TAB 4: Quiz ───────────────────────────────────────────────────────────
    with tab4:
        st.subheader(f"❓ Knowledge Check: {memory.topic}")

        quiz = memory.quiz
        questions = quiz.get("questions", [])

        if not questions:
            st.warning("No quiz questions generated.")
        else:
            if not st.session_state.quiz_submitted:
                with st.form("quiz_form"):
                    for q in questions:
                        st.markdown(f"**Q{q['id']}. {q['question']}**")

                        if q["type"] == "mcq":
                            opts = q.get("options", [])
                            choice = st.radio(
                                f"Select answer for Q{q['id']}",
                                opts,
                                key=f"q{q['id']}",
                                label_visibility="collapsed"
                            )
                            st.session_state.mcq_answers[q["id"]] = choice

                        elif q["type"] == "short_answer":
                            ans = st.text_area(
                                f"Your answer for Q{q['id']}",
                                key=f"qa{q['id']}",
                                label_visibility="collapsed",
                                placeholder="Type your answer here..."
                            )
                            st.session_state.mcq_answers[q["id"]] = ans

                        st.divider()

                    submitted = st.form_submit_button("📤 Submit Quiz", type="primary")

                if submitted:
                    # Build answer summary
                    answer_text = "\n".join([
                        f"Q{qid}: {ans}"
                        for qid, ans in st.session_state.mcq_answers.items()
                    ])

                    with st.spinner("🤔 Feedback Agent evaluating your answers..."):
                        updated_memory = st.session_state.pipeline.get_feedback(
                            memory, answer_text
                        )
                        st.session_state.memory = updated_memory
                        st.session_state.feedback = updated_memory.feedback
                        st.session_state.quiz_submitted = True
                    st.rerun()

            else:
                # Show results
                st.success("Quiz submitted! Here's your feedback:")

                # Quick MCQ results
                for q in questions:
                    if q["type"] == "mcq":
                        pipeline = st.session_state.pipeline
                        selected = st.session_state.mcq_answers.get(q["id"], "")
                        result = pipeline.feedback_agent.evaluate_mcq(q, selected[:1] if selected else "")
                        icon = "✅" if result["correct"] else "❌"
                        st.markdown(f"{icon} **Q{q['id']}**: {q['question']}")
                        if not result["correct"]:
                            st.caption(f"Correct: {result['correct_answer']} — {result['explanation']}")

                st.divider()
                st.subheader("🎯 Detailed Feedback")
                st.markdown(st.session_state.feedback)

                if st.button("🔄 Retake Quiz"):
                    st.session_state.quiz_submitted = False
                    st.session_state.mcq_answers = {}
                    st.rerun()

    # ── TAB 5: Chat ───────────────────────────────────────────────────────────
    with tab5:
        st.subheader(f"💬 Ask the Tutor: {memory.topic}")
        st.caption("Ask any follow-up questions about the lesson")

        # Chat history display
        for msg in st.session_state.chat_messages:
            with st.chat_message(msg["role"]):
                st.markdown(msg["content"])

        # Chat input
        if user_q := st.chat_input("Ask a question about the lesson..."):
            st.session_state.chat_messages.append({"role": "user", "content": user_q})
            with st.chat_message("user"):
                st.markdown(user_q)

            with st.chat_message("assistant"):
                with st.spinner("Tutor is thinking..."):
                    answer = st.session_state.pipeline.tutor_chat(memory, user_q)
                st.markdown(answer)

            st.session_state.chat_messages.append({"role": "assistant", "content": answer})
