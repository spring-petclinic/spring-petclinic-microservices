const REQUEST_TIMEOUT_MS = 15000;

function isJsonResponse(contentType) {
    const mediaType = contentType.split(';', 1)[0].trim().toLowerCase();
    return mediaType === 'application/json' || mediaType.endsWith('+json');
}

export class ApiError extends Error {
    constructor(message, { status = null, payload = null, timeout = false, cancelled = false } = {}) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.payload = payload;
        this.timeout = timeout;
        this.cancelled = cancelled;
    }
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
    } catch {
        throw new ApiError('The server returned an invalid JSON response.', { status: response.status });
    }
}

function toApiError(error, { timedOut, externalSignal }) {
    if (timedOut) {
        return new ApiError('The request timed out. Please try again.', { timeout: true });
    }
    if (externalSignal?.aborted) {
        return new ApiError('The request was cancelled.', { cancelled: true });
    }
    if (error instanceof ApiError) {
        return error;
    }
    return new ApiError('Unable to reach the server.');
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

    if (externalSignal?.aborted) {
        abortRequest();
    } else {
        externalSignal?.addEventListener('abort', abortRequest, { once: true });
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
        throw toApiError(error, { timedOut, externalSignal });
    } finally {
        clearTimeout(timeout);
        externalSignal?.removeEventListener('abort', abortRequest);
    }

    if (!response.ok) {
        throw new ApiError(`Request failed (${response.status}).`, {
            status: response.status,
            payload: payload && typeof payload === 'object' ? payload : null
        });
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
        throw new ApiError(message);
    }
    return value;
}

export function requireObject(value, message) {
    if (!value || typeof value !== 'object' || Array.isArray(value)) {
        throw new ApiError(message);
    }
    return value;
}
