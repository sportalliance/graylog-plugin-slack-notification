import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';

import SlackNotificationForm from 'form/SlackNotificationForm';
import SlackNotificationSummary from 'form/SlackNotificationSummary';


PluginStore.register(new PluginManifest({}, {

  eventNotificationTypes: [
    {
      type: 'slack-notification-v1',
      displayName: 'Slack Notification',
      formComponent: SlackNotificationForm,
      summaryComponent: SlackNotificationSummary,
      defaultConfig: SlackNotificationForm.defaultConfig
    }
  ]
}));
