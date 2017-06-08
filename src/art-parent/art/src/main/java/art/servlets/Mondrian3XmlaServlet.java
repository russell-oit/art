/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.servlets;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import net.sf.mondrianart.mondrian.xmla.impl.DynamicDatasourceXmlaServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet to enable ART to serve mondrian via xmla requests
 *
 * @author Timothy Anyona
 */
public class Mondrian3XmlaServlet extends DynamicDatasourceXmlaServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(Mondrian3XmlaServlet.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	protected String makeDataSourcesUrl(ServletConfig servletConfig) {
		String datasourcesFile = Config.getTemplatesPath() + "datasources.xml";
		File file = new File(datasourcesFile);

		String finalUrl = null;

		if (file.exists()) {
			try {
				URL dataSourcesConfigUrl = file.toURI().toURL();
				finalUrl = dataSourcesConfigUrl.toString();
			} catch (MalformedURLException ex) {
				logger.error("Error", ex);
			}
		} else {
			logger.warn("datasources.xml file not found");
		}

		return finalUrl;
	}

//    /**
//     * Returns datasources
//	 * 
//     * @param config
//     * @return datasources as configured in datasources.xml file
//     */
//    @Override
//    protected DataSourcesConfig.DataSources makeDataSources(ServletConfig config) {
//        String datasourcesFile; //path to datasources.xml file
//
//        datasourcesFile = Config.getTemplatesPath() + "datasources.xml";
//        File file = new File(datasourcesFile);
//
//        try {
//            return parseDataSourcesUrl(file.toURI().toURL());
//        } catch (Exception ex) {
//            logger.error("Error",ex);
//            return null;
//        }
//    }
}
