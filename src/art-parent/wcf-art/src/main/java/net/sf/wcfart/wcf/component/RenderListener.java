/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * Copyright (C) 2003-2004 TONBELLER AG.
 * All Rights Reserved.
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 *
 * 
 */
package net.sf.wcfart.wcf.component;

import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * Part of a component that wants to be informed about the rendering.
 * Only few Components support this.
 *
 * @author av
 */
public interface RenderListener {

  /**
   * informs the implementor that the component is going to be rendered
   */
  void startRendering(RequestContext context);
  
  /**
   * informs the implementor that the tree has been rendered
   */
  void stopRendering();

}
