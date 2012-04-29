package art.output;

import art.servlets.ArtDBCP;

import art.utils.ArtQueryParam;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate tsv gz output
 * 
 * @author Enrico Liboni
 */
public class tsvGzOutput implements ArtOutputInterface {

    final static Logger logger = LoggerFactory.getLogger(tsvGzOutput.class);
    
    FileOutputStream fout;
    GZIPOutputStream zout;
    byte[] buf;
    String tmpstr;
    StringBuffer exportFileStrBuf;
    String filename;
    NumberFormat nfPlain;
    String fullFileName;
    PrintWriter htmlout;
    String queryName;
    String userName;
    int maxRows;
    int counter;
    int columns;
    String y_m_d;
    String h_m_s;
    Map<Integer, ArtQueryParam> displayParams;
    final int FLUSH_SIZE = 1024 * 4; // flush to disk each 4kb of columns ;) 

    /**
     * Constructor
     */
    public tsvGzOutput() {
        exportFileStrBuf = new StringBuffer(8 * 1024);
        counter = 0;
        nfPlain = NumberFormat.getInstance();
        nfPlain.setMinimumFractionDigits(0);
        nfPlain.setGroupingUsed(false);
        nfPlain.setMaximumFractionDigits(99);
    }

    @Override
    public String getName() {
        return "Spreadsheet (Gzipped tsv - text)";
    }

    @Override
    public String getContentType() {
        return "text/html;charset=utf-8";
    }

    @Override
    public void setExportPath(String s) {
        try {
            // Build filename			
            Date today = new Date();

            String dateFormat = "yyyy_MM_dd";
            SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
            y_m_d = dateFormatter.format(today);

            String timeFormat = "HH_mm_ss";
            SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
            h_m_s = timeFormatter.format(today);

            filename = userName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ".tsv.gz";
            filename=ArtDBCP.cleanFileName(filename); //replace characters that would make an invalid filename
            fullFileName = s + filename;

            fout = new FileOutputStream(fullFileName);
            zout = new GZIPOutputStream(fout);
        } catch (IOException e) {
            logger.error("Error",e);
        }
    }

    @Override
    public String getFileName() {
        return fullFileName;
    }

    @Override
    public void setWriter(PrintWriter o) {
        htmlout = o;
    }

    @Override
    public void setQueryName(String s) {
        queryName = s;
    }

    @Override
    public void setUserName(String s) {
        userName = s;
    }

    @Override
    public void setMaxRows(int i) {
        maxRows = i;
    }

    @Override
    public void setColumnsNumber(int i) {
        columns = i;
    }

    @Override
    public void setDisplayParameters(Map<Integer, ArtQueryParam> t) {
        displayParams = t;
    }

    @Override
    public void beginHeader() {
        if (displayParams != null && displayParams.size()>0) {
            exportFileStrBuf.append("Params:\t");
            // decode the parameters handling multi one
            Iterator it = displayParams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ArtQueryParam param=(ArtQueryParam)entry.getValue();
                String paramName=param.getName();
                Object pValue = param.getParamValue();
                
                if (pValue instanceof String) {
                    exportFileStrBuf.append(paramName);
                    exportFileStrBuf.append("=");
                    exportFileStrBuf.append(pValue);
                    exportFileStrBuf.append(" \t ");
                } else if (pValue instanceof String[]) { // multi
                    StringBuilder pValuesSb = new StringBuilder(256);
                    String[] pValues = (String[]) pValue;
                    for (int i = 0; i < pValues.length; i++) {
                        pValuesSb.append(pValues[i]);
                        pValuesSb.append(",");
                    }
                    exportFileStrBuf.append(paramName);
                    exportFileStrBuf.append("= (");
                    exportFileStrBuf.append(pValuesSb.toString());
                    exportFileStrBuf.append(" ) \t");
                }
            }
            exportFileStrBuf.append("\n");
        }
    }

    @Override
    public void addHeaderCell(String s) {
        exportFileStrBuf.append(s);
        exportFileStrBuf.append("\t");
    }

    @Override
    public void endHeader() {
    }

    @Override
    public void beginLines() {
    }

    @Override
    public void addCellString(String s) {
        if (s != null) {
            exportFileStrBuf.append(s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' '));
            exportFileStrBuf.append("\t");
        } else {
            exportFileStrBuf.append(s);
            exportFileStrBuf.append("\t");
        }
    }

    @Override
    public void addCellDouble(Double d) {
        String formattedValue = null;
        if (d != null) {
            formattedValue = nfPlain.format(d.doubleValue());
        }
        exportFileStrBuf.append("" + formattedValue + "\t");
    }

    @Override
    public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT
        exportFileStrBuf.append("" + i + "\t");
    }

    @Override
    public void addCellDate(Date d) {
        exportFileStrBuf.append("" + d + "\t");
    }

    @Override
    public boolean newLine() {
        exportFileStrBuf.append("\n");
        counter++;
        if ((counter * columns) > FLUSH_SIZE) {
            try {
                tmpstr = exportFileStrBuf.toString();
                buf = new byte[tmpstr.length()];
                buf = tmpstr.getBytes("UTF-8");
                zout.write(buf);
                zout.flush();
                exportFileStrBuf = new StringBuffer(32 * 1024);
            } catch (IOException e) {
                logger.error("Error. Data not completed. Please narrow your search",e);

                //htmlout not used for scheduled jobs
                if (htmlout != null) {
                    htmlout.println("<span style=\"color:red\">Error: " + e
                            + ")! Data not completed. Please narrow your search!</span>");
                }
            }
        }

        if (counter < maxRows) {
            return true;
        } else {
            addCellString("Maximum number of rows exceeded! Query not completed.");
            endLines(); // close files
            return false;
        }
    }

    @Override
    public void endLines() {
        addCellString("\n Total rows retrieved:");
        addCellString("" + (counter));

        try {
            tmpstr = exportFileStrBuf.toString();
            buf = new byte[tmpstr.length()];
            buf = tmpstr.getBytes("UTF-8");
            zout.write(buf);
            zout.flush();
            zout.close();
            fout.close();

            //htmlout not used for scheduled jobs
            if (htmlout != null) {
                htmlout.println("<p><div align=\"Center\"><table border=\"0\" width=\"90%\">");
                htmlout.println("<tr><td colspan=2 class=\"data\" align=\"center\" >"
                        + "<a  type=\"application/octet-stream\" href=\"../export/" + filename + "\"> "
                        + filename + "</a>"
                        + "</td></tr>");
                htmlout.println("</table></div></p>");
            }
        } catch (IOException e) {
            logger.error("Error",e);
        }
    }

    @Override
    public boolean isDefaultHtmlHeaderAndFooterEnabled() {
        return true;
    }
}
