/*
 * Copyright (c) 1971-2003 TONBELLER AG, Bensheim.
 * All rights reserved.
 */
package net.sf.wcfart.wcf.popup;

import java.util.List;

public interface GroupItem extends Item {
  List<Item> getChildren();
}
