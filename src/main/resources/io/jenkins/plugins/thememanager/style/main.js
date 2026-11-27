document.addEventListener("DOMContentLoaded", () => {
    const darkMediaQuery = window.matchMedia?.("(prefers-color-scheme: dark)");

    document.querySelectorAll(".app-theme-picker").forEach((picker) => {
        const applyPreview = (isDark = Boolean(darkMediaQuery?.matches)) => {
            const role = isDark ? "dark" : "light";
            const checked = picker.querySelector(
                `.app-theme-picker__input[data-theme-role="${role}"]:checked`,
            );
            if (checked) {
                document.documentElement.dataset.theme = checked.dataset.theme;
            }
        };

        picker.querySelectorAll(".app-theme-picker__input").forEach((input) => {
            input.addEventListener("change", () => applyPreview());
        });

        darkMediaQuery?.addEventListener?.("change", (e) => applyPreview(e.matches));
    });
});
