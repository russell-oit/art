package art.saiku.web.export;

import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FoConverter {
	
	private static final Logger logger = LoggerFactory.getLogger(FoConverter.class);

    public static Document getFo(Document xmlDoc) {
        try {
            return xml2Fo(xmlDoc);
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return null;
    }

    private static Document xml2Fo(Document xml) throws javax.xml.transform.TransformerException {
        DOMSource xmlDomSource = new DOMSource(xml);
        DOMResult domResult = new DOMResult();
        Transformer transformer = createTransformer();
        transformer.transform(xmlDomSource, domResult);
        return (org.w3c.dom.Document) domResult.getNode();
    }

    private static Transformer createTransformer() throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();

        if (transformer == null) {
            throw new TransformerConfigurationException("Transformer is null");
        }
        return transformer;
    }

}
