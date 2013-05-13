package com.pressassociation.maven.wmb.types;

import com.google.common.base.Objects;

import java.util.Properties;

public class ConfigurableServiceItem {
    /**
     * @parameter
     * @required
     */
    private String type;

    /**
     * @parameter
     * @required
     */
    private String name;

    /**
     * @parameter
     */
    private Properties properties;

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(ConfigurableServiceItem.class).add("type", type).add("name", name).toString();
    }
}
