export function matchesQuery(value, query) {
    if (value === null || value === undefined) {
        return false;
    }

    if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
        return String(value).toLowerCase().includes(query);
    }

    if (Array.isArray(value)) {
        return value.some((item) => matchesQuery(item, query));
    }

    if (typeof value === 'object') {
        return Object.keys(value).some((key) => !key.startsWith('$') && matchesQuery(value[key], query));
    }

    return false;
}
