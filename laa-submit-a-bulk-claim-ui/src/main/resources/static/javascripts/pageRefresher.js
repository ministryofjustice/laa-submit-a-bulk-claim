document.addEventListener('DOMContentLoaded', () => {
  const refreshElement = document.querySelector(
      `[data-module="page-refresh-on-submission-status"]`);

  if (refreshElement) {
    // JS is available, so remove the browser-driven meta refresh
    refreshElement.remove();

    const checkSubmissionStatus = async () => {
      try {
        const response = await fetch(`/upload-is-being-checked/status`);

        if (response.ok) {
          const isDone = await response.json();
          if (isDone === true) {
            window.location.reload();
          }
          return;
        }

        if (response.status !== 404) {
          console.warn(`Unexpected response status: ${response.status}`);
        }
      } catch (error) {
        console.error('Error checking submission status:', error);
      }
    };

    // Start running checkSubmissionStatus every 5 seconds
    checkSubmissionStatus();
    setInterval(checkSubmissionStatus, 5000);
  }

});