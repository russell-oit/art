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
 * Restricts which classes can be used in a groovy script
 *
 * @author Timothy Anyona
 */
public class GroovySandbox extends GroovyValueFilter {
	//https://github.com/kohsuke/groovy-sandbox/blob/master/src/test/groovy/org/kohsuke/groovy/sandbox/robot/RobotSandbox.groovy
	//https://github.com/kohsuke/groovy-sandbox/tree/master/src/test/groovy/org/kohsuke/groovy/sandbox/robot
	//https://github.com/kohsuke/groovy-sandbox/blob/master/src/test/groovy/org/kohsuke/groovy/sandbox/TheTest.groovy
	//https://stackoverflow.com/questions/6210045/bullet-proof-groovy-script-embedding

	private static final Logger logger = LoggerFactory.getLogger(GroovySandbox.class);

	private Set<Class<?>> allowedTypes = new HashSet<>();
	private String whitelistFilePath;

	public GroovySandbox() {
		whitelistFilePath = Config.getWebinfPath() + "groovy-whitelist.txt";
		setAllowedTypes();
	}

	/**
	 * Sets the allowed classes
	 */
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

	/**
	 * Returns classes that are allowed without having to be specified in the
	 * whitelist file
	 *
	 * @return classes that are allowed without having to be specified in the
	 * whitelist file
	 */
	private Set<Class<?>> getDefaultTypes() {
		Set<Class<?>> defaultTypes = new HashSet<>();

		//https://stackoverflow.com/questions/2041778/how-to-initialize-hashset-values-by-construction
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
				Object.class,
				boolean.class,
				byte.class,
				short.class,
				char.class,
				int.class,
				long.class,
				float.class,
				double.class
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

		Class<?> cl;
		Object value = null;
		//https://stackoverflow.com/questions/9068150/best-way-to-negate-an-instanceof
		if (o instanceof Class) {
			cl = (Class<?>) o;
		} else {
			cl = o.getClass();
			value = o;
		}
		
		//allow dynamic classes defined in groovy script
		String className = cl.getName();
		if(StringUtils.startsWith(className, "art.groovy.")){
			return o;
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
