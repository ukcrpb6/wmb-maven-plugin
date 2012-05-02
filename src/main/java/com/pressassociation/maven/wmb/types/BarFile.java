package com.pressassociation.maven.wmb.types;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.pressassociation.maven.wmb.utils.ElementIterable;
import nu.xom.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class BarFile extends ZipFile {

    private Map<String, String> properties;
    private ZipEntry brokerXmlEntry;
    private static final String BROKER_XML_ENTRY = "META-INF/broker.xml";

    public BarFile(String name) throws IOException {
        super(name);
    }

    public BarFile(File file, int mode) throws IOException {
        super(file, mode);
    }

    public BarFile(File file) throws IOException {
        super(file);
    }

    public synchronized Map<String, String> getBrokerProperties() throws IOException, ParsingException {
        if (properties == null) {
            HashMap<String, String> ourProperties = Maps.newHashMap();

            Builder builder = new Builder();
            Document document = builder.build(
                    super.getInputStream(checkNotNull(getBrokerXmlEntry())));
            Nodes nodes = document.query("//ConfigurableProperty");
            for (Element e : new ElementIterable(nodes)) {
                String uri = e.getAttributeValue("uri");
                Optional<String> override = Optional.fromNullable(e.getAttributeValue("override"));
                ourProperties.put(uri, override.isPresent() ? override.get() : "");
            }
            properties = ImmutableMap.copyOf(ourProperties);
        }
        return properties;
    }

    private ZipEntry getBrokerXmlEntry() {
        if (brokerXmlEntry == null) {
            brokerXmlEntry = getEntry(BROKER_XML_ENTRY);
        }
        return brokerXmlEntry;
    }
}
