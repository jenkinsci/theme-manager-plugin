(function () {
  const themeJson = document.getElementById('theme-manager-theme').text
  const theme = JSON.parse(themeJson);

  if (theme.id && theme.id !== '') {
    document.documentElement.dataset.theme = theme.id
  }
  window.isSystemRespectingTheme = theme.respect_system_appearance

  const propertiesJson = document.getElementById('theme-manager-properties')
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
  const isAppearancePage = document.querySelector("[data-model-type='hudson.model.userproperty.UserPropertyCategoryAppearanceAction']");
  const themesTemplate = document.querySelector("#account-theme-picker-template");
  let userActions = document.querySelector("#root-action-UserAction + template");

  if (isAppearancePage || !themesTemplate || !userActions) {
    return;
  }

  // Copy the <select> and its options and add it to the account menu
  const select = themesTemplate.content;
  userActions.content.querySelector('.jenkins-dropdown [data-dropdown-type="SEPARATOR"]').after(select);

  // Add an event listener for the themes
  Behaviour.specify(
      '#account-theme-picker',
      "account-theme-picker",
      0,
      function (e) {
        e.addEventListener("change", () => {
          document.documentElement.dataset.theme = e.value;
          document.querySelector("label[for='account-theme-picker'] span").innerHTML = e.selectedOptions[0].textContent;
          document.querySelector("label[for='account-theme-picker'] .jenkins-dropdown__item__icon").innerHTML =
              document.querySelector("[data-theme-icon='" + e.value + "']").innerHTML;

          const root = document.head.dataset.rooturl
          fetch(root + '/theme/set', {
            method: 'POST',
            headers: Object.assign({}, crumb.wrap({}), {
              'Content-Type': 'application/x-www-form-urlencoded'
            }),
            body: 'value=' + e.value
          });
        });
      },
  );
})
