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

package net.sf.cewolfart.storage;

import java.io.Serializable;
import java.util.Date;

import net.sf.cewolfart.CewolfException;
import net.sf.cewolfart.ChartImage;

/**
 * @author guido
 *
 */
public class SerializableChartImage implements ChartImage, Serializable {

	static final long serialVersionUID = -6746254726157616461L;

	private final int width;
	private final int height;
	private final int type;
	private final Date timeoutTime;
	private final String mimeType;
	private final byte[] data;

	public SerializableChartImage(ChartImage img) throws CewolfException{
		this.width = img.getWidth();
		this.height = img.getHeight();
		this.type = img.getType();
		this.mimeType = img.getMimeType();
		this.data = img.getBytes();
		this.timeoutTime = img.getTimeoutTime();
	}

	/**
	 * @see net.sf.cewolfart.ChartImage#getWidth()
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @see net.sf.cewolfart.ChartImage#getHeight()
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @see net.sf.cewolfart.ChartImage#getType()
	 */
	public int getType() {
		return type;
	}

	/**
	 * @see net.sf.cewolfart.ChartImage#getBytes()
	 */
	public byte[] getBytes() throws CewolfException {
		return data;
	}

	/**
	 * @see net.sf.cewolfart.ChartImage#getMimeType()
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @see net.sf.cewolfart.ChartImage#getSize()
	 */
	public int getSize() throws CewolfException {
		return data.length;
	}

  /* (non-Javadoc)
   * @see net.sf.cewolfart.ChartImage#getTimeoutTime()
   */
  public Date getTimeoutTime() {      
    return timeoutTime;
  }

}
