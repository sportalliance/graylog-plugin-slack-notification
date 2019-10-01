package com.sportalliance.graylog.plugins.slacknotification;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class SlackNotificationMetaData implements PluginMetaData {
    private static final String PLUGIN_PROPERTIES = "com.sportalliance.graylog.plugins.graylog-plugin-slack-notification/graylog-plugin.properties";

    @Override
    public String getUniqueId() {
        return "com.sportalliance.graylog.plugins.slacknotification.SlackNotificationPlugin";
    }

    @Override
    public String getName() {
        return "SlackNotification";
    }

    @Override
    public String getAuthor() {
        return "Sport Alliance GmbH <sascha.boeing@sportalliance.com>";
    }

    @Override
    public URI getURL() {
        return URI.create("https://github.com/sportalliance/graylog-plugin-slack-notification");
    }

    @Override
    public Version getVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "version", Version.from(0, 0, 0, "unknown"));
    }

    @Override
    public String getDescription() {
        return "Allows to send notifications to Slack";
    }

    @Override
    public Version getRequiredVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "graylog.version", Version.from(0, 0, 0, "unknown"));
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
