/*
* Copyright (C) 2001/2003  Enrico Liboni  - enrico@computer.org
*
*   This program is free software; you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation;
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   (version 2) along with this program (see documentation directory); 
*   otherwise, have a look at http://www.gnu.org or write to the Free Software
*   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*  
*/

package art.utils;

import java.util.*;
import javax.servlet.http.*;


/**
 * Ensure input values don't contain characters with special meaning in html
 * 
 * @author Enrico Liboni
 */
public class ArtParamValidation {
		

    /**
     * 
     */
    public ArtParamValidation() {		
	}


    /**
     * Check all fields except any named "SQL"
     * 
     * @param request
     * @return <code>true</code> if there's a field with an invalid character
     */
    public boolean validate(HttpServletRequest request ) { 
		// Check input parameters
		Enumeration parametersNames = request.getParameterNames();
		boolean isThereAnInvalidChar = false;
		int ifIamNegativeIsGood;
		char[] forbiddenChars = { '"', '\'', '<', '>' };
		
		while (parametersNames.hasMoreElements()) {
			String name = (String) parametersNames.nextElement();			
			if (!name.equals("SQL")) {								
				for(int j=0; j<  forbiddenChars.length ; j++) {
					ifIamNegativeIsGood = request.getParameter(name).indexOf(forbiddenChars[j]);															
					if (ifIamNegativeIsGood >= 0) {
						isThereAnInvalidChar = true;
						break;
					}
				}   
			}
		}
		
		// If an input value contains a forbidden char, exit.
		return !isThereAnInvalidChar;
	}

}   
