/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.springconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 * Spring security configuration to provide csrf protection
 *
 * @author Timothy Anyona
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//https://stackoverflow.com/questions/28209344/using-just-spring-security-csrf-feature
		//https://github.com/spring-projects/spring-mvc-showcase/commit/361adc124c05a8187b84f25e8a57550bb7d9f8e4
		//https://docs.spring.io/spring-security/site/docs/current/reference/html/csrf.html#csrf-include-csrf-token
		//https://stackoverflow.com/questions/22524470/spring-security-3-2-csrf-disable-for-specfic-urls
		//https://stackoverflow.com/questions/32698808/how-to-ignore-spring-security-csrf-for-specific-urls-in-spring-boot-project
		http
				//https://docs.spring.io/autorepo/docs/spring-security/3.2.0.CI-SNAPSHOT/reference/html/headers.html
				//https://docs.spring.io/spring-security/site/docs/4.0.2.RELEASE/reference/htmlsingle/#headers-frame-options
				//https://stackoverflow.com/questions/28647136/how-to-disable-x-frame-options-response-header-in-spring-security
				.headers().frameOptions().disable().and() //uncomment to enable running art in an iframe
				//.headers().disable() //uncomment to disable all spring secrity added response headers
				.csrf().ignoringAntMatchers("/saiku2/**", "/api/**")
				.and()
				.csrf().disable()
				.authorizeRequests()
				.anyRequest().permitAll();
	}

	@Bean
	public HttpFirewall httpFirewall() {
		//https://stackoverflow.com/questions/48580584/stricthttpfirewall-in-spring-security-4-2-vs-spring-mvc-matrixvariable
		//https://stackoverflow.com/questions/48453980/spring-5-0-3-requestrejectedexception-the-request-was-rejected-because-the-url
		//https://stackoverflow.com/questions/44673490/how-to-prevent-the-jsessionid-showing-in-the-url
		//https://stackoverflow.com/questions/29692353/how-to-prevent-adding-jsessionid-at-the-end-of-redirected-url
		//https://stackoverflow.com/questions/962729/is-it-possible-to-disable-jsessionid-in-tomcat-servlet
		//https://fralef.me/tomcat-disable-jsessionid-in-url.html
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowSemicolon(true);
		return firewall;
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		super.configure(web);
		web.httpFirewall(httpFirewall());
	}

}
