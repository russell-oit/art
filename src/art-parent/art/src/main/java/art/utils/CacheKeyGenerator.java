/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * Custom cache key generator for spring + ehcache. Default key generator only
 * uses method parameters so chances of conflicts are high.
 *
 * @author Timothy Anyona
 */
public class CacheKeyGenerator implements KeyGenerator {
	//https://stackoverflow.com/questions/14072380/cacheable-key-on-multiple-method-arguments
	//https://jira.spring.io/browse/SPR-9036
	//https://jira.spring.io/browse/SPR-10237 .implies don't include method name? not relevant if only using allEntries?

	@Override
	public Object generate(final Object target, final Method method,
			final Object... params) {

		final List<Object> key = new ArrayList<>();
		key.add(method.getDeclaringClass().getName());
		key.add(method.getName());

		for (final Object o : params) {
			key.add(o);
		}
		return key;
	}

}
