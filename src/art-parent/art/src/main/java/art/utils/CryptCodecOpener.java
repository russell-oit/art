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

import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import java.io.File;
import java.io.IOException;
import net.ucanaccess.jdbc.JackcessOpenerInterface;

/**
 * Enables reporting from password protected MS Access files using the
 * UCanAccess driver
 *
 * @author Timothy Anyona
 */
public class CryptCodecOpener implements JackcessOpenerInterface {
	//http://ucanaccess.sourceforge.net/site.html#examples
	//https://support.office.com/en-us/article/Encrypt-a-database-by-using-a-database-password-61ae3428-79f5-432e-9668-246d5656d96f

	@Override
	public Database open(File file, String password) throws IOException {
		DatabaseBuilder dbd = new DatabaseBuilder(file);
		dbd.setAutoSync(false);
		dbd.setCodecProvider(new CryptCodecProvider(password));
		dbd.setReadOnly(false);
		return dbd.open();
	}

}
