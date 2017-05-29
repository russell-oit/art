/* ================================================================
 * Cewolf : Chart enabling Web Objects Framework
 * ================================================================
 *
 * Project Info:  http://cewolf.sourceforge.net
 * Project Lead:  Guido Laures (guido@laures.de);
 *
 * (C) Copyright 2002, by Guido Laures
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sf.cewolfart.taglib.util;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.MarshalledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author  Guido Laures
 */
public abstract class KeyGenerator {

private static final Logger logger = LoggerFactory.getLogger(KeyGenerator.class);
    
    private static class NoKeyException extends RuntimeException {

		static final long serialVersionUID = -6826954827480537636L;

        public NoKeyException (String msg){
            super(msg);
        }
    }

    public static int generateKey (Serializable obj) {
        if (obj == null) {
            NoKeyException ex = new NoKeyException("assertion failed: can not generate key for null,");
            throw ex;
        }
        try {
            MarshalledObject<Serializable> mo = new MarshalledObject<Serializable>(obj);
            return mo.hashCode();
        } catch (IOException ioex) {
            logger.error("Error",ioex);
            throw new NoKeyException(obj + " is not serializable.");
        }
    }
}
