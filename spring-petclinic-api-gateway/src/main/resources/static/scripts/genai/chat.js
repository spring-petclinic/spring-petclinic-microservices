function appendMessage(message, type) {
    const chatMessages = document.getElementById('chatbox-messages');
    const messageElement = document.createElement('div');
    messageElement.classList.add('chat-bubble', type);

    // Convert Markdown to HTML
    const htmlContent = marked.parse(message); // Use marked.parse() for newer versions
    messageElement.innerHTML = htmlContent;

    chatMessages.appendChild(messageElement);

    // Scroll to the bottom of the chatbox to show the latest message
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function toggleChatbox() {
    const chatbox = document.getElementById('chatbox');
    const chatboxContent = document.getElementById('chatbox-content');

    if (chatbox.classList.contains('minimized')) {
        chatbox.classList.remove('minimized');
        chatboxContent.style.height = '400px'; // Set to initial height when expanded
    } else {
        chatbox.classList.add('minimized');
        chatboxContent.style.height = '40px'; // Set to minimized height
    }
}

function sendMessage() {
    const query = document.getElementById('chatbox-input').value;

    // Only send if there's a message
    if (!query.trim()) return;

    // Clear the input field after sending the message
    document.getElementById('chatbox-input').value = '';

    // Display user message in the chatbox
    appendMessage(query, 'user');

    // Send the message to the backend
    fetch('/api/genai/chatclient', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(query),
    })
        .then(response => response.text())
        .then(responseText => {
            // Display the response from the server in the chatbox
            appendMessage(responseText, 'bot');
        })
        .catch(error => {
            console.error('Error:', error);
            // Display the fallback message in the chatbox
            appendMessage('Chat is currently unavailable', 'bot');
        });
}

function handleKeyPress(event) {
    if (event.key === "Enter") {
        event.preventDefault(); // Prevents adding a newline
        sendMessage(); // Send the message when Enter is pressed
    }
}

// Save chat messages to localStorage
function saveChatMessages() {
    const messages = document.getElementById('chatbox-messages').innerHTML;
    localStorage.setItem('chatMessages', messages);
}

// Load chat messages from localStorage
function loadChatMessages() {
    const messages = localStorage.getItem('chatMessages');
    if (messages) {
        document.getElementById('chatbox-messages').innerHTML = messages;
        document.getElementById('chatbox-messages').scrollTop = document.getElementById('chatbox-messages').scrollHeight;
    }
}
