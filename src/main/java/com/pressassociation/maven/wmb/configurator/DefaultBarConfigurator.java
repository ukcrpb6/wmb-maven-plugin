package com.pressassociation.maven.wmb.configurator;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;
import com.pressassociation.maven.wmb.types.BarFile;
import com.pressassociation.maven.wmb.utils.ElementIterable;
import nu.xom.*;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;

import java.io.*;
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
    private static final String EXT_LIBRARY = ".libzip";
    private static final String EXT_APPLICATION = ".appzip";

    @Override
    public Artifact configure(Artifact sourceArtifact, final File targetArtifactFile, Properties properties) throws IOException, ParsingException {
        final BarFile file = new BarFile(sourceArtifact.getFile());
        getLogger().info("Configuring source artifact : " + sourceArtifact);

        repackArchive(file, createZipOutputStreamSupplier(targetArtifactFile), Sets.<String>newHashSet(), properties);

        return sourceArtifact;
    }

    private OutputSupplier<ZipOutputStream> createZipOutputStreamSupplier(final File file) {
        return new OutputSupplier<ZipOutputStream>() {
            @Override public ZipOutputStream getOutput() throws IOException {
                return new ZipOutputStream(new FileOutputStream(file));
            }
        };
    }

    private void repackArchive(BarFile file, OutputSupplier<ZipOutputStream> supplier, Set<String> resources, Properties properties) throws IOException, ParsingException {
        ZipOutputStream zos = supplier.getOutput();
        try {
            repackArchive(file, zos, resources, properties);
        } finally {
            zos.close();
        }
    }

    private void repackArchive(BarFile file, ZipOutputStream zos, Set<String> resources, Properties properties) throws IOException, ParsingException {
        for (ZipEntry entry : typeSafeCaptureOfIterable(file.entries())) {
            if (!entry.isDirectory()) {
                InputStream is = file.getInputStream(entry);
                try {
                    final String name = entry.getName();
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
                    if (name.endsWith(EXT_LIBRARY) || name.endsWith(EXT_APPLICATION)) {
                        File tmpFile = createTemporaryExtractedFile(name, is);
                        final File tmpTargetFile = File.createTempFile("temp", name);
                        try {
                            repackArchive(new BarFile(tmpFile), createZipOutputStreamSupplier(tmpTargetFile), Sets.<String>newHashSet(), properties);
                            zos.putNextEntry(new ZipEntry(name));
                            ByteStreams.copy(new InputSupplier<InputStream>() {
                                @Override public InputStream getInput() throws IOException {
                                    return new FileInputStream(tmpTargetFile);
                                }
                            }, zos);
                            resources.add(name);
                        } finally {
                            tmpFile.delete();
                            tmpTargetFile.delete();
                        }
                    } else if (BROKER_XML_ENTRY.equals(name)) {
                        addTransformBrokerXml(resources, zos, is, Maps.fromProperties(properties));
                    } else {
                        addResource(resources, zos, name, is);
                    }
                } finally {
                    IOUtil.close(is);
                }
            }
        }
    }

    private File createTemporaryExtractedFile(String originalFilename, InputStream is) throws IOException {
        final File tmpFile = File.createTempFile("zip", "-" + originalFilename);
        ByteStreams.copy(is, new OutputSupplier<OutputStream>() {
            @Override public OutputStream getOutput() throws IOException {
                return new FileOutputStream(tmpFile);
            }
        });
        return tmpFile;
    }

    @Override
    public Map<String, String> resolveProperties(Artifact artifact) throws IOException, ParsingException {
        getLogger().info("Resolving " + artifact);
        return resolveProperties(new BarFile(artifact.getFile()));
    }

    private Map<String, String> resolveProperties(BarFile file) throws IOException, ParsingException {
        getLogger().info("Resolving properties for " + file.getName());

        Map<String, String> map = Maps.newHashMap();

        for (ZipEntry entry : typeSafeCaptureOfIterable(file.entries())) {
            if (!entry.isDirectory()) {
                if (entry.getName().endsWith(EXT_LIBRARY) || entry.getName().endsWith(EXT_APPLICATION)) {
                    File tmpFile = createTemporaryExtractedFile(entry.getName(), file.getInputStream(entry));
                    try {
                        map.putAll(resolveProperties(new BarFile(tmpFile)));
                    } finally {
                        tmpFile.delete();
                    }
                } else if (BROKER_XML_ENTRY.equals(entry.getName())) {
                    InputStream is = file.getInputStream(entry);
                    try {
                        map.putAll(extractProperties(is));
                        return map;
                    } finally {
                        IOUtil.close(is);
                    }
                }
            }
        }

        return map;
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
