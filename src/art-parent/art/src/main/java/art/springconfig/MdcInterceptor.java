/*
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.springconfig;

import art.user.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Sets mdc attributes for each request
 *
 * @author Timothy Anyona
 */
@Component
public class MdcInterceptor extends HandlerInterceptorAdapter {
	//https://github.com/hoserdude/spring-petclinic-instrumented/blob/master/src/main/java/org/springframework/samples/petclinic/util/LogInterceptor.java
	//http://logback.qos.ch/manual/mdc.html
	//http://logback.qos.ch/xref/ch/qos/logback/classic/helpers/MDCInsertingServletFilter.html

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {

		HttpSession session = request.getSession();

		User user = (User) session.getAttribute("sessionUser");

		String username;
		if (user == null) {
			username = "";
		} else {
			username = user.getUsername();
		}

		MDC.put("user", username);
		MDC.put("remoteAddr", request.getRemoteAddr());
		MDC.put("requestURI", request.getRequestURI());

		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor == null) {
			//ensure it isn't null otherwise pivot table display will throw null pointer error
			xForwardedFor = "";
		}
		MDC.put("xForwardedFor", xForwardedFor);

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception ex) throws Exception {
		
		//everything added to mdc should be removed here
		MDC.remove("user");
		MDC.remove("remoteAddr");
		MDC.remove("requestURI");
		MDC.remove("xForwardedFor");
	}

}
