document.addEventListener('DOMContentLoaded', () => {
  const refreshElement = document.querySelector(
      `[data-module="page-refresh-on-submission-status"]`);

  if (refreshElement) {
    const submissionId = refreshElement.getAttribute('data-submission-id');
    // JS is available, so remove the browser-driven meta refresh
    refreshElement.remove();

    const checkSubmissionStatus = async () => {
      try {

        const response = await fetch(`/submission/${submissionId}/status`);
        if (response.ok) {
          const isDone = await response.json();
          console.info('Submission status:', isDone);
          if (isDone === true) {
            window.location.reload();
          }
        } else if (response.status === 404) {
          console.debug(
              'Submission not found yet (404), might not have been parsed yet');
        }
      } catch (error) {
        console.error('Error checking submission status:', error);
      }
    };

    const startPolling = () => {
      setInterval(checkSubmissionStatus, 5000);
    };

    startPolling();
  }

});