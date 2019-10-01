package com.sportalliance.graylog.plugins.slacknotification;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Collection;
import java.util.Collections;

public class SlackNotificationPlugin implements Plugin {
    @Override
    public PluginMetaData metadata() {
        return new SlackNotificationMetaData();
    }

    @Override
    public Collection<PluginModule> modules () {
        return Collections.<PluginModule>singletonList(new SlackNotificationModule());
    }
}
