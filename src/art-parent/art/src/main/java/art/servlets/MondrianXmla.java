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