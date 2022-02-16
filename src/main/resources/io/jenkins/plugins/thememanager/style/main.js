document.addEventListener("DOMContentLoaded", () => {

    const appThemePicker = document.getElementsByClassName('app-theme-picker')[0]

    appThemePicker.querySelectorAll('input[type="radio"]')
        .forEach((item) => {
            item.addEventListener('click', (e) => {
                document.documentElement.dataset.theme = e.target.dataset.theme
            });
        });
});