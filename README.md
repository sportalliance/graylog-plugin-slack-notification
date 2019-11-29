# Slack Notification Plugin for Graylog

[![Build Status](https://travis-ci.com/sportalliance/graylog-plugin-slack-notification.svg?branch=master)](https://travis-ci.com/sportalliance/graylog-plugin-slack-notification)

Plugin for Graylog to be able to send notifications to Slack

**Required Graylog version:** 3.1.3 and later

Usage
-----
After installation select `Slack Notification` as notification type when creating a new
notification and fill out all required fields.

![](https://github.com/sportalliance/graylog-plugin-slack-notification/blob/master/images/page_1.png)

![](https://github.com/sportalliance/graylog-plugin-slack-notification/blob/master/images/page_2.png)

Installation
------------

[Download the plugin](https://github.com/sportalliance/graylog-plugin-slack-notification/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

Templating
----------

For the `custom message` template you can use following placeholders:
- event_definition (optionally present)
- event_definition_id (`<unknown>` if not present)
- event_definition_type (`<unknown>` if not present)
- event_definition_title (`<unknown>` if not present)
- event_definition_description (`<unknown>` if not present)
- job_definition_id (`<unknown>` if not present)
- job_trigger_id (`<unknown>` if not present)
- event
- backlog (not present if empty)
- backlog_size
- graylog_url (`<unknown>` if not present)
- streams (not present if empty)

For the `backlog item message` template you can use following placeholders:
- event_definition (optionally present)
- event_definition_id (`<unknown>` if not present)
- event_definition_type (`<unknown>` if not present)
- event_definition_title (`<unknown>` if not present)
- event_definition_description (`<unknown>` if not present)
- job_definition_id (`<unknown>` if not present)
- job_trigger_id (`<unknown>` if not present)
- event
- backlog_item
- graylog_url (`<unknown>` if not present)
- streams (not present if empty)


Development
-----------

You can improve your development experience for the web interface part of your plugin
dramatically by making use of hot reloading. To do this, do the following:

* `git clone https://github.com/Graylog2/graylog2-server.git`
* `cd graylog2-server/graylog2-web-interface`
* `ln -s $YOURPLUGIN plugin/`
* `npm install && npm start`


Build
-----

This project is using Maven 3 and requires Java 8 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.

License
-------

This plugin is released under version 3.0 of the [GNU General Public License](https://www.gnu.org/licenses/gpl-3.0.txt).
