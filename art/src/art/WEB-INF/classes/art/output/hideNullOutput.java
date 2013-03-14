package art.output;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import art.utils.ArtQueryParam;

public class hideNullOutput implements ArtOutputInterface {
	private final ArtOutputInterface artOutputInterface;

	@Override
	public String getName() {
		return artOutputInterface.getName();
	}

	@Override
	public String getContentType() {
		return artOutputInterface.getContentType();
	}

	@Override
	public void setWriter(PrintWriter o) {
		artOutputInterface.setWriter(o);
	}

	@Override
	public void setQueryName(String s) {
		artOutputInterface.setQueryName(s);
	}

	@Override
	public void setFileUserName(String s) {
		artOutputInterface.setFileUserName(s);
	}

	@Override
	public void setMaxRows(int i) {
		artOutputInterface.setMaxRows(i);
	}

	@Override
	public void setColumnsNumber(int i) {
		artOutputInterface.setColumnsNumber(i);
	}

	@Override
	public void setExportPath(String s) {
		artOutputInterface.setExportPath(s);
	}

	@Override
	public void setDisplayParameters(Map<Integer, ArtQueryParam> params) {
		artOutputInterface.setDisplayParameters(params);
	}

	@Override
	public void beginHeader() {
		artOutputInterface.beginHeader();
	}

	@Override
	public void addHeaderCell(String s) {
		artOutputInterface.addHeaderCell(s);
	}

	@Override
	public void endHeader() {
		artOutputInterface.endHeader();
	}

	@Override
	public void beginLines() {
		artOutputInterface.beginLines();
	}

	@Override
	public void addCellString(String s) {
		artOutputInterface.addCellString(s==null?" ":s);
	}

	@Override
	public void addCellDouble(Double d) {
		if (d == null){
			artOutputInterface.addCellString(" ");
		}else{
			artOutputInterface.addCellDouble(d);
		}
	}

	@Override
	public void addCellLong(Long i) {
		if (i == null){
			artOutputInterface.addCellString(" ");
		}else{
			artOutputInterface.addCellLong(i);
		}
	}

	@Override
	public void addCellDate(Date d) {
		artOutputInterface.addCellDate(d);
	}

	@Override
	public boolean newLine() {
		return artOutputInterface.newLine();
	}

	@Override
	public void endLines() {
		artOutputInterface.endLines();
	}

	@Override
	public String getFileName() {
		return artOutputInterface.getFileName();
	}

	@Override
	public boolean isShowQueryHeaderAndFooter() {
		return artOutputInterface.isShowQueryHeaderAndFooter();
	}

	public hideNullOutput(ArtOutputInterface artOutputInterface) {
		super();
		this.artOutputInterface = artOutputInterface;
	}

}
