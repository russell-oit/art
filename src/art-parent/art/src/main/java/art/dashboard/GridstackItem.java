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

/**
 * Represents an item that is contained within a gridstack dashboard
 *
 * @author Timothy Anyona
 */
public class GridstackItem extends DashboardItem {

	//https://stackoverflow.com/questions/9142516/if-my-class-implements-serializable-do-i-have-to-implement-it-in-its-subclasses
	//https://github.com/troolee/gridstack.js/tree/master/doc
	private static final long serialVersionUID = 1L;
	private int xPosition;
	private int yPosition;
	private int width;
	private int height;
	private boolean noResize;
	private boolean noMove;
	private boolean autoposition;
	private boolean locked;
	private int minWidth; //0 doesn't have same effect as html attribute not being present in jsp, therefore requires conditional inclusion using ternary operator in jsp
	private int minHeight; //0 has same effect as html attribute no being present in jsp. doesn't require ternary operator in jsp
	private int maxWidth; //0 results in item having mimimum width, so omit html attribute if 0 (if not specified)
	private int maxHeight; //0 results in item having mimimum height, so omit html attribute if 0 (if not specified)

	/**
	 * @return the xPosition
	 */
	public int getxPosition() {
		return xPosition;
	}

	/**
	 * @param xPosition the xPosition to set
	 */
	public void setxPosition(int xPosition) {
		this.xPosition = xPosition;
	}

	/**
	 * @return the yPosition
	 */
	public int getyPosition() {
		return yPosition;
	}

	/**
	 * @param yPosition the yPosition to set
	 */
	public void setyPosition(int yPosition) {
		this.yPosition = yPosition;
	}

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

	/**
	 * @return the noResize
	 */
	public boolean isNoResize() {
		return noResize;
	}

	/**
	 * @param noResize the noResize to set
	 */
	public void setNoResize(boolean noResize) {
		this.noResize = noResize;
	}

	/**
	 * @return the noMove
	 */
	public boolean isNoMove() {
		return noMove;
	}

	/**
	 * @param noMove the noMove to set
	 */
	public void setNoMove(boolean noMove) {
		this.noMove = noMove;
	}

	/**
	 * @return the autoposition
	 */
	public boolean isAutoposition() {
		return autoposition;
	}

	/**
	 * @param autoposition the autoposition to set
	 */
	public void setAutoposition(boolean autoposition) {
		this.autoposition = autoposition;
	}

	/**
	 * @return the locked
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * @param locked the locked to set
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/**
	 * @return the minWidth
	 */
	public int getMinWidth() {
		return minWidth;
	}

	/**
	 * @param minWidth the minWidth to set
	 */
	public void setMinWidth(int minWidth) {
		this.minWidth = minWidth;
	}

	/**
	 * @return the minHeight
	 */
	public int getMinHeight() {
		return minHeight;
	}

	/**
	 * @param minHeight the minHeight to set
	 */
	public void setMinHeight(int minHeight) {
		this.minHeight = minHeight;
	}

	/**
	 * @return the maxWidth
	 */
	public int getMaxWidth() {
		return maxWidth;
	}

	/**
	 * @param maxWidth the maxWidth to set
	 */
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	/**
	 * @return the maxHeight
	 */
	public int getMaxHeight() {
		return maxHeight;
	}

	/**
	 * @param maxHeight the maxHeight to set
	 */
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

}
