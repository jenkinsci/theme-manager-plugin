(function () {
  const themeJson = document.getElementById('theme-manager-theme').text
  const theme = JSON.parse(themeJson);

  if (theme.id && theme.id !== '') {
    document.documentElement.dataset.theme = theme.id
  }
  window.isSystemRespectingTheme = theme.respect_system_appearance

  const propertiesJson = document.getElementById('theme-manager-properties').text
  const parsedProperties = JSON.parse(propertiesJson);


  window.getThemeManagerProperty = function (plugin, propertyName) {
    const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches

    let propertyNameNormalised = propertyName
    if (isSystemRespectingTheme) {
      propertyNameNormalised = isDark ? `${propertyName}-dark` : `${propertyName}-light`
    }
    return parsedProperties[`${plugin}:${propertyNameNormalised}`]
  }
})()
