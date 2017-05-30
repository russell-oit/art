/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.springconfig;

import art.report.StringToReport;
import art.usergroup.StringToUserGroup;
import art.utils.StringToDouble;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Provides spring configuration
 *
 * @author Timothy Anyona
 */
@Configuration
@EnableWebMvc
@ComponentScan("art")
public class AppConfig extends WebMvcConfigurerAdapter implements ApplicationContextAware {

	@Autowired
	private StringToUserGroup stringToUserGroup;

	@Autowired
	private StringToReport stringToReport;

	@Autowired
	private StringToDouble stringToDouble;

	@Autowired
	private MdcInterceptor mdcInterceptor;

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Autowired
	private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

	@PostConstruct
	public void init() {
		//https://github.com/arey/spring-javaconfig-sample/blob/master/src/main/java/com/javaetmoi/sample/config/WebMvcConfig.java
		//don't include/show model attributes in url (request) when using redirect:
		requestMappingHandlerAdapter.setIgnoreDefaultModelOnRedirect(true);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/css/**").addResourceLocations("/css/");
		registry.addResourceHandler("/js/**").addResourceLocations("/js/");
		registry.addResourceHandler("/images/**").addResourceLocations("/images/");
		registry.addResourceHandler("/docs/**").addResourceLocations("/docs/");
		registry.addResourceHandler("/jpivot/**").addResourceLocations("/jpivot/");
		registry.addResourceHandler("/wcf/**").addResourceLocations("/wcf/");
		registry.addResourceHandler("/js-templates/**").addResourceLocations("/js-templates/");
		registry.addResourceHandler("/saiku/**").addResourceLocations("/saiku/");
		registry.addResourceHandler("/public/**").addResourceLocations("/public/");
	}

	@Bean
	public ViewResolver viewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setViewClass(JstlView.class);
		viewResolver.setPrefix("/WEB-INF/jsp/");
		viewResolver.setSuffix(".jsp");
		//http://forum.spring.io/forum/spring-projects/web/58269-how-to-access-the-current-locale-in-jstl-view
		//http://forum.spring.io/forum/spring-projects/web/web-flow/73404-gettign-user-locale-in-jsp-page
		viewResolver.setRequestContextAttribute("rc");
		return viewResolver;
	}

	@Bean
	public TemplateEngine emailTemplateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(emailTemplateResolver());
		return templateEngine;
	}

	private ITemplateResolver emailTemplateResolver() {
		SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setApplicationContext(applicationContext);
		resolver.setPrefix("/WEB-INF/thymeleaf/");
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");
		return resolver;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");

		//https://stackoverflow.com/questions/11586757/spring-3-interceptor-order
		registry.addInterceptor(localeChangeInterceptor);
		registry.addInterceptor(mdcInterceptor);

		AuthorizationInterceptor authorizationInterceptor = new AuthorizationInterceptor();
		authorizationInterceptor.setApplicationContext(applicationContext);

		//https://ant.apache.org/manual/dirtasks.html#patterns
		//https://opensourceforgeeks.blogspot.co.ke/2016/01/difference-between-and-in-spring-mvc.html
		registry.addInterceptor(authorizationInterceptor)
				.addPathPatterns("/**")
				.excludePathPatterns("/login", "/logout", "/accessDenied", 
						"/error", "/error-404", "/error-405", "/error-400", "/error-500");
	}

	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
		cookieLocaleResolver.setDefaultLocale(org.springframework.util.StringUtils.parseLocaleString("en"));
		return cookieLocaleResolver;
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("/WEB-INF/i18n/ArtMessages");
		messageSource.setUseCodeAsDefaultMessage(true);
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setFallbackToSystemLocale(false);
		messageSource.setCacheSeconds((int) TimeUnit.HOURS.toSeconds(1));
		return messageSource;
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(stringToUserGroup);
		registry.addConverter(stringToDouble);
		registry.addConverter(stringToReport);
	}

	@Bean
	public CommonsMultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}

}
