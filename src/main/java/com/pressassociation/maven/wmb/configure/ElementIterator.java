package com.pressassociation.maven.wmb.configure;

import com.google.common.collect.AbstractIterator;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class ElementIterator extends AbstractIterator<Element> {

    private final Nodes nodes;
    private final int length;

    private volatile int pos;

    public ElementIterator(Nodes nodes) {
        this.nodes = nodes;
        this.length = nodes.size();
    }

    @Override
    protected Element computeNext() {
        while (pos < length) {
            Node node = nodes.get(pos++);
            if (node instanceof Element) {
                return (Element) node;
            }
        }
        return endOfData();
    }
}
