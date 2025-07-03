(function () {
  const themeJson = document.getElementById('theme-manager-theme').text
  const theme = JSON.parse(themeJson);

  if (theme.id && theme.id !== '') {
    document.documentElement.dataset.theme = theme.id
  }
  window.isSystemRespectingTheme = theme.respect_system_appearance

  const propertiesJson = document.getElementById('theme-manager-properties')
  // may not be present, e.g. on BlueOcean where the theme is not marked as BlueOcean compatible
  if (propertiesJson) {
    const parsedProperties = JSON.parse(propertiesJson.text);

    window.getThemeManagerProperty = function (plugin, propertyName) {
      const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches

      let propertyNameNormalised = propertyName
      if (isSystemRespectingTheme) {
        propertyNameNormalised = isDark ? `${propertyName}-dark` : `${propertyName}-light`
      }
      return parsedProperties[`${plugin}:${propertyNameNormalised}`]
    }
  }
})()

document.addEventListener('DOMContentLoaded', function () {
  const userActions = document.querySelector("#root-action-UserAction")?.nextElementSibling;

  if (!userActions) {
    return;
  }

  // Create the template
  const tpl = document.createElement('template');
  tpl.dataset.dropdownType = 'CUSTOM';

  // Copy the <select> and its options
  const select = document.querySelector("#account-theme-picker-template").content;

  // Add the select to the template
  userActions.content.querySelector('.jenkins-dropdown [data-dropdown-type="SEPARATOR"]').after(select);

  // Add an event listener for the themes
  Behaviour.specify(
      '#account-theme-picker',
      "account-theme-picker",
      0,
      function (e) {
        e.addEventListener("change", () => {
          document.documentElement.dataset.theme = e.value;
          document.querySelector("label[for='account-theme-picker'] span").innerHTML = e.selectedOptions[0].textContent;;
        });
      },
  );
})
