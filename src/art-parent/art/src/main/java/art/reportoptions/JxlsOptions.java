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
package art.reportoptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

/**
 * Report options for jxls reports
 * 
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JxlsOptions implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String areaConfigFile;
	private boolean useStandardFormulaProcessor;

	/**
	 * @return the areaConfigFile
	 */
	public String getAreaConfigFile() {
		return areaConfigFile;
	}

	/**
	 * @param areaConfigFile the areaConfigFile to set
	 */
	public void setAreaConfigFile(String areaConfigFile) {
		this.areaConfigFile = areaConfigFile;
	}

	/**
	 * @return the useStandardFormulaProcessor
	 */
	public boolean isUseStandardFormulaProcessor() {
		return useStandardFormulaProcessor;
	}

	/**
	 * @param useStandardFormulaProcessor the useStandardFormulaProcessor to set
	 */
	public void setUseStandardFormulaProcessor(boolean useStandardFormulaProcessor) {
		this.useStandardFormulaProcessor = useStandardFormulaProcessor;
	}
}
