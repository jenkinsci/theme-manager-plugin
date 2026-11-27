document.addEventListener("DOMContentLoaded", () => {
    const supportsHas = typeof CSS !== "undefined"
        && typeof CSS.supports === "function"
        && CSS.supports("selector(:has(*))");
    const darkMediaQuery = typeof window.matchMedia === "function"
        ? window.matchMedia("(prefers-color-scheme: dark)")
        : null;

    document.querySelectorAll(".app-theme-selector").forEach((selector) => {
        const toggle = selector.querySelector(".app-theme-selector__system-toggle");
        const staticClass = selector.querySelector(".app-theme-selector__class--static");
        const systemClass = selector.querySelector(".app-theme-selector__class--system");
        const staticWrappers = selector.querySelectorAll(".app-theme-picker__static");
        const systemWrappers = selector.querySelectorAll(".app-theme-picker__system-controls");
        const staticInputs = selector.querySelectorAll(".app-theme-picker__static-input");
        const systemInputs = selector.querySelectorAll(".app-theme-picker__system-input");

        if (!toggle || !staticClass || !systemClass) {
            return;
        }

        const applyPreview = () => {
            let checked;
            if (toggle.checked) {
                const role = darkMediaQuery && darkMediaQuery.matches ? "dark" : "light";
                checked = selector.querySelector(
                    `.app-theme-picker__system-input[data-theme-role="${role}"]:checked`,
                );
            } else {
                checked = selector.querySelector(".app-theme-picker__static-input:checked");
            }
            if (checked) {
                document.documentElement.dataset.theme = checked.dataset.theme;
            }
        };

        const setDisabledGroup = (wrappers, inputs, disabled) => {
            wrappers.forEach((wrapper) => {
                if (disabled) {
                    wrapper.setAttribute("field-disabled", "true");
                } else {
                    wrapper.removeAttribute("field-disabled");
                }
            });
            inputs.forEach((input) => {
                input.disabled = disabled;
            });
        };

        const syncMode = () => {
            const isSystem = toggle.checked;
            toggle.setAttribute("aria-expanded", String(isSystem));

            if (isSystem) {
                staticClass.setAttribute("field-disabled", "true");
                systemClass.removeAttribute("field-disabled");
            } else {
                staticClass.removeAttribute("field-disabled");
                systemClass.setAttribute("field-disabled", "true");
            }

            setDisabledGroup(staticWrappers, staticInputs, isSystem);
            setDisabledGroup(systemWrappers, systemInputs, !isSystem);

            if (!supportsHas) {
                selector.classList.toggle("is-system-theme", isSystem);
            }

            applyPreview();
        };

        if (!supportsHas) {
            selector.classList.toggle("is-system-theme", toggle.checked);
        }

        toggle.addEventListener("change", syncMode);

        selector.querySelectorAll('input[type="radio"]').forEach((item) => {
            item.addEventListener("change", applyPreview);
        });

        if (darkMediaQuery && typeof darkMediaQuery.addEventListener === "function") {
            darkMediaQuery.addEventListener("change", () => {
                if (toggle.checked) {
                    applyPreview();
                }
            });
        }
    });
});
