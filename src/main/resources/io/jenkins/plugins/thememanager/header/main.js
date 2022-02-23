const themeJson = document.getElementById('theme-manager-theme').text
const theme = JSON.parse(themeJson);

if (theme.id && theme.id !== '') {
    document.documentElement.dataset.theme = theme.id
}