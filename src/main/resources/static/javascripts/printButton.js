document.addEventListener('DOMContentLoaded', function () {
    const printButton = document.getElementById('print-button')
    if (printButton) {
        printButton.addEventListener('click', function () {
            window.print()
        })
    }
})