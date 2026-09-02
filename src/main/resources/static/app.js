const messages = document.querySelector('#messages');
const chatForm = document.querySelector('#chat-form');
const chatInput = document.querySelector('#chat-input');

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
  appendMessage(text, true);
  chatInput.value = '';
  setTimeout(() => appendMessage('I’ll use that as the starting point for your trip. Once your backend is connected, this response will come from LangChain4j.', false), 450);
}

function appendMessage(text, isUser) {
  const message = document.createElement('div');
  message.className = `message ${isUser ? 'user-message' : 'assistant'}`;
  message.innerHTML = isUser
    ? `<div><span class="message-name">You</span><p>${escapeHtml(text)}</p></div>`
    : `<div class="message-avatar">✦</div><div><span class="message-name">TravelPilot</span><p>${escapeHtml(text)}</p></div>`;
  messages.appendChild(message);
  messages.scrollTop = messages.scrollHeight;
}

function escapeHtml(value) {
  const element = document.createElement('div');
  element.textContent = value;
  return element.innerHTML;
}
