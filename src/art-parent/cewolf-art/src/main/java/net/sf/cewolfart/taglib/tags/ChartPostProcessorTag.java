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

package net.sf.cewolfart.taglib.tags;

import java.io.Serializable;
import javax.servlet.jsp.JspException;

import net.sf.cewolfart.ChartPostProcessor;
import net.sf.cewolfart.NonSerializableChartPostProcessor;
import net.sf.cewolfart.taglib.util.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Tag to define a post processor.
 * @see ChartPostProcessor
 * @author  Guido Laures 
 */
public class ChartPostProcessorTag extends AbstractParameterizedObjectTag {

    private static final Logger logger = LoggerFactory.getLogger(ChartPostProcessorTag.class);

	static final long serialVersionUID = 2608233610238632442L;

	@Override
    public int doEndTag() throws JspException {
        ChartPostProcessor pp = null;
        try {
            pp = (ChartPostProcessor) getObject();
            if (pp == null) {
                throw new JspException("Could not find ChartPostProcessor under ID '" + getId() + "'");
            }
        } catch (ClassCastException cce) {
            throw new JspException("Bean under ID '" + getId() + "' is of type '"
            + getObject().getClass().getName() + "'.\nType expected:" + ChartPostProcessor.class.getName());
        }
        AbstractChartTag rt = (AbstractChartTag) PageUtils.findRoot(this, pageContext);
        ChartPostProcessor cpp = (ChartPostProcessor) getObject();
		if (cpp instanceof Serializable) {
			rt.addChartPostProcessor(cpp, getStringParameters());
		} else if (cpp instanceof NonSerializableChartPostProcessor) {
			rt.addChartPostProcessor(((NonSerializableChartPostProcessor) cpp).getSerializablePostProcessor(), getStringParameters());
		} else {
			logger.error("ChartPostProcessor {} implements neither Serializable nor NonSerializableChartPostProcessor. It will be ignored.", cpp.getClass().getName());
		}
        return doAfterEndTag(EVAL_PAGE);
    }
}
