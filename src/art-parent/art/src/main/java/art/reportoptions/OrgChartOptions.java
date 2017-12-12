/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.reportoptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents report options for org chart report types
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgChartOptions {

	private String cssFile;
	private String nodeTitle = "name";
	private String nodeId = "id";
	private boolean toggleSiblingsResp = false;
	private int depth = 999;
	private boolean exportButton = false;
	private String exportFilename = "OrgChart";
	private String exportFileextension = "png";
	private String parentNodeSymbol = "fa-users";
	private boolean draggable = false;
	private String direction = "t2b";
	private boolean pan = false;
	private boolean zoom = false;
	private double zoominLimit = 7D;
	private double zoomoutLimit = 0.5D;
	private String nodeContent = "title";

	/**
	 * @return the cssFile
	 */
	public String getCssFile() {
		return cssFile;
	}

	/**
	 * @param cssFile the cssFile to set
	 */
	public void setCssFile(String cssFile) {
		this.cssFile = cssFile;
	}

	/**
	 * @return the nodeTitle
	 */
	public String getNodeTitle() {
		return nodeTitle;
	}

	/**
	 * @param nodeTitle the nodeTitle to set
	 */
	public void setNodeTitle(String nodeTitle) {
		this.nodeTitle = nodeTitle;
	}

	/**
	 * @return the nodeId
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * @param nodeId the nodeId to set
	 */
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return the toggleSiblingsResp
	 */
	public boolean isToggleSiblingsResp() {
		return toggleSiblingsResp;
	}

	/**
	 * @param toggleSiblingsResp the toggleSiblingsResp to set
	 */
	public void setToggleSiblingsResp(boolean toggleSiblingsResp) {
		this.toggleSiblingsResp = toggleSiblingsResp;
	}

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @param depth the depth to set
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * @return the exportButton
	 */
	public boolean isExportButton() {
		return exportButton;
	}

	/**
	 * @param exportButton the exportButton to set
	 */
	public void setExportButton(boolean exportButton) {
		this.exportButton = exportButton;
	}

	/**
	 * @return the exportFilename
	 */
	public String getExportFilename() {
		return exportFilename;
	}

	/**
	 * @param exportFilename the exportFilename to set
	 */
	public void setExportFilename(String exportFilename) {
		this.exportFilename = exportFilename;
	}

	/**
	 * @return the exportFileextension
	 */
	public String getExportFileextension() {
		return exportFileextension;
	}

	/**
	 * @param exportFileextension the exportFileextension to set
	 */
	public void setExportFileextension(String exportFileextension) {
		this.exportFileextension = exportFileextension;
	}

	/**
	 * @return the parentNodeSymbol
	 */
	public String getParentNodeSymbol() {
		return parentNodeSymbol;
	}

	/**
	 * @param parentNodeSymbol the parentNodeSymbol to set
	 */
	public void setParentNodeSymbol(String parentNodeSymbol) {
		this.parentNodeSymbol = parentNodeSymbol;
	}

	/**
	 * @return the draggable
	 */
	public boolean isDraggable() {
		return draggable;
	}

	/**
	 * @param draggable the draggable to set
	 */
	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}

	/**
	 * @return the direction
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}

	/**
	 * @return the pan
	 */
	public boolean isPan() {
		return pan;
	}

	/**
	 * @param pan the pan to set
	 */
	public void setPan(boolean pan) {
		this.pan = pan;
	}

	/**
	 * @return the zoom
	 */
	public boolean isZoom() {
		return zoom;
	}

	/**
	 * @param zoom the zoom to set
	 */
	public void setZoom(boolean zoom) {
		this.zoom = zoom;
	}

	/**
	 * @return the zoominLimit
	 */
	public double getZoominLimit() {
		return zoominLimit;
	}

	/**
	 * @param zoominLimit the zoominLimit to set
	 */
	public void setZoominLimit(double zoominLimit) {
		this.zoominLimit = zoominLimit;
	}

	/**
	 * @return the zoomoutLimit
	 */
	public double getZoomoutLimit() {
		return zoomoutLimit;
	}

	/**
	 * @param zoomoutLimit the zoomoutLimit to set
	 */
	public void setZoomoutLimit(double zoomoutLimit) {
		this.zoomoutLimit = zoomoutLimit;
	}

	/**
	 * @return the nodeContent
	 */
	public String getNodeContent() {
		return nodeContent;
	}

	/**
	 * @param nodeContent the nodeContent to set
	 */
	public void setNodeContent(String nodeContent) {
		this.nodeContent = nodeContent;
	}

}
