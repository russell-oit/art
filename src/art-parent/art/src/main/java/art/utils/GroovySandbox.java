/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
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
package art.utils;

import art.servlets.Config;
import groovy.lang.Closure;
import groovy.lang.Script;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.groovy.sandbox.GroovyValueFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public class GroovySandbox extends GroovyValueFilter {
	//https://github.com/kohsuke/groovy-sandbox/blob/master/src/test/groovy/org/kohsuke/groovy/sandbox/robot/RobotSandbox.groovy
	//https://github.com/kohsuke/groovy-sandbox/tree/master/src/test/groovy/org/kohsuke/groovy/sandbox/robot

	private static final Logger logger = LoggerFactory.getLogger(GroovySandbox.class);

	private Set<Class> allowedTypes = new HashSet<>();
	private String whitelistFilePath;

	public GroovySandbox() {
		whitelistFilePath = Config.getWebinfPath() + "groovy-whitelist.txt";
		setAllowedTypes();
	}

	private void setAllowedTypes() {
		//https://www.mkyong.com/java/java-read-a-text-file-line-by-line/
		File whitelistFile = new File(whitelistFilePath);
		if (whitelistFile.exists()) {
			try {
				List<String> lines = FileUtils.readLines(whitelistFile, "UTF-8");
				for (String line : lines) {
					if (StringUtils.isBlank(line) || StringUtils.startsWith(line, "#")) {
						//do nothing
					} else {
						try {
							allowedTypes.add(Class.forName(line));
						} catch (ClassNotFoundException ex) {
							logger.error("Error", ex);
						}
					}
				}
			} catch (IOException ex) {
				logger.error("Error", ex);
			}
		}

		allowedTypes.addAll(getDefaultTypes());
	}

	private Set<Class> getDefaultTypes() {
		Set<Class> defaultTypes = new HashSet<>();

		//http://tutorials.jenkov.com/java/data-types.html
		Collections.addAll(defaultTypes,
				Boolean.class,
				Byte.class,
				Short.class,
				Character.class,
				Integer.class,
				Long.class,
				Float.class,
				Double.class,
				String.class,
				Object.class
		);

		return defaultTypes;
	}

	@Override
	public Object filter(Object o) {
		//https://stackoverflow.com/questions/24944431/getclass-returning-java-lang-class-reflection
		if (o == null) {
			return o;
		}

		if (o instanceof Script || o instanceof Closure) {
			return o; // access to properties of compiled groovy script
		}

		Class cl;
		Object value = null;
		if (o instanceof Class) {
			cl = (Class) o;
		} else {
			cl = o.getClass();
			value = o;
		}
		if (allowedTypes.contains(cl)) {
			return o;
		}

		String message = cl.toString();
		if (value != null) {
			message += ", value = " + value;
		}
		throw new SecurityException("Unexpected type: " + message);
	}

}
