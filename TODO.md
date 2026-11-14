# TODO

List of things to do to upgrade this plugin to support system theming

- [x] Design an API to support both single and system themes
- [ ] Wire in the new API to work with the existing single theme process
  - [ ] Don't forget that there are 3 places to set the theme
    - [x] system appearance settings
    - [ ] user appearance settings
    - [ ] user dropdown
- [ ] Design a UI for setting whether to follow system theme
- [ ] When following system theme then design new UI for choosing light vs dark mode
  - [ ] Maybe I need to rethink this?? -- decide what is a simpler approach
    - We might want a MODE option where it is light/dark/system
    - The 2 former basically are just single themes, the latter is 2 themes
    - and then we always allow the user to select 2 rather than having 1 vs 2 be selectable based on a UI radio button?
- [ ] Persist light vs dark mode
- [ ] Have the CSS get added correctly so that switching system theme is reflected