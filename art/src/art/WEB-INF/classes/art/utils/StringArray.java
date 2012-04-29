/*
* Copyright (C) 2001/2004  Enrico Liboni  - enrico@computer.org
*
*   This program is free software; you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation;
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   (version 2) along with this program (see documentation directory); 
*   otherwise, have a look at http://www.gnu.org or write to the Free Software
*   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*  
*/

/** StringArray.java
*
* Caller:	ArtJob.jsp
* Purpose:	a growable string array
*/

package art.utils;


/**
 * A growable string array
 * 
 * @author Enrico Liboni
 */
public class StringArray {

	String[] sa;
	int size     = 0;
	int capacity = 10;

    /**
     * 
     */
    public StringArray() {
		sa = new String[capacity];
	}

    /**
     * 
     * @param c
     */
    public StringArray(int c) {
		capacity=c;
		sa = new String[capacity];
	}

	private void reallocate() {
		capacity = capacity * 2;
		String[] tmp = new String[capacity];
		for(int j=0; j< sa.length; j++) {
			tmp[j] = sa[j];
		}
		sa = tmp;
	}

    /**
     * 
     * @param s
     */
    public void add(String s){
		if ( size >= sa.length ) reallocate();
		sa[size] = s;
		size++;
	}

    /**
     * 
     * @param i
     * @return character at the given position
     */
    public String get(int i) {
		if (i >=0 && i <sa.length){
			return sa[i];
		} else {
			return null;
		}
	}

    /**
     * 
     * @return array size
     */
    public int length() {
		return size;
	}

    /**
     * 
     * @return array of a given size
     */
    public String[] getStringArray() {
		/* logic to provide an array just as 
		long as it should be: */
		String[] tmp = new String[size];
		for(int j=0; j< size; j++) {
			tmp[j] = sa[j];
		}
		return tmp;
	}
}
