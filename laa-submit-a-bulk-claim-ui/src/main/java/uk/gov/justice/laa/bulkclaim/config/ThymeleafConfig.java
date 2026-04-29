package uk.gov.justice.laa.bulkclaim.config;

import java.util.Collections;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;
import uk.gov.justice.laa.bulkclaim.util.ThymeleafHrefUtils;

/**
 * Configuration class for setting up Thymeleaf template engine in the application. This class
 * defines the necessary beans to configure and manage the Thymeleaf template engine with additional
 * features like Spring Expression Language (Spring EL) and custom dialects.
 */
@Configuration
public class ThymeleafConfig {

  /**
   * Configures and initializes a SpringTemplateEngine bean for rendering Thymeleaf templates.
   * Default setup is done for it, however it excludes ThymeleafHrefUtils from it's restricted
   * classes.
   *
   * @param properties The ThymeleafProperties object that provides configuration options for
   *     Thymeleaf, such as enabling the Spring Expression Language (Spring EL) compiler and
   *     rendering hidden markers before checkboxes.
   * @param templateResolvers A provider of ITemplateResolver instances used for resolving templates
   *     within the Thymeleaf engine.
   * @param dialects A provider of IDialect instances used to add custom processing logic when
   *     rendering templates in Thymeleaf.
   * @return A fully configured SpringTemplateEngine instance with the specified properties,
   *     template resolvers, and dialects.
   */
  @Bean
  SpringTemplateEngine templateEngine(
      ThymeleafProperties properties,
      ObjectProvider<ITemplateResolver> templateResolvers,
      ObjectProvider<IDialect> dialects) {
    SpringTemplateEngine engine = new SpringTemplateEngine();
    // Insert the needed classes
    engine.setAllowedClassOverridesForViews(Collections.singleton(ThymeleafHrefUtils.class));
    // Perform all other initializations
    engine.setEnableSpringELCompiler(properties.isEnableSpringElCompiler());
    engine.setRenderHiddenMarkersBeforeCheckboxes(
        properties.isRenderHiddenMarkersBeforeCheckboxes());
    templateResolvers.orderedStream().forEach(engine::addTemplateResolver);
    dialects.orderedStream().forEach(engine::addDialect);
    return engine;
  }
}
