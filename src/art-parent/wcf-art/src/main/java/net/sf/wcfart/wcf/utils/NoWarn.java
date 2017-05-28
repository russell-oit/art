/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 */
package net.sf.wcfart.wcf.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates calls to legacy code that doesn't use generics
 * 
 * @author Timothy Anyona
 */
public class NoWarn {
	//http://www.deplication.net/2011/07/handling-raw-type-and-type-safety.html
	//https://stackoverflow.com/questions/1130433/how-to-avoid-unchecked-conversion-warning-in-java-if-you-use-legacy-libraries
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<T> castList(List v) {
        return (List<T>) v;        
    }
}
