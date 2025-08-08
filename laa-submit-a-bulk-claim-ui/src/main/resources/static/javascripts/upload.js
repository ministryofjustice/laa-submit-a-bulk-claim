document.addEventListener('DOMContentLoaded', function () {
    const selectedTab = document.getElementById('selected-tab');
    if (selectedTab && selectedTab.value === 'search') {
        const searchTab = document.getElementById("tab_search-upload");
        if (searchTab) {
            searchTab.click();
        }
    }
})