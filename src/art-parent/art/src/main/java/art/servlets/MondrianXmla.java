/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.servlets;

import java.io.File;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import mondrian.xmla.DataSourcesConfig;
import mondrian.xmla.impl.DynamicDatasourceXmlaServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet to enable ART to serve mondrian via xmla requests.
 * 
 * @author Timothy Anyona
 */
public class MondrianXmla extends DynamicDatasourceXmlaServlet {
    
    private static final long serialVersionUID = 1L;
    
    final static Logger logger = LoggerFactory.getLogger(MondrianXmla.class);
    

    /**
     * 
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * 
     * @param config
     * @return datasources as configured in datasources.xml file
     */
    @Override
    protected DataSourcesConfig.DataSources makeDataSources(ServletConfig config) {
        String datasourcesFile; //path to datasources.xml file

        datasourcesFile = ArtDBCP.getTemplatesPath() + "datasources.xml";
        File file = new File(datasourcesFile);

        try {
            return parseDataSourcesUrl(file.toURI().toURL());
        } catch (Exception e) {
            logger.error("Error",e);
            return null;
        }
    }
}