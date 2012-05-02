package com.pressassociation.maven.wmb.configurator;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pressassociation.maven.wmb.types.BarFile;
import com.pressassociation.maven.wmb.utils.ElementIterable;
import nu.xom.*;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.pressassociation.maven.wmb.utils.TypeSafetyHelper.typeSafeCaptureOfIterable;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@Component(role = BarConfigurator.class, hint = "default")
public class DefaultBarConfigurator extends AbstractLogEnabled implements BarConfigurator {

    private static final String BROKER_XML_ENTRY = "META-INF/broker.xml";
    private static final String ATTR_URI = "uri";
    private static final String ATTR_OVERRIDE = "override";
    private static final String XPATH_CONFIGURABLE_PROPERTY = "//ConfigurableProperty";

    @Override
    public Artifact configure(Artifact sourceArtifact, File targetArtifactFile, Properties properties) throws IOException, ParsingException {
        final BarFile file = new BarFile(sourceArtifact.getFile());
        getLogger().info("Configuring " + sourceArtifact);

        Set<String> resources = Sets.newHashSet();
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetArtifactFile));

        try {
            for (ZipEntry entry : typeSafeCaptureOfIterable(file.entries())) {
                if (!entry.isDirectory()) {
                    InputStream is = file.getInputStream(entry);
                    try {
                        String name = entry.getName();
                        int idx = name.lastIndexOf("/");
                        if (idx != -1) {
                            String dir = name.substring(0, idx);
                            if (!resources.contains(dir)) {
                                addDirectory(resources, zos, dir);
                            }
                        }
                        if (resources.contains(name)) {
                            continue;
                        }
                        if (!BROKER_XML_ENTRY.equals(name)) {
                            addResource(resources, zos, name, is);
                        } else {
                            addTransformBrokerXml(resources, zos, is, Maps.fromProperties(properties));
                        }
                    } finally {
                        IOUtil.close(is);
                    }
                }
            }
        } finally {
            IOUtil.close(zos);
        }
        return sourceArtifact;
    }

    @Override
    public Map<String, String> resolveProperties(Artifact artifact) throws IOException, ParsingException {
        final BarFile file = new BarFile(artifact.getFile());
        getLogger().info("Resolving " + artifact);

        for (ZipEntry entry : typeSafeCaptureOfIterable(file.entries())) {
            if (!entry.isDirectory() && BROKER_XML_ENTRY.equals(entry.getName())) {
                InputStream is = file.getInputStream(entry);
                try {
                    return extractProperties(is);
                } finally {
                    IOUtil.close(is);
                }
            }
        }

        return null;
    }

    /**
     * Extract map of configurable properties from archive META-INF/broker.xml.
     *
     * @param inputStream Input stream of broker xml
     * @return Map of configurable properties
     * @throws IOException
     * @throws ParsingException
     */
    private Map<String, String> extractProperties(InputStream inputStream) throws IOException, ParsingException {
        Nodes nodes = new Builder().build(checkNotNull(inputStream)).query(XPATH_CONFIGURABLE_PROPERTY);

        Map<String, String> map = Maps.newHashMap();
        for (Element e : new ElementIterable(nodes)) {
            final String uri = e.getAttributeValue(ATTR_URI);
            final Attribute override = e.getAttribute(ATTR_OVERRIDE);
            map.put(uri, override == null ? null : override.getValue());
        }
        return map;
    }

    /**
     * Transform and add broker XML to zip output stream.
     *
     * @param resources  Set of handled resources
     * @param zos        Output stream to be written to
     * @param is         Input stream of broker XML to be transformed
     * @param properties Properties to be applied to Broker XML
     * @throws IOException
     * @throws ParsingException
     */
    private void addTransformBrokerXml(Set<String> resources, ZipOutputStream zos, InputStream is,
                                       Map<String, String> properties) throws IOException, ParsingException {
        checkNotNull(properties);

        Builder builder = new Builder();
        Document document = builder.build(checkNotNull(is));

        /* Update properties */
        Nodes nodes = document.query(XPATH_CONFIGURABLE_PROPERTY);
        for (Element e : new ElementIterable(nodes)) {
            final String uri = e.getAttributeValue(ATTR_URI);
            if (properties.containsKey(uri)) {
                Attribute override = e.getAttribute(ATTR_OVERRIDE);
                if (override != null) {
                    override.setValue(properties.get(uri));
                } else {
                    e.addAttribute(new Attribute(ATTR_OVERRIDE, properties.get(uri)));
                }
                getLogger().info("Overriding configurable property '" + uri + "' with '" + properties.get(uri) + "'.");
            }
        }

        zos.putNextEntry(new ZipEntry(BROKER_XML_ENTRY));
        Serializer s = new Serializer(zos);
        s.write(document);
        resources.add(BROKER_XML_ENTRY);
    }

    /**
     * Add directory to zip output stream.
     *
     * @param resources Set of handled resources
     * @param zos       Output stream to be written to
     * @param name      name of directory
     * @throws IOException
     */
    private void addDirectory(Set<String> resources, ZipOutputStream zos, String name)
            throws IOException {
        if (name.lastIndexOf('/') > 0) {
            String parent = name.substring(0, name.lastIndexOf('/'));
            if (!resources.contains(parent)) {
                addDirectory(resources, zos, parent);
            }
        }

        // directory entries must end in "/"
        ZipEntry entry = new ZipEntry(name + "/");
        zos.putNextEntry(entry);

        resources.add(name);
    }

    /**
     * Add resource to zip output stream.
     *
     * @param resources Set of handled resources
     * @param zos       Output stream to be written to
     * @param name      Name of resource
     * @param is        Input stream of resource to be written
     * @throws IOException
     */
    private void addResource(Set<String> resources, ZipOutputStream zos, String name, InputStream is)
            throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        IOUtil.copy(is, zos);
        resources.add(name);
    }

}
