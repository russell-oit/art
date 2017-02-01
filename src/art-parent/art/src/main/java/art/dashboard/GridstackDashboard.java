/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.dashboard;

import java.util.List;

/**
 * Represents a gridstack dashboard, holding properties of the overall dashboard
 * 
 * @author Timothy Anyona
 */
public class GridstackDashboard extends AbstractDashboard {
	//https://github.com/troolee/gridstack.js/tree/master/doc
	private static final long serialVersionUID = 1L;
	private int width;
	private boolean floatEnabled;
	private boolean animate;
	private boolean disableDrag;
	private boolean disableResize;
	private String cellHeight;
	private String verticalMargin;
	private boolean alwaysShowResizeHandle;
	private List<GridstackItem> items;
	private int height;

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the floatEnabled
	 */
	public boolean isFloatEnabled() {
		return floatEnabled;
	}

	/**
	 * @param floatEnabled the floatEnabled to set
	 */
	public void setFloatEnabled(boolean floatEnabled) {
		this.floatEnabled = floatEnabled;
	}

	/**
	 * @return the animate
	 */
	public boolean isAnimate() {
		return animate;
	}

	/**
	 * @param animate the animate to set
	 */
	public void setAnimate(boolean animate) {
		this.animate = animate;
	}

	/**
	 * @return the disableDrag
	 */
	public boolean isDisableDrag() {
		return disableDrag;
	}

	/**
	 * @param disableDrag the disableDrag to set
	 */
	public void setDisableDrag(boolean disableDrag) {
		this.disableDrag = disableDrag;
	}

	/**
	 * @return the disableResize
	 */
	public boolean isDisableResize() {
		return disableResize;
	}

	/**
	 * @param disableResize the disableResize to set
	 */
	public void setDisableResize(boolean disableResize) {
		this.disableResize = disableResize;
	}

	/**
	 * @return the cellHeight
	 */
	public String getCellHeight() {
		return cellHeight;
	}

	/**
	 * @param cellHeight the cellHeight to set
	 */
	public void setCellHeight(String cellHeight) {
		this.cellHeight = cellHeight;
	}

	/**
	 * @return the verticalMargin
	 */
	public String getVerticalMargin() {
		return verticalMargin;
	}

	/**
	 * @param verticalMargin the verticalMargin to set
	 */
	public void setVerticalMargin(String verticalMargin) {
		this.verticalMargin = verticalMargin;
	}

	/**
	 * @return the alwaysShowResizeHandle
	 */
	public boolean isAlwaysShowResizeHandle() {
		return alwaysShowResizeHandle;
	}

	/**
	 * @param alwaysShowResizeHandle the alwaysShowResizeHandle to set
	 */
	public void setAlwaysShowResizeHandle(boolean alwaysShowResizeHandle) {
		this.alwaysShowResizeHandle = alwaysShowResizeHandle;
	}

	/**
	 * @return the items
	 */
	public List<GridstackItem> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(List<GridstackItem> items) {
		this.items = items;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
}
