(function () {
  const themeElement = document.getElementById('theme-manager-theme');
  if (!themeElement) {
    return;
  }

  const theme = JSON.parse(themeElement.text);
  const darkMediaQuery = window.matchMedia?.('(prefers-color-scheme: dark)');

  const getMode = (isDark = Boolean(darkMediaQuery?.matches)) => (isDark ? 'dark' : 'light');
  let accountThemePickerSelect = null;

  const applyTheme = (isDark = Boolean(darkMediaQuery?.matches)) => {
    const activeTheme = theme[getMode(isDark)];
    if (activeTheme) {
      document.documentElement.dataset.theme = activeTheme;
    }
  };

  const syncAccountThemePicker = (
    isDark = Boolean(darkMediaQuery?.matches),
    selectEl = accountThemePickerSelect ?? document.querySelector('#account-theme-picker'),
  ) => {
    const label = selectEl?.closest("label[for='account-theme-picker']");
    if (!selectEl || !label) {
      return;
    }

    const activeTheme = theme[getMode(isDark)];
    if (activeTheme) {
      selectEl.value = activeTheme;
    }

    const labelEl = label.querySelector('.tm-account-theme-picker-label');
    if (labelEl) {
      labelEl.textContent = isDark ? labelEl.dataset.darkLabel : labelEl.dataset.lightLabel;
    }

    const selectedOption = selectEl.selectedOptions[0];
    const valueEl = label.querySelector('.tm-account-theme-picker-value');
    if (valueEl && selectedOption) {
      valueEl.textContent = selectedOption.textContent.trim();
    }

    const iconTemplate = label.querySelector(`[data-theme-icon='${selectEl.value}']`);
    const iconEl = label.querySelector('.jenkins-dropdown__item__icon');
    if (iconEl && iconTemplate) {
      iconEl.innerHTML = iconTemplate.innerHTML;
    }
  };

  applyTheme();
  window.isSystemRespectingTheme = true;

  darkMediaQuery?.addEventListener?.('change', (e) => {
    if (document.querySelector('.app-theme-picker')) {
      return;
    }
    applyTheme(e.matches);
    syncAccountThemePicker(e.matches);
  });

  const propertiesJson = document.getElementById('theme-manager-properties');
  if (propertiesJson) {
    const parsedProperties = JSON.parse(propertiesJson.text);

    window.getThemeManagerProperty = function (plugin, propertyName) {
      const mode = getMode();
      const activeProperties = parsedProperties[mode] ?? {};
      const key = `${plugin}:${propertyName}`;
      return Object.hasOwn(activeProperties, key)
        ? activeProperties[key]
        : activeProperties[`${key}-${mode}`];
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
        document.querySelector('#account-theme-picker')?.showPicker?.();
      }
    });

    // Add an event listener for the themes
    Behaviour.specify(
        '#account-theme-picker',
        "account-theme-picker",
        0,
        function (selectEl) {
          accountThemePickerSelect = selectEl;
          syncAccountThemePicker(undefined, selectEl);

          selectEl.addEventListener("change", () => {
            const mode = getMode();
            theme[mode] = selectEl.value;
            applyTheme();
            syncAccountThemePicker(undefined, selectEl);

            const root = document.head.dataset.rooturl;
            const body = new URLSearchParams({ value: selectEl.value, mode });
            fetch(`${root}/theme/set`, {
              method: 'POST',
              headers: Object.assign({}, crumb.wrap({}), {
                'Content-Type': 'application/x-www-form-urlencoded'
              }),
              body: body.toString()
            });
          });
        },
    );
  });
})();
