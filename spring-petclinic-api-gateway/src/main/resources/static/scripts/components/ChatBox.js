import { nextTick, onMounted, ref, watch } from 'vue';
import DOMPurify from 'dompurify';
import { marked } from 'marked';

import { jsonRequest, request } from '../api/client.js';
import { clearError, reportError } from '../state/errors.js';

function renderMarkdown(content) {
    return DOMPurify.sanitize(marked.parse(content));
}

export const ChatBox = {
    setup() {
        const storageKey = 'petclinic.chat.messages';
        const minimized = ref(false);
        const message = ref('');
        const messages = ref([]);
        const messageContainer = ref(null);

        function save() {
            localStorage.setItem(storageKey, JSON.stringify(messages.value));
        }

        function scrollToBottom() {
            nextTick(() => {
                if (messageContainer.value) {
                    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
                }
            });
        }

        async function send() {
            const query = message.value.trim();
            if (!query) {
                return;
            }

            messages.value.push({
                author: 'user',
                content: query
            });
            message.value = '';
            scrollToBottom();

            const errorContext = clearError();
            try {
                const answer = await request('/api/genai/chatclient', jsonRequest('POST', query));
                messages.value.push({
                    author: 'bot',
                    content: answer
                });
            } catch (error) {
                reportError(error, errorContext);
                messages.value.push({
                    author: 'bot',
                    content: 'Chat is currently unavailable'
                });
            }
            scrollToBottom();
        }

        onMounted(() => {
            try {
                const savedMessages = JSON.parse(localStorage.getItem(storageKey) || '[]');
                if (Array.isArray(savedMessages)) {
                    messages.value = savedMessages.filter((entry) =>
                        entry && (entry.author === 'user' || entry.author === 'bot') && typeof entry.content === 'string');
                }
            } catch {
                // Corrupted saved conversation: discard it and start with an empty chat.
                localStorage.removeItem(storageKey);
            }
            scrollToBottom();
        });

        watch(messages, save, {
            deep: true
        });

        return {
            minimized,
            message,
            messages,
            messageContainer,
            renderMarkdown,
            clearError,
            send
        };
    },
    template: `
        <div class="chatbox" :class="{ minimized: minimized }">
            <button class="chatbox-header" type="button" @click="minimized = !minimized">Chat with Us!</button>
            <div class="chatbox-content">
                <div ref="messageContainer" class="chatbox-messages">
                    <div v-for="(chatMessage, index) in messages" :key="index" class="chat-bubble" :class="chatMessage.author" v-html="renderMarkdown(chatMessage.content)"></div>
                </div>
                <form class="chatbox-footer" @submit.prevent="send">
                    <label class="visually-hidden" for="chat-message">Chat message</label>
                    <input id="chat-message" v-model="message" name="message" type="text" placeholder="Type a message..." @input="clearError">
                    <button type="submit">Send</button>
                </form>
            </div>
        </div>`
};
