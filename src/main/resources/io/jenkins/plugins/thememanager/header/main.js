(function () {
  const themeElement = document.getElementById('theme-manager-theme');
  if (!themeElement) {
    return;
  }

  const theme = JSON.parse(themeElement.text);
  const darkMediaQuery = window.matchMedia?.('(prefers-color-scheme: dark)');

  const currentMode = (isDark = darkMediaQuery?.matches) => (isDark ? 'dark' : 'light');
  let accountThemePickerSelect = null;

  const applyTheme = (mode = currentMode()) => {
    const activeTheme = theme[mode];
    if (activeTheme) {
      document.documentElement.dataset.theme = activeTheme;
    }
  };

  const syncAccountThemePicker = (mode = currentMode()) => {
    const selectEl = accountThemePickerSelect ?? document.querySelector('#account-theme-picker');
    const label = selectEl?.closest("label[for='account-theme-picker']");
    if (!selectEl || !label) {
      return;
    }

    const activeTheme = theme[mode];
    if (activeTheme) {
      selectEl.value = activeTheme;
    }

    const labelEl = label.querySelector('.tm-account-theme-picker-label');
    if (labelEl) {
      labelEl.textContent = mode === 'dark' ? labelEl.dataset.darkLabel : labelEl.dataset.lightLabel;
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
    const mode = currentMode(e.matches);
    applyTheme(mode);
    syncAccountThemePicker(mode);
  });

  const propertiesJson = document.getElementById('theme-manager-properties');
  if (propertiesJson) {
    const parsedProperties = JSON.parse(propertiesJson.text);

    window.getThemeManagerProperty = function (plugin, propertyName) {
      const mode = currentMode();
      const activeProperties = parsedProperties[mode] ?? {};
      const key = `${plugin}:${propertyName}`;
      const modeKey = `${key}-${mode}`;
      return Object.hasOwn(activeProperties, modeKey)
        ? activeProperties[modeKey]
        : activeProperties[key];
    };
  }

  document.addEventListener('DOMContentLoaded', function () {
    const isAppearancePage = document.querySelector("[data-model-type='hudson.model.userproperty.UserPropertyCategoryAppearanceAction']");
    const themesTemplate = document.querySelector("#account-theme-picker-template");
    const userActions = document.querySelector("#root-action-UserAction + template");

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
          syncAccountThemePicker();

          selectEl.addEventListener("change", () => {
            const mode = currentMode();
            theme[mode] = selectEl.value;
            applyTheme(mode);
            syncAccountThemePicker(mode);

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
