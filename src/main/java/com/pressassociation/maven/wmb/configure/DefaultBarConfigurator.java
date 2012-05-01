package com.pressassociation.maven.wmb.configure;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import static com.pressassociation.maven.wmb.configure.TypeSafetyHelper.typeSafeCaptureOfIterable;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@Component(role = BarConfigurator.class, hint = "default")
public class DefaultBarConfigurator extends AbstractLogEnabled implements BarConfigurator {

    private static final String BROKER_XML_ENTRY = "META-INF/broker.xml";

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
                            addTransformBrokerXml(resources, zos, name, is, Maps.fromProperties(properties));
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
            if (!entry.isDirectory()) {
                InputStream is = file.getInputStream(entry);
                try {
                    String name = entry.getName();
                    if (BROKER_XML_ENTRY.equals(name)) {
                        return extractProperties(is);
                    }
                } finally {
                    IOUtil.close(is);
                }
            }
        }

        return null;
    }

    public Map<String, String> extractProperties(InputStream inputStream) throws IOException, ParsingException {
        Nodes nodes = new Builder().build(checkNotNull(inputStream)).query("//ConfigurableProperty");

//        ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        Map<String, String> map = Maps.newHashMap();
        for (Element e : new ElementIterable(nodes)) {
            final String uri = e.getAttributeValue("uri");
            final Attribute override = e.getAttribute("override");
            map.put(uri, override == null ? null : override.getValue());
        }
        return map; //.build();
    }

    public void addTransformBrokerXml(Set<String> resources, ZipOutputStream zos, String name, InputStream is,
                                      Map<String, String> properties) throws IOException, ParsingException {
        checkNotNull(properties);

        Builder builder = new Builder();
        Document document = builder.build(checkNotNull(is));

        /* Update properties */
        Nodes nodes = document.query("//ConfigurableProperty");
        for (Element e : new ElementIterable(nodes)) {
            final String uri = e.getAttributeValue("uri");
            if (properties.containsKey(uri)) {
                Attribute override = e.getAttribute("override");
                if (override != null) {
                    override.setValue(properties.get(uri));
                } else {
                    e.addAttribute(new Attribute("override", properties.get(uri)));
                }
                getLogger().info("Overriding configurable property '" + uri + "' with '" + properties.get(uri) + "'.");
            }
        }

        zos.putNextEntry(new ZipEntry(name));
        Serializer s = new Serializer(zos);
        s.write(document);
        resources.add(name);
    }

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

    private void addResource(Set<String> resources, ZipOutputStream zos, String name, InputStream is)
            throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        IOUtil.copy(is, zos);
        resources.add(name);
    }


}
