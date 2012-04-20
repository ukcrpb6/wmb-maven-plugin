package com.pressassociation.maven.wmb.configure;

import com.google.common.collect.FluentIterable;
import nu.xom.Element;
import nu.xom.Nodes;

import java.util.Iterator;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class ElementIterable extends FluentIterable<Element> {
    private final Nodes nodes;

    public ElementIterable(Nodes nodes) {
        this.nodes = nodes;
    }

    @Override
    public Iterator<Element> iterator() {
        return new ElementIterator(nodes);
    }
}
