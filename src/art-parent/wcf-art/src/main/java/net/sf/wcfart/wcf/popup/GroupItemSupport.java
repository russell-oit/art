/*
 * Copyright (c) 1971-2003 TONBELLER AG, Bensheim.
 * All rights reserved.
 */
package net.sf.wcfart.wcf.popup;

import java.util.ArrayList;
import java.util.List;

public class GroupItemSupport extends ItemSupport implements GroupItem {
  List<Item> children = new ArrayList<>();
  
  public List<Item> getChildren() {
    return children;
  }

  public GroupItemSupport() {
  }

  public GroupItemSupport(String label) {
    super(label);
  }

  public GroupItemSupport(String label, String image) {
    super(label, image);
  }


}
