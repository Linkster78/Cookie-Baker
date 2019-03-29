package com.tek.cookiebaker.jda;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class MessageWaiter {
	
	private EventWaiter waiter;
	
	public MessageWaiter(EventWaiter waiter) {
		this.waiter = waiter;
	}
	
	public void waitForMessage(int timeout, String channelId, String userId, String messageId, Consumer<String> callbackSuccess, Runnable callbackTimeout) {
		waiter.waitForEvent(MessageReceivedEvent.class,
			(event) -> {
				return event.getChannel().getId().equals(channelId) 
						&& event.getAuthor().getId().equals(userId)
						&& !event.getMessage().getId().equals(messageId);
			}, (event) -> {
				callbackSuccess.accept(event.getMessage().getContentStripped());
			}, timeout, TimeUnit.SECONDS, callbackTimeout);
	}
	
	public void waitForMessage(int timeout, String channelId, String userId, String messageId, Predicate<Message> constraint, Consumer<String> callbackSuccess, Runnable callbackTimeout) {
		waiter.waitForEvent(MessageReceivedEvent.class,
			(event) -> {
				return event.getChannel().getId().equals(channelId) 
						&& event.getAuthor().getId().equals(userId)
						&& !event.getMessage().getId().equals(messageId)
						&& constraint.test(event.getMessage());
			}, (event) -> {
				callbackSuccess.accept(event.getMessage().getContentStripped());
			}, timeout, TimeUnit.SECONDS, callbackTimeout);
	}
	
	public EventWaiter getWaiter() {
		return waiter;
	}
	
}
