const messages = document.querySelector('#messages');
const chatForm = document.querySelector('#chat-form');
const chatInput = document.querySelector('#chat-input');
const sendButton = chatForm.querySelector('button[type="submit"]');
const status = document.querySelector('.status');
const conversationId = window.crypto?.randomUUID?.() || `conversation-${Date.now()}`;
let isSending = false;

document.querySelectorAll('.tab').forEach((tab) => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach((item) => item.classList.remove('active'));
    tab.classList.add('active');
    if (tab.dataset.tab !== 'chat') {
      appendMessage(`${tab.textContent} will connect to your Spring Boot API later. We can keep the conversation here for now.`, false);
    }
  });
});

document.querySelectorAll('[data-prompt]').forEach((button) => {
  button.addEventListener('click', () => sendMessage(button.dataset.prompt));
});

chatForm.addEventListener('submit', (event) => {
  event.preventDefault();
  const text = chatInput.value.trim();
  if (text) sendMessage(text);
});

function sendMessage(text) {
  if (isSending) return;
  appendMessage(text, true);
  chatInput.value = '';
  isSending = true;
  chatInput.disabled = true;
  sendButton.disabled = true;
  setStatus('Thinking…');

  fetch('/api/v1/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ conversationId, message: text })
  })
    .then(async (response) => {
      const body = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(body.detail || body.message || `Request failed with status ${response.status}`);
      }
      return body;
    })
    .then((body) => appendMessage(body.chatResponse || body.answer || 'I received your request, but no answer was returned.', false))
    .catch(() => appendMessage('I could not reach the travel assistant. Check that Spring Boot is running and that the selected LLM provider and model are available.', false, true))
    .finally(() => {
      isSending = false;
      chatInput.disabled = false;
      sendButton.disabled = false;
      chatInput.focus();
      setStatus('Ready');
    });
}

function appendMessage(text, isUser, isError = false) {
  const message = document.createElement('div');
  message.className = `message ${isUser ? 'user-message' : 'assistant'}${isError ? ' error-message' : ''}`;
  message.innerHTML = isUser
    ? `<div><span class="message-name">You</span><p>${escapeHtml(text)}</p></div>`
    : `<div class="message-avatar">✦</div><div><span class="message-name">TravelPilot</span><p>${escapeHtml(text)}</p></div>`;
  messages.appendChild(message);
  messages.scrollTop = messages.scrollHeight;
}

function setStatus(label) {
  status.innerHTML = `<i></i> ${label}`;
}

function escapeHtml(value) {
  const element = document.createElement('div');
  element.textContent = value;
  return element.innerHTML;
}
