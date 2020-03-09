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
package art.migration;

import art.enums.MigrationFileFormat;
import art.enums.MigrationRecordType;
import java.io.Serializable;

/**
 * Represents an import records operation
 * 
 * @author Timothy Anyona
 */
public class ImportRecords implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private MigrationRecordType recordType;
	private MigrationFileFormat fileFormat = MigrationFileFormat.json;
	private boolean overwrite;

	/**
	 * @return the fileFormat
	 */
	public MigrationFileFormat getFileFormat() {
		return fileFormat;
	}

	/**
	 * @param fileFormat the fileFormat to set
	 */
	public void setFileFormat(MigrationFileFormat fileFormat) {
		this.fileFormat = fileFormat;
	}

	/**
	 * @return the recordType
	 */
	public MigrationRecordType getRecordType() {
		return recordType;
	}

	/**
	 * @param recordType the recordType to set
	 */
	public void setRecordType(MigrationRecordType recordType) {
		this.recordType = recordType;
	}

	/**
	 * @return the overwrite
	 */
	public boolean isOverwrite() {
		return overwrite;
	}

	/**
	 * @param overwrite the overwrite to set
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}
}
