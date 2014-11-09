/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.chart;

import de.laures.cewolf.ChartPostProcessor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timothy Anyona
 */
public class PostProcessorDefinition implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private Map<String, String> params = new HashMap<>();
	private ChartPostProcessor postProcessorInstance;

	/**
	 * @return the postProcessorInstance
	 */
	public ChartPostProcessor getPostProcessorInstance() {
		return postProcessorInstance;
	}

	/**
	 * @param postProcessorInstance the postProcessorInstance to set
	 */
	public void setPostProcessorInstance(ChartPostProcessor postProcessorInstance) {
		this.postProcessorInstance = postProcessorInstance;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the params
	 */
	public Map<String, String> getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public void addParameter(String name, String value) {
		params.put(name, value);
	}

}
