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
package art.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Provides utility methods related to output using the poi library
 *
 * @author Timothy Anyona
 */
public class PoiUtils {

	/**
	 * Adds a password to prevent opening of a file
	 *
	 * @param openPassword the password to set
	 * @param fullOutputFileName the full path to the file to set a password
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @throws InvalidFormatException
	 */
	public static void addOpenPassword(String openPassword, String fullOutputFileName)
			throws IOException, GeneralSecurityException, InvalidFormatException {

		//http://www.quicklyjava.com/create-password-protected-excel-using-apache-poi/
		//http://www.quicklyjava.com/create-password-protected-excel-sheets-using-apache-poi/
		//https://poi.apache.org/encryption.html
		//https://blogs.msdn.microsoft.com/david_leblanc/2010/04/16/dont-use-office-rc4-encryption-really-just-dont-do-it/
		//https://stackoverflow.com/questions/14701322/apache-poi-how-to-protect-sheet-with-options
		//https://stackoverflow.com/questions/17675685/apache-poi-excel-sheet-protection-and-data-validation
		//https://stackoverflow.com/questions/45097889/apache-poi-sheet-password-not-working
		if (StringUtils.isEmpty(openPassword)) {
			return;
		}

		try (POIFSFileSystem fs = new POIFSFileSystem()) {
			EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
			// EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile, CipherAlgorithm.aes192, HashAlgorithm.sha384, -1, -1, null);

			Encryptor enc = info.getEncryptor();
			enc.confirmPassword(openPassword);

			// Read in an existing OOXML file
			try (OPCPackage opc = OPCPackage.open(new File(fullOutputFileName), PackageAccess.READ_WRITE);
					OutputStream os = enc.getDataStream(fs)) {
				opc.save(os);
			}

			// Write out the encrypted version
			try (FileOutputStream fos = new FileOutputStream(fullOutputFileName)) {
				fs.writeFilesystem(fos);
			}
		}
	}
}
