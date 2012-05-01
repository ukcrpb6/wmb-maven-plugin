package com.pressassociation.maven.wmb.configure;

import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import nu.xom.ParsingException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
import org.jfrog.maven.annomojo.annotations.MojoRequiresProject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.pressassociation.maven.wmb.configure.TypeSafetyHelper.typeSafeSet;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@MojoRequiresDependencyResolution("runtime")
@MojoRequiresProject
public abstract class AbstractAnalyzeConfigurationBarMojo extends AbstractConfigureBarMojo {

    enum Mode {INHERITED,OVERRIDDEN,OVERRIDABLE,MISSING}

    public void execute(Mode mode) throws MojoExecutionException, MojoFailureException {
        Map<String, String> barProperties = Maps.newTreeMap();
        for (Artifact artifact : typeSafeSet(project.getArtifacts(), Artifact.class)) {
            if (!"bar".equals(artifact.getType())) {
                getLog().info("Skipping " + artifact.getType() + " dependency " + artifact.getId() +
                        " when configuring bars.");
                continue;
            }

            try {
                barProperties.putAll(barConfigurator.resolveProperties(artifact));
            } catch (ParsingException e) {
                throw propagate(e);
            } catch (IOException e) {
                throw propagate(e);
            }
        }

        Map<String, String> pomProperties = Maps.fromProperties(getProperties());

        if (barProperties == null) {
            throw new MojoExecutionException("No broker archive configuration XML found!");
        }

        MapDifference<String, String> difference = Maps.difference(barProperties, pomProperties);

        if (mode == Mode.OVERRIDDEN && !difference.entriesInCommon().isEmpty()) {
            getLog().info("\n\t\t\t ----- Overridden properties ----- \n");
            for (Map.Entry<String, String> entry : difference.entriesInCommon().entrySet()) {
                getLog().info(entry.getKey() + " = " + entry.getValue());
            }
        }

        if ((mode == Mode.INHERITED || mode == Mode.OVERRIDABLE) && !difference.entriesOnlyOnLeft().isEmpty()) {
            if(mode == Mode.OVERRIDABLE) {
                getLog().info("\n\t\t\t ----- Overridable properties ----- \n");
            }
            List<Map.Entry<String, String>> warningList = Lists.newLinkedList();
            for (Map.Entry<String, String> entry : difference.entriesOnlyOnLeft().entrySet()) {
                if (entry.getValue() == null) {
                    if(mode == Mode.OVERRIDABLE) {
                        getLog().info(entry.getKey());
                    }
                } else {
                    warningList.add(entry);
                }
            }

            if (mode == Mode.INHERITED && !warningList.isEmpty()) {
                getLog().info("\n\t\t\t ----- Inherited properties ----- \n");
                for (Map.Entry<String, String> entry : warningList) {
                    getLog().info(entry.getKey() + " = " + entry.getValue());
                }
            }
        }

        if (mode == Mode.MISSING && !difference.entriesOnlyOnRight().isEmpty()) {
            getLog().info("\n\t\t\t ----- Missing properties ----- \n");
            for (Map.Entry<String, String> entry : difference.entriesOnlyOnRight().entrySet()) {
                getLog().warn(entry.getKey() + " defined to be overridden but not declared in generic bar.");
            }
        }
    }
}
