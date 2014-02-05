/**
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

package art.utils;

import art.usergroup.StringToUserGroup;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.stereotype.Component;

/**
 * Spring class to register custom formatters and converters
 * 
 * @author Timothy Anyona
 */
@Component("conversionService")
public class ApplicationConversionService extends DefaultFormattingConversionService  {
	
	public ApplicationConversionService(){
		//DefaultFormattingConversionService's default constructor
		//creates default formatters and converters
		super(); //no need for explicit super()?
		
		//add custom formatters and converters
		addConverter(new StringToUserGroup());
		//TODO reinstate
//		addConverter(new StringToInteger()); //override default StringToInteger converter
	}
	
}
