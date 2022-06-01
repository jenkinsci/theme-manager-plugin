package io.jenkins.plugins.thememanager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;
import org.junit.Test;

public class ThemeTest {

  @Test
  public void getPropertyReturnsKeyIfPresent() {
    Theme theme = Theme.builder().withProperty("prism-api", "theme", "some-theme").build();

    Optional<String> property = theme.getProperty("prism-api", "theme");
    assertThat(property, is(Optional.of("some-theme")));
  }

  @Test
  public void getPropertyReturnsOptionalIfNotPresent() {
    Theme theme = Theme.builder().build();

    Optional<String> property = theme.getProperty("prism-api", "theme");
    assertThat(property, is(Optional.empty()));
  }
}
