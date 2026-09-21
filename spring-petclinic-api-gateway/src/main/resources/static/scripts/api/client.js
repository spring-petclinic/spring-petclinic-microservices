const REQUEST_TIMEOUT_MS = 15000;

function isJsonResponse(contentType) {
    const mediaType = contentType.split(';', 1)[0].trim().toLowerCase();
    return mediaType === 'application/json' || mediaType.endsWith('+json');
}

async function readPayload(response) {
    const body = await response.text();
    if (!body) {
        return null;
    }

    if (!isJsonResponse(response.headers.get('content-type') || '')) {
        return body;
    }

    try {
        return JSON.parse(body);
    } catch (error) {
        throw {
            message: 'The server returned an invalid JSON response.',
            status: response.status,
            payload: null
        };
    }
}

export async function request(url, options = {}) {
    const { signal: externalSignal, ...fetchOptions } = options;
    const controller = new AbortController();
    let timedOut = false;
    const timeout = setTimeout(() => {
        timedOut = true;
        controller.abort();
    }, REQUEST_TIMEOUT_MS);
    const abortRequest = () => controller.abort();

    if (externalSignal) {
        if (externalSignal.aborted) {
            abortRequest();
        } else {
            externalSignal.addEventListener('abort', abortRequest, { once: true });
        }
    }

    let response;
    let payload;

    try {
        response = await fetch(url, {
            ...fetchOptions,
            signal: controller.signal
        });
        payload = await readPayload(response);
    } catch (error) {
        if (timedOut) {
            throw { message: 'The request timed out. Please try again.', status: null, timeout: true };
        }
        if (externalSignal && externalSignal.aborted) {
            throw { message: 'The request was cancelled.', status: null, cancelled: true };
        }
        if (error && typeof error === 'object' && 'message' in error && 'status' in error) {
            throw error;
        }
        throw { message: 'Unable to reach the server.', status: null };
    } finally {
        clearTimeout(timeout);
        if (externalSignal) {
            externalSignal.removeEventListener('abort', abortRequest);
        }
    }

    if (!response.ok) {
        throw {
            message: `Request failed (${response.status}).`,
            status: response.status,
            payload: payload && typeof payload === 'object' ? payload : null
        };
    }

    return payload;
}

export function jsonRequest(method, body) {
    return {
        method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    };
}

export function requireArray(value, message) {
    if (!Array.isArray(value)) {
        throw { message };
    }
    return value;
}

export function requireObject(value, message) {
    if (!value || typeof value !== 'object' || Array.isArray(value)) {
        throw { message };
    }
    return value;
}
