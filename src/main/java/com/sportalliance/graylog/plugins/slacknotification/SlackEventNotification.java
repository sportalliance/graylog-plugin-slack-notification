/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sportalliance.graylog.plugins.slacknotification;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.graylog.events.event.EventDto;
import org.graylog.events.notifications.EventNotification;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.events.notifications.EventNotificationService;
import org.graylog.events.notifications.PermanentEventNotificationException;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog2.notifications.Notification;
import org.graylog2.notifications.NotificationService;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.streams.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.floreysoft.jmte.Engine;

public class SlackEventNotification implements EventNotification {
	public interface Factory extends EventNotification.Factory {
		@Override
		SlackEventNotification create();
	}

	private static final Logger LOG = LoggerFactory.getLogger(SlackEventNotification.class);

	private final EventNotificationService notificationCallbackService;
	private final StreamService streamService;
	private final Engine templateEngine;
	private final NotificationService notificationService;
	private final NodeId nodeId;

	@Inject
	public SlackEventNotification(EventNotificationService notificationCallbackService,
								  StreamService streamService,
								  Engine templateEngine,
								  NotificationService notificationService,
								  NodeId nodeId) {
		this.notificationCallbackService = notificationCallbackService;
		this.streamService = streamService;
		this.templateEngine = templateEngine;
		this.notificationService = notificationService;
		this.nodeId = nodeId;
	}

	@Override
	public void execute(EventNotificationContext ctx) throws PermanentEventNotificationException {
		final SlackEventNotificationConfig config = (SlackEventNotificationConfig) ctx.notificationConfig();
		SlackClient slackClient = new SlackClient(config);

		try {
			SlackMessage slackMessage = createSlackMessage(ctx, config);
			slackClient.send(slackMessage);
		} catch (Exception e) {
			String exceptionDetail = e.toString();
			if (e.getCause() != null) {
				exceptionDetail += " (" + e.getCause() + ")";
			}

			final Notification systemNotification = notificationService.buildNow()
					.addNode(nodeId.toString())
					.addType(Notification.Type.GENERIC)
					.addSeverity(Notification.Severity.NORMAL)
					.addDetail("exception", exceptionDetail);
			notificationService.publishIfFirst(systemNotification);

			throw new PermanentEventNotificationException("Slack notification is triggered, but sending failed. " + e.getMessage());
		}
	}

	private SlackMessage createSlackMessage(EventNotificationContext ctx, SlackEventNotificationConfig config) {
		//Note: Link names if notify channel or else the channel tag will be plain text.
		boolean linkNames = config.linkNames() || config.notifyChannel();
		String message = buildDefaultMessage(ctx, config);

		String customMessage = null;
		String template = config.customMessage();
		boolean hasTemplate = !isNullOrEmpty(template);
		if (hasTemplate) {
			customMessage = buildCustomMessage(ctx, config, template);
		}

		List<String> backlogItemMessages = Collections.emptyList();
		String backlogItemTemplate = config.backlogItemMessage();
		boolean hasBacklogItemTemplate = !isNullOrEmpty(backlogItemTemplate);
		if(hasBacklogItemTemplate) {
			backlogItemMessages = buildBacklogItemMessages(ctx, config, backlogItemTemplate);
		}

		return new SlackMessage(
				config.color(),
				config.iconEmoji(),
				config.iconUrl(),
				config.userName(),
				config.channel(),
				linkNames,
				message,
				customMessage,
				backlogItemMessages);
	}

	private String buildDefaultMessage(EventNotificationContext ctx, SlackEventNotificationConfig config) {
		String title = buildMessageTitle(ctx, config);

		// Build custom message
		String audience = config.notifyChannel() ? "@channel " : "";
		String description = ctx.eventDefinition().map(EventDefinitionDto::description).orElse("");
		return String.format("%s*Alert %s* triggered:\n> %s \n", audience, title, description);
	}

	private String buildMessageTitle(EventNotificationContext ctx, SlackEventNotificationConfig config) {
		String graylogUrl = config.graylogUrl();
		String eventDefinitionName = ctx.eventDefinition().map(EventDefinitionDto::title).orElse("Unnamed");
		if(!isNullOrEmpty(graylogUrl)) {
			return "<" + graylogUrl + "|" + eventDefinitionName + ">";
		} else {
			return "_" + eventDefinitionName + "_";
		}
	}

