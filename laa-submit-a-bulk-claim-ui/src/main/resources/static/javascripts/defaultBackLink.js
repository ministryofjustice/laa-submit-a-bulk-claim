document.querySelectorAll('[data-module="default-back-link"]').forEach(function(link) {
    link.addEventListener('click', function(e) {
        e.preventDefault();
        window.history.back();
    });
});