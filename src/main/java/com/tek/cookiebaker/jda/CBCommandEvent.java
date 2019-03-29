package com.tek.cookiebaker.jda;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.Checks;

public class CBCommandEvent extends CommandEvent {

	public CBCommandEvent(MessageReceivedEvent event, String args, CommandClient client) {
		super(event, args, client);
	}
	
	@Override
	public void linkId(Message message) {
		Checks.check(message.getAuthor().equals(getSelfUser()), "Attempted to link a Message who's author was not the bot!");
		((CBCommandClientImpl)getClient()).linkIds(getEvent().getMessageIdLong(), message);
	}
	
	@Override
	public void reply(MessageEmbed embed) {
		try {
			super.reply(embed);
		} catch(Exception e) { }
	}
	
	@Override
	public void reply(Message message) {
		try {
			super.reply(message);
		} catch(Exception e) { }
	}

}
