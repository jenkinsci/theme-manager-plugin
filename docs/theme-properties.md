# Theme properties

Themes can set properties giving information about themselves.

This can be useful when interfacing with other libraries like Prism or CodeMirror that have their own theming capability but need to know which theme to use.

Instead of making a user select their theme for each library, a theme can say which one works best with it.

## Provider guide

Inside your `#getTheme` method of your `ThemeManagerFactory` call:

`builder#withProperty(artifactId, propertyName, propertyValue)`

```java
public class YourThemeManagerFactory {
    public Theme getTheme() {
        return Theme.builder()
                .withCssUrl(getCssUrl())
                .withProperty("prism-api", "theme", "tomorrow")
                .build();
    }
}
```

The artifactId will be prepended to the property name with a : separator, this is to prevent any clashes between plugins.

## Consumer guide

Retrieve a property for the active theme (automatically resolving light/dark mode) from JavaScript using `window.getThemeManagerProperty`:

```js
const themeName = window.getThemeManagerProperty?.("prism-api", "theme") ?? "prism";
```

## Existing implementations

- [Prism API](https://github.com/jenkinsci/prism-api-plugin/pull/41) - Uses the `theme` property to select which prism theme should be used by default