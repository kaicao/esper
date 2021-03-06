/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.internal.event.xml;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.PropertyAccessException;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionField;
import com.espertech.esper.common.internal.event.core.EventPropertyGetterSPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

/**
 * Getter for retrieving a value at a certain index.
 */
public class DOMIndexedGetter implements EventPropertyGetterSPI, DOMPropertyGetter {
    private final String propertyName;
    private final int index;
    private final FragmentFactoryDOMGetter fragmentFactory;

    /**
     * Ctor.
     *
     * @param propertyName    property name
     * @param index           index
     * @param fragmentFactory for creating fragments if required
     */
    public DOMIndexedGetter(String propertyName, int index, FragmentFactoryDOMGetter fragmentFactory) {
        this.propertyName = propertyName;
        this.index = index;
        this.fragmentFactory = fragmentFactory;
    }

    public Node[] getValueAsNodeArray(Node node) {
        return null;
    }

    public Object getValueAsFragment(Node node) {
        if (fragmentFactory == null) {
            return null;
        }
        Node result = getValueAsNode(node);
        if (result == null) {
            return null;
        }
        return fragmentFactory.getEvent(result);
    }

    private CodegenMethod getValueAsFragmentCodegen(CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        CodegenExpressionField member = codegenClassScope.addFieldUnshared(true, FragmentFactory.EPTYPE, fragmentFactory.make(codegenClassScope.getPackageScope().getInitMethod(), codegenClassScope));
        CodegenMethod method = codegenMethodScope.makeChild(EPTypePremade.OBJECT.getEPType(), this.getClass(), codegenClassScope).addParam(EPTypePremade.NODE.getEPType(), "node");
        method.getBlock()
                .declareVar(EPTypePremade.NODE.getEPType(), "result", staticMethod(DOMIndexedGetter.class, "getNodeValue", ref("node"), constant(propertyName), constant(index)))
                .ifRefNullReturnNull("result")
                .methodReturn(exprDotMethod(member, "getEvent", ref("result")));
        return method;
    }

    public Node getValueAsNode(Node node) {
        return getNodeValue(node, propertyName, index);
    }

    public Object get(EventBean eventBean) throws PropertyAccessException {
        Object result = eventBean.getUnderlying();
        if (!(result instanceof Node)) {
            return null;
        }
        Node node = (Node) result;
        return getValueAsNode(node);
    }

    public boolean isExistsProperty(EventBean eventBean) {
        Object result = eventBean.getUnderlying();
        if (!(result instanceof Node)) {
            return false;
        }
        Node node = (Node) result;
        return getValueAsNode(node) != null;
    }

    public Object getFragment(EventBean eventBean) {
        Object result = eventBean.getUnderlying();
        if (!(result instanceof Node)) {
            return null;
        }
        Node node = (Node) result;
        return getValueAsFragment(node);
    }

    public CodegenExpression eventBeanGetCodegen(CodegenExpression beanExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return underlyingGetCodegen(castUnderlying(EPTypePremade.NODE.getEPType(), beanExpression), codegenMethodScope, codegenClassScope);
    }

    public CodegenExpression eventBeanExistsCodegen(CodegenExpression beanExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return underlyingExistsCodegen(castUnderlying(EPTypePremade.NODE.getEPType(), beanExpression), codegenMethodScope, codegenClassScope);
    }

    public CodegenExpression eventBeanFragmentCodegen(CodegenExpression beanExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return underlyingFragmentCodegen(castUnderlying(EPTypePremade.NODE.getEPType(), beanExpression), codegenMethodScope, codegenClassScope);
    }

    public CodegenExpression underlyingGetCodegen(CodegenExpression underlyingExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return staticMethod(this.getClass(), "getNodeValue", underlyingExpression, constant(propertyName), constant(index));
    }

    public CodegenExpression underlyingExistsCodegen(CodegenExpression underlyingExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return staticMethod(this.getClass(), "getNodeValueExists", underlyingExpression, constant(propertyName), constant(index));
    }

    public CodegenExpression underlyingFragmentCodegen(CodegenExpression underlyingExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        if (fragmentFactory == null) {
            return constantNull();
        }
        return localMethod(getValueAsFragmentCodegen(codegenMethodScope, codegenClassScope), underlyingExpression);
    }

    public CodegenExpression getValueAsNodeCodegen(CodegenExpression value, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return underlyingGetCodegen(value, codegenMethodScope, codegenClassScope);
    }

    public CodegenExpression getValueAsNodeArrayCodegen(CodegenExpression value, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return constantNull();
    }

    public CodegenExpression getValueAsFragmentCodegen(CodegenExpression value, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return underlyingFragmentCodegen(value, codegenMethodScope, codegenClassScope);
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param node         node
     * @param propertyName property
     * @param index        index
     * @return value
     */
    public static Node getNodeValue(Node node, String propertyName, int index) {
        NodeList list = node.getChildNodes();
        int count = 0;
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            if (childNode == null) {
                continue;
            }
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String elementName = childNode.getLocalName();
            if (elementName == null) {
                elementName = childNode.getNodeName();
            }

            if (!(propertyName.equals(elementName))) {
                continue;
            }

            if (count == index) {
                return childNode;
            }
            count++;
        }
        return null;
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param node         node
     * @param propertyName property
     * @param index        index
     * @return value
     */
    public static boolean getNodeValueExists(Node node, String propertyName, int index) {
        return getNodeValue(node, propertyName, index) != null;
    }
}
