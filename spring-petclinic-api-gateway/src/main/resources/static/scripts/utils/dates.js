export function today() {
    return toDateInputValue(new Date());
}

export function formatDate(value) {
    if (!value) {
        return '';
    }

    const date = new Date(value);
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return `${date.getFullYear()} ${months[date.getMonth()]} ${String(date.getDate()).padStart(2, '0')}`;
}

export function toDateInputValue(value) {
    if (!value) {
        return '';
    }

    const date = new Date(value);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}
