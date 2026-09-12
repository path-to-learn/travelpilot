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
    .then((body) => appendTravelPlan(body.travelPlan))
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

function appendTravelPlan(plan) {
  if (!plan) {
    appendMessage('I received your request, but no travel plan was returned.', false, true);
    return;
  }

  const message = document.createElement('div');
  message.className = 'message assistant plan-message';
  message.innerHTML = `
    <div class="message-avatar">✦</div>
    <div class="plan-content">
      <span class="message-name">TravelPilot</span>
      ${renderPlan(plan)}
    </div>`;
  messages.appendChild(message);
  messages.scrollTop = messages.scrollHeight;
}

function renderPlan(plan) {
  const status = plan.status || 'UNKNOWN';
  const statusLabel = {
    READY: 'Ready',
    NEEDS_CLARIFICATION: 'Need more details',
    NO_RESULTS: 'No matches'
  }[status] || 'Travel plan';
  const statusClass = {
    READY: 'ready',
    NEEDS_CLARIFICATION: 'needs-clarification',
    NO_RESULTS: 'no-results'
  }[status] || 'unknown';
  const flights = Array.isArray(plan.recommendedFlights) ? plan.recommendedFlights : [];
  const hotels = Array.isArray(plan.recommendedHotels) ? plan.recommendedHotels : [];
  const assumptions = Array.isArray(plan.assumptions) ? plan.assumptions : [];
  const warnings = Array.isArray(plan.warnings) ? plan.warnings : [];

  return `
    <div class="plan-header"><span class="plan-status ${statusClass}">${statusLabel}</span></div>
    ${plan.summary ? `<p class="plan-summary">${escapeHtml(plan.summary)}</p>` : ''}
    ${plan.clarificationQuestion ? `<p class="plan-question">${escapeHtml(plan.clarificationQuestion)}</p>` : ''}
    ${flights.length ? `<section class="plan-section"><h3>Flights</h3>${flights.map(renderFlight).join('')}</section>` : ''}
    ${hotels.length ? `<section class="plan-section"><h3>Hotels</h3>${hotels.map(renderHotel).join('')}</section>` : ''}
    ${renderListSection('Assumptions', assumptions, 'plan-notes')}
    ${renderListSection('Warnings', warnings, 'plan-notes warning')}
    ${plan.bookingRequired ? '<p class="booking-note">Booking requires your explicit approval.</p>' : ''}`;
}

function renderFlight(flight) {
  const route = `${escapeHtml(flight.origin)} → ${escapeHtml(flight.destination)}`;
  const schedule = `${formatDateTime(flight.departureTime)} – ${formatDateTime(flight.arrivalTime)}`;
  const details = `${escapeHtml(flight.airline)} ${escapeHtml(flight.flightNumber)} · ${flight.stops ?? 0} stop(s) · ${escapeHtml(flight.cabinClass)}`;
  return `<article class="option-card"><div><strong>${route}</strong><span>${details}</span><span>${schedule}</span></div><b>${formatMoney(flight.totalPrice, flight.currency)}</b></article>`;
}

function renderHotel(hotel) {
  const details = `${escapeHtml(hotel.area)} · ${escapeHtml(hotel.city)} · ${hotel.rating ?? '—'}★`;
  const dates = `${escapeHtml(hotel.checkIn)} – ${escapeHtml(hotel.checkOut)}`;
  return `<article class="option-card"><div><strong>${escapeHtml(hotel.name)}</strong><span>${details}</span><span>${dates} · ${hotel.refundable ? 'Refundable' : 'Non-refundable'}</span></div><b>${formatMoney(hotel.nightlyRate, hotel.currency)}<small>/night</small></b></article>`;
}

function renderListSection(title, items, className) {
  if (!items.length) return '';
  return `<section class="${className}"><h3>${title}</h3><ul>${items.map((item) => `<li>${escapeHtml(item)}</li>`).join('')}</ul></section>`;
}

function formatMoney(amount, currency) {
  const number = Number(amount);
  if (!Number.isFinite(number)) return escapeHtml(`${amount ?? ''} ${currency ?? ''}`.trim());
  return escapeHtml(new Intl.NumberFormat(undefined, { style: 'currency', currency: currency || 'USD' }).format(number));
}

function formatDateTime(value) {
  if (!value) return 'Time unavailable';
  return escapeHtml(String(value).replace('T', ' '));
}

function setStatus(label) {
  status.innerHTML = `<i></i> ${label}`;
}

function escapeHtml(value) {
  const element = document.createElement('div');
  element.textContent = value ?? '';
  return element.innerHTML;
}
