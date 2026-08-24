document.querySelectorAll('[data-module="logout-link"]').forEach((link) => {
  link.addEventListener('click', (event) => {
    event.preventDefault()
    document.forms.namedItem('logoutForm')?.requestSubmit()
  })
})