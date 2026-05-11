/**
 * EduPlatform AI — Frontend JS
 * Handles: tab switching, markdown rendering, chat AJAX, loading animation
 */

// ─── Markdown Rendering ───────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {
    console.log('App.js loaded');

    // Hide loading overlay if it's visible
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.style.display = 'none';
        console.log('Loading overlay hidden');
    }

    // Configure marked options
    if (typeof marked !== 'undefined') {
        marked.setOptions({
            breaks: true,
            gfm: true,
        });

        // Render all markdown-body elements with data-md attribute
        document.querySelectorAll('.markdown-body[data-md]').forEach(el => {
            const raw = el.getAttribute('data-md');
            if (raw && raw.trim()) {
                el.innerHTML = marked.parse(raw);
            }
        });
    }

    // ─── Tab Switching ─────────────────────────────────────────────────────
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    console.log('Found ' + tabBtns.length + ' tab buttons');
    console.log('Found ' + tabContents.length + ' tab content panels');

    tabBtns.forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            const target = this.getAttribute('data-tab');
            console.log('Tab clicked: ' + target);

            // Update buttons
            tabBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');

            // Update content panels
            tabContents.forEach(c => c.classList.remove('active'));
            const targetContent = document.getElementById('tab-' + target);
            if (targetContent) {
                targetContent.classList.add('active');

                // Re-render markdown in newly visible tab
                if (typeof marked !== 'undefined') {
                    targetContent.querySelectorAll('.markdown-body[data-md]').forEach(el => {
                        if (!el.hasAttribute('data-rendered')) {
                            const raw = el.getAttribute('data-md');
                            if (raw && raw.trim()) {
                                el.innerHTML = marked.parse(raw);
                                el.setAttribute('data-rendered', '1');
                            }
                        }
                    });
                }
            } else {
                console.error('Tab content not found: tab-' + target);
            }
        });
    });

    // Mark all already-rendered ones so they don't get re-rendered
    document.querySelectorAll('.markdown-body[data-md]').forEach(el => {
        el.setAttribute('data-rendered', '1');
    });

    // ─── Auto-scroll chat to bottom ────────────────────────────────────────
    const chatMessages = document.getElementById('chatMessages');
    if (chatMessages) {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // ─── Loading animation steps ─────────────────────────────────────────
    // Cycle through steps if loading overlay is visible
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay && loadingOverlay.style.display !== 'none') {
        animateLoadingSteps();
    }
});

// ─── Show Loading Overlay ─────────────────────────────────────────────────
function showLoading() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.style.display = 'flex';
        animateLoadingSteps();
    }
}

function animateLoadingSteps() {
    const steps = ['ls1', 'ls2', 'ls3', 'ls4'];
    let current = 0;

    // Reset all
    steps.forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.classList.remove('active', 'done');
        }
    });

    // Activate first
    const activate = (idx) => {
        if (idx >= steps.length) return;
        const el = document.getElementById(steps[idx]);
        if (!el) return;

        // Mark previous as done
        if (idx > 0) {
            const prev = document.getElementById(steps[idx - 1]);
            if (prev) {
                prev.classList.remove('active');
                prev.classList.add('done');
            }
        }
        el.classList.add('active');
    };

    // Step through with delays matching typical pipeline timing
    const delays = [0, 4000, 10000, 16000];
    delays.forEach((delay, idx) => {
        setTimeout(() => activate(idx), delay);
    });
}

// ─── Chat AJAX ────────────────────────────────────────────────────────────
async function sendChat() {
    const input = document.getElementById('chatInput');
    if (!input) return;

    const question = input.value.trim();
    if (!question) return;

    input.value = '';
    const container = document.getElementById('chatMessages');
    if (!container) return;

    // Add user bubble immediately
    appendChatBubble(container, 'user', question);

    // Show typing indicator
    const typingId = 'typing-' + Date.now();
    appendChatBubble(container, 'assistant', '💭 Thinking...', typingId);
    container.scrollTop = container.scrollHeight;

    try {
        const response = await fetch('/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ question })
        });

        const data = await response.json();

        // Remove typing indicator
        const typingEl = document.getElementById(typingId);
        if (typingEl) typingEl.parentElement.remove();

        if (data.answer) {
            appendChatBubble(container, 'assistant', data.answer);
        } else if (data.error) {
            appendChatBubble(container, 'assistant', '❌ Error: ' + data.error);
        }

    } catch (err) {
        const typingEl = document.getElementById(typingId);
        if (typingEl) typingEl.parentElement.remove();
        appendChatBubble(container, 'assistant', '❌ Network error. Please try again.');
    }

    container.scrollTop = container.scrollHeight;
}

function appendChatBubble(container, role, content, id) {
    const bubble = document.createElement('div');
    bubble.className = 'chat-bubble ' + (role === 'user' ? 'chat-user' : 'chat-assistant');

    const avatar = document.createElement('div');
    avatar.className = 'chat-avatar';
    avatar.textContent = role === 'user' ? '👤' : '🤖';

    const text = document.createElement('div');
    text.className = 'chat-text markdown-body';
    if (id) text.id = id;

    // Render markdown if marked is available
    if (typeof marked !== 'undefined' && role === 'assistant') {
        text.innerHTML = marked.parse(content);
    } else {
        text.textContent = content;
    }

    bubble.appendChild(avatar);
    bubble.appendChild(text);
    container.appendChild(bubble);
}

// Allow pressing Enter in chat input
document.addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && document.activeElement === document.getElementById('chatInput')) {
        sendChat();
    }
});
