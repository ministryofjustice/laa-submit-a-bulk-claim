document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('submitForm');
    const btn = document.getElementById('submitBtn');
    if (form && btn) {
        form.addEventListener('submit', function () {
            btn.disabled = true;
        });
    }
})