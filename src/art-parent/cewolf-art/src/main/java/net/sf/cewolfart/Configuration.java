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

package net.sf.cewolfart;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * This class represents the configuration of the Cewolf framework.
 * It is designed as a singleton and resides in the application context.
 * @author glaures
 */
public class Configuration implements Serializable {

	static final long serialVersionUID = -3271334302902082721L;

	public static final String KEY = Configuration.class.getName();
	private static final String DEFAULT_STORAGE = "net.sf.cewolfart.storage.TransientSessionStorage";

	private String overlibURL = "overlib.js";
	private boolean debugged = false;
	private String storageClassName = DEFAULT_STORAGE;
	private Storage storage = null;
	private int maxImageWidth = 2048;
	private int maxImageHeight = 1024;

	private transient Map<String,String> parameters = new HashMap<String,String>();

	private Configuration() { }

	/** package protected constructor triggered by servlet */
	protected Configuration (ServletContext ctx) {
        ctx.log("configuring cewolf app..");
        ctx.setAttribute(KEY, this);

        //retrieve the init config params
        ServletConfig config = (ServletConfig) ctx.getAttribute(CewolfRenderer.INIT_CONFIG);
        if (config != null)
        {
            Enumeration<String> initParams = config.getInitParameterNames();
            try {
                while (initParams.hasMoreElements()) {
                    String param = initParams.nextElement();
                    String value = config.getInitParameter(param);
                    if ("debug".equalsIgnoreCase(param)) {
                        debugged = Boolean.parseBoolean(value);
                    } else if ("overliburl".equalsIgnoreCase(param)) {
                        overlibURL = value;
                    } else if ("storage".equalsIgnoreCase(param)) {
                        storageClassName = value;
                    } else if ("maxImageWidth".equalsIgnoreCase(param)) {
						maxImageWidth = Integer.parseInt(value); 
                    } else if ("maxImageHeight".equalsIgnoreCase(param)) {
						maxImageHeight = Integer.parseInt(value); 
                    } else {
						// not quite true: FileStorage.deleteOnExit is used just fine
                        ctx.log(param + " parameter is ignored.");
                    }
                    parameters.put(param,value);
                }
            } catch (Throwable t) {
                ctx.log("Error in Cewolf config.", t);
            }            
        } else {
        	ctx.log("Cewolf Misconfiguration. You should add a <load-on-startup> tag "
        			+ "to your web.xml for the Cewolf rendering servlet.\n"
					+ "A default Configuration will be used if not.");
        }

		try {
			initStorage(ctx);
		} catch (CewolfException ex) {
			ctx.log("exception during storage init from class " + storageClassName);
			ctx.log("using " + DEFAULT_STORAGE);
			storageClassName = DEFAULT_STORAGE;
			try {
				initStorage(ctx);
			} catch(CewolfException cwex){
				cwex.printStackTrace();
				throw new RuntimeException(storageClassName + ".init() threw exception.");
			}
		}
		ctx.log("using storage class " + storageClassName);
		ctx.log("using overlibURL " + overlibURL);
		ctx.log("max image width: " + maxImageWidth);
		ctx.log("max image height: " + maxImageHeight);
		ctx.log("debugging is turned " + (debugged ? "on" : "off"));
		ctx.log("...done.");
	}

	private void initStorage (ServletContext ctx) throws CewolfException {
		try {
			storage = (Storage)Class.forName(storageClassName).newInstance();
		} catch(Exception ex){
			ex.printStackTrace();
			throw new CewolfException(ex.getMessage());
		}
		storage.init(ctx);
	}

	/**
	 * Factory method. If no Configuration had been initialized before, a new
	 * one is created, stored in ctx and returned to the caller.
	 * @param ctx the servlet context from where to retrieve the Configuration object.
	 * @return the config object
	 */
	public static Configuration getInstance (ServletContext ctx) {
		Configuration config = null;

		try {
			config = (Configuration) ctx.getAttribute(KEY);
		} catch (ClassCastException ex) {
		    ctx.log("Configuration on context is wrong class");
		}

		if (config == null) {
			ctx.log("No Configuration for this context. Initializing.");
			config = new Configuration(ctx);
			ctx.setAttribute(KEY, config);
		}

		return config;
	}
        
	/**
	 * Checks if debugging is configured to be turned on. Configured by
	 * init param <code>debug</code> in web.xml.
	 * @return <code>true</code> if a debugging is on, else <code>false</false>
	 */
	public boolean isDebugged() {
		return debugged;
	}

	/**
	 * Returns the location of the overlib.js relative to webapp's root.
	 * Configured by init param <code>overliburl</code> in web.xml. Defaults to <code>overlib.js</code>
	 * @return String
	 */
	public String getOverlibURL() {
		return overlibURL;
	}

	public Storage getStorage() {
		return storage;
	}

	public int getMaxImageWidth() {
		return maxImageWidth;
	}

	public int getMaxImageHeight() {
		return maxImageHeight;
	}

	/**
	 * Get the initialization parameters from Cewolf servlet.
	 * @return The parameter map (String->String) values
	 */
	public Map<String,String> getParameters() {
		return parameters;
	}
}