	private String buildCustomMessage(EventNotificationContext ctx, SlackEventNotificationConfig config, String template) {
		List<Message> backlog = getAlarmBacklog(ctx, config);
		Map<String, Object> model = getCustomMessageModel(ctx, config, backlog);
		try {
			return templateEngine.transform(template, model);
		} catch (Exception e) {
			LOG.error("Exception during templating", e);
			return e.toString();
		}
	}

	private List<String> buildBacklogItemMessages(EventNotificationContext ctx, SlackEventNotificationConfig config, String template) {
		return getAlarmBacklog(ctx, config).stream()
				.map(backlogItem -> {
					Map<String, Object> model = getBacklogItemModel(ctx, config, backlogItem);
					try {
						return templateEngine.transform(template, model);
					} catch (Exception e) {
						LOG.error("Exception during templating", e);
						return e.toString();
					}
				}).collect(Collectors.toList());
	}

	private List<Message> getAlarmBacklog(EventNotificationContext ctx, SlackEventNotificationConfig config) {
		final List<MessageSummary> matchingMessages = notificationCallbackService.getBacklogForEvent(ctx);

		return matchingMessages.stream()
				.map(MessageSummary::getRawMessage)
				.collect(Collectors.toList());
	}

	private Map<String, Object> getCustomMessageModel(EventNotificationContext ctx, SlackEventNotificationConfig config, List<Message> backlog) {
		Map<String, Object> model = new HashMap<>();
		EventDto eventDto = ctx.event();
		if(eventDto.timerangeStart().isPresent()) {
			model.put("event_timerange_start", eventDto.timerangeStart().get());
			model.put("event_timerange_end", eventDto.timerangeEnd().get());
		}

		List<StreamDto> streams = streamService.loadByIds(eventDto.sourceStreams())
				.stream()
				.map(stream -> buildStreamWithUrl(stream, ctx, config))
				.collect(Collectors.toList());
		model.put("streams", streams);
		model.put("message", eventDto.message());
		model.put("priority", eventDto.priority());
		model.put("alert", eventDto.alert());

		model.put("backlog", backlog);
		model.put("backlog_size", backlog.size());

		if(!isNullOrEmpty(config.graylogUrl())) {
			model.put("graylog_url", config.graylogUrl());
		}

		return model;
	}

	private Map<String, Object> getBacklogItemModel(EventNotificationContext ctx, SlackEventNotificationConfig config, Message backlogItem) {
		Map<String, Object> model = new HashMap<>();
		EventDto eventDto = ctx.event();
		if(eventDto.timerangeStart().isPresent()) {
			model.put("event_timerange_start", eventDto.timerangeStart().get());
			model.put("event_timerange_end", eventDto.timerangeEnd().get());
		}
		model.put("streams", eventDto.sourceStreams());
		model.put("message", eventDto.message());
		model.put("priority", eventDto.priority());
		model.put("alert", eventDto.alert());

		model.put("backlog_item", backlogItem);

		if(!isNullOrEmpty(config.graylogUrl())) {
			model.put("graylog_url", config.graylogUrl());
		}

		return model;
	}

	private StreamDto buildStreamWithUrl(Stream stream, EventNotificationContext ctx, SlackEventNotificationConfig config) {
		String graylogUrl = config.graylogUrl();
		String streamUrl = null;
		if(!isNullOrEmpty(graylogUrl)) {
			streamUrl = StringUtils.appendIfMissing(graylogUrl, "/") + "streams/" + stream.getId() + "/search";

			if(ctx.eventDefinition().isPresent()) {
				EventDefinitionDto eventDefinitionDto = ctx.eventDefinition().get();
				if(eventDefinitionDto.config() instanceof AggregationEventProcessorConfig) {
					String query = ((AggregationEventProcessorConfig) eventDefinitionDto.config()).query();
					streamUrl += "?q=" + query;
				}
			}
		}

		return new StreamDto(
				stream.getId(),
				stream.getTitle(),
				stream.getDescription(),
				streamUrl);
	}

	private static class StreamDto {
		private final String id;
		private final String title;
		private final String description;
		private final String url;

		private StreamDto(String id, String title, String description, String url) {
			this.id = id;
			this.title = title;
			this.description = description;
			this.url = url;
		}

		public String getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}

		public String getUrl() {
			return url;
		}
	}
}
