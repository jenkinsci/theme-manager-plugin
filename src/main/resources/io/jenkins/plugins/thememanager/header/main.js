(function () {
  const themeElement = document.getElementById('theme-manager-theme');
  if (!themeElement) {
    return;
  }

  const theme = JSON.parse(themeElement.text);
  const darkMediaQuery = typeof window.matchMedia === 'function'
    ? window.matchMedia('(prefers-color-scheme: dark)')
    : null;

  const applyTheme = () => {
    const isDark = Boolean(darkMediaQuery && darkMediaQuery.matches);
    const activeTheme = isDark ? theme.dark : theme.light;
    if (activeTheme && activeTheme !== '') {
      document.documentElement.dataset.theme = activeTheme;
    }
  };

  applyTheme();
  window.isSystemRespectingTheme = true;

  if (darkMediaQuery && typeof darkMediaQuery.addEventListener === 'function') {
    darkMediaQuery.addEventListener('change', () => {
      if (document.querySelector('.app-theme-picker')) {
        return;
      }
      applyTheme();
    });
  }

  const propertiesJson = document.getElementById('theme-manager-properties');
  if (propertiesJson) {
    const parsedProperties = JSON.parse(propertiesJson.text);

    window.getThemeManagerProperty = function (plugin, propertyName) {
      const mode = darkMediaQuery && darkMediaQuery.matches ? 'dark' : 'light';
      const activeProperties = parsedProperties[mode] || {};
      const key = `${plugin}:${propertyName}`;
      if (Object.prototype.hasOwnProperty.call(activeProperties, key)) {
        return activeProperties[key];
      }
      return activeProperties[`${key}-${mode}`];
    };
  }

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

    // Handle keyboard activation for the theme picker.
    document.addEventListener('keydown', function (e) {
      if (e.key !== 'Enter' && e.key !== ' ') {
        return;
      }
      const label = document.querySelector("label[for='account-theme-picker']");
      if (label && (label === document.activeElement || label.classList.contains('jenkins-dropdown__item--selected'))) {
        e.preventDefault();
        document.querySelector('#account-theme-picker').showPicker();
      }
    });

    // Add an event listener for the themes
    Behaviour.specify(
        '#account-theme-picker',
        "account-theme-picker",
        0,
        function (e) {
          e.addEventListener("change", () => {
            theme.light = e.value;
            theme.dark = e.value;
            applyTheme();
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
  });
})();
