document.addEventListener("DOMContentLoaded", () => {
    const darkMediaQuery = typeof window.matchMedia === "function"
        ? window.matchMedia("(prefers-color-scheme: dark)")
        : null;

    document.querySelectorAll(".app-theme-picker").forEach((picker) => {
        const applyPreview = () => {
            const role = darkMediaQuery && darkMediaQuery.matches ? "dark" : "light";
            const checked = picker.querySelector(
                `.app-theme-picker__input[data-theme-role="${role}"]:checked`,
            );
            if (checked) {
                document.documentElement.dataset.theme = checked.dataset.theme;
            }
        };

        picker.querySelectorAll(".app-theme-picker__input").forEach((input) => {
            input.addEventListener("change", applyPreview);
        });

        if (darkMediaQuery && typeof darkMediaQuery.addEventListener === "function") {
            darkMediaQuery.addEventListener("change", applyPreview);
        }
    });
});
