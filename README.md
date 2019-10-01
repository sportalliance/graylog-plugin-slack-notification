# Slack Notification Plugin for Graylog

[![Build Status](https://travis-ci.org/sportalliance/graylog-plugin-slack-notification.svg?branch=master)](https://travis-ci.org/sportalliance/graylog-plugin-slack-notification)

**WIP**

Plugin for Graylog to be able to send notifications to Slack

**Required Graylog version:** 3.1 and later

Installation
------------

[Download the plugin](https://github.com/sportalliance/graylog-plugin-slack-notification/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

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