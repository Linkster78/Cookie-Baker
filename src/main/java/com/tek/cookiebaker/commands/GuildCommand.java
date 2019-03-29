package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.Optional;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.jda.MessageWaiter;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.Guild;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class GuildCommand extends Command {

	public GuildCommand() {
		this.name = "guild";
		this.aliases = new String[] {"guilds", "clan", "clans"};
		this.arguments = "<create/join/leave/invite/delete/info/edit/officers> [name/mention/key/add/remove] [value]";
		this.help = "Main guild command";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		Storage storage = CookieBaker.getInstance().getStorage();
		UserProfile profile = storage.getUserProfile(user.getId());
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		Optional<Guild> guild = storage.getGuildByMemberId(user.getId());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 1) {
			if(arguments[0].equals("info")) {
				if(guild.isPresent()) {
					event.reply(guild.get().createGuildDescription(event.getJDA()).build());
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "**You're not in a guild.**\n_Did you mean `" + serverProfile.getPrefix() + "guild info <name>` ?_ ", false);
					event.reply(builder.build());
				}
			} else if(arguments[0].equalsIgnoreCase("leave")) {
				if(guild.isPresent()) {
					if(!guild.get().getOwner().equals(user.getId())) {
						guild.get().getMembers().remove(user.getId());
						storage.updateGuild(guild.get(), "members");
						
						EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
						builder.addField("Left Guild", "You left the guild **" + guild.get().getName() + "**", false);
						event.reply(builder.build());
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Oh no!", "**You can't leave your own guild.**\n_Did you mean `" + serverProfile.getPrefix() + "guild delete` ?_", false);
						event.reply(builder.build());
					}
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "You're not in a guild.", false);
					event.reply(builder.build());
				}
			} else if(arguments[0].equalsIgnoreCase("delete")){
				if(guild.isPresent()) {
					if(guild.get().getOwner().equals(user.getId())) {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Deletion Confirmation", "You are about to delete your guild...\n**Are you sure?** (yes/no)", false);
						event.reply(builder.build());
						
						MessageWaiter waiter = CookieBaker.getInstance().getDiscord().getMessageWaiter();
						waiter.waitForMessage(10, event.getChannel().getId(), user.getId(), event.getMessage().getId(), response -> {
							if(response.equalsIgnoreCase("yes")) {
								storage.deleteGuild(guild.get());
								
								EmbedBuilder tBuilder = Reference.createBlankEmbed(Color.green, event.getJDA());
								tBuilder.addField("Guild Deleted", "You have deleted the guild **" + guild.get().getName() + "**.", false);
								event.reply(tBuilder.build());
							} else {
								EmbedBuilder tBuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
								tBuilder.addField("Deletion Cancelled", "You cancelled the guild deletion.", false);
								event.reply(tBuilder.build());
							}
						}, () -> {
							EmbedBuilder tBuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
							tBuilder.addField("Deletion Cancelled", "You didn't answer so guild deletion timed-out.", false);
							event.reply(tBuilder.build());
						});
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Oh no!", "**You must be the owner of the guild to delete it.**\n_Did you mean `" + serverProfile.getPrefix() + "guild leave` ?_", false);
						event.reply(builder.build());
					}
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "You're not in a guild.", false);
					event.reply(builder.build());
				}
			} else {
				event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
			}
		} else if(arguments.length == 2) {
			if(arguments[0].equalsIgnoreCase("info")) {
				Optional<Guild> guildOpt = storage.getGuildByName(arguments[1]);
				
				if(guildOpt.isPresent()) {
					event.reply(guildOpt.get().createGuildDescription(event.getJDA()).build());
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "There is no guild by the name of **" + arguments[1] + "**.", false);
					event.reply(builder.build());
				}
			} else if(arguments[0].equalsIgnoreCase("create")) {
				if(!guild.isPresent()) {
					if(profile.getMoney() >= Reference.GUILD_COST) {
						Optional<Guild> preGuildOpt = storage.getGuildByName(arguments[1]);
						
						if(!preGuildOpt.isPresent()) {
							if(arguments[1].length() <= 16) {
								MessageWaiter waiter = CookieBaker.getInstance().getDiscord().getMessageWaiter();
								
								EmbedBuilder cbuilder = Reference.createBlankEmbed(Color.orange, event.getJDA());
								cbuilder.addField("Creation Confirmation", "You are about to create a guild for " + Reference.FORMATTER.format(Reference.GUILD_COST) + " " + Reference.EMOTE_DOLLAR + "\n**Are you sure?** (yes/no)", false);
								event.reply(cbuilder.build());
								
								waiter.waitForMessage(15, event.getChannel().getId(), user.getId(), event.getMessage().getId(), response -> {
									if(response.equalsIgnoreCase("yes")) {
										profile.setMoney(profile.getMoney() - Reference.GUILD_COST);
										storage.updateUserProfile(profile, "money");
										Guild newGuild = new Guild(arguments[1], user.getId());
										storage.createGuild(newGuild);
										
										EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
										builder.addField("Guild Created", "Congratulations! You're now the owner of the guild **" + newGuild.getName() + "**!\n -" + Reference.FORMATTER.format(Reference.GUILD_COST) + " " + Reference.EMOTE_DOLLAR, false);
										event.reply(builder.build());
									} else {
										EmbedBuilder tBuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
										tBuilder.addField("Creation Cancelled", "You cancelled the guild creation.", false);
										event.reply(tBuilder.build());
									}
								}, () -> {
									EmbedBuilder tBuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
									tBuilder.addField("Creation Cancelled", "You didn't answer so guild creation timed-out.", false);
									event.reply(tBuilder.build());
								});
							} else {
								EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
								builder.addField("Oh no!", "Guild names must be 16 characters or less.", false);
								event.reply(builder.build());
							}
						} else {
							EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
							builder.addField("Oh no!", "A guild by the name **" + preGuildOpt.get().getName() + "** already exists.", false);
							event.reply(builder.build());
						}
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Oh no!", "You need " + Reference.FORMATTER.format(Reference.GUILD_COST) + " " + Reference.EMOTE_DOLLAR + " to create a guild.", false);
						event.reply(builder.build());
					}
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "You already own a guild.", false);
					event.reply(builder.build());
				}
			} else if(arguments[0].equalsIgnoreCase("join")) {
				if(!guild.isPresent()) {
					Optional<Guild> queryGuildOpt = storage.getGuildByName(arguments[1]);
					
					if(queryGuildOpt.isPresent()) {
						if(!queryGuildOpt.get().isInviteOnly()) {
							if(queryGuildOpt.get().getMemberCount() < queryGuildOpt.get().getMemberLimit()) {
								queryGuildOpt.get().getMembers().add(user.getId());
								storage.updateGuild(queryGuildOpt.get(), "members");
								
								EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
								builder.addField("Joined Guild", "Congratulations! You just joined the guild **" + queryGuildOpt.get().getName() + "**", false);
								event.reply(builder.build());
							} else {
								EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
								builder.addField("Oh no!", "This guild has reached its maximum capacity.", false);
								event.reply(builder.build());
							}
						} else {
							if(queryGuildOpt.get().getInvites().contains(user.getId())) {
								queryGuildOpt.get().getMembers().add(user.getId());
								queryGuildOpt.get().getInvites().remove(user.getId());
								storage.updateGuild(queryGuildOpt.get(), "members", "invites");
								
								EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
								builder.addField("Joined Guild", "Congratulations! You just joined the guild **" + queryGuildOpt.get().getName() + "**", false);
								event.reply(builder.build());
							} else {
								EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
								builder.addField("Oh no!", "You have not been invited to this guild.", false);
								event.reply(builder.build());
							}
						}
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Oh no!", "There is no guild by the name of **" + arguments[1] + "**.", false);
						event.reply(builder.build());
					}
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "You are already part of a guild.", false);
					event.reply(builder.build());
				}
			} else if(arguments[0].equalsIgnoreCase("invite")) {
				if(guild.isPresent()) {
					if(guild.get().canInvite(user.getId())) {
						if(guild.get().isInviteOnly()) {
							if(event.getMessage().getMentionedMembers().size() == 1) {
								Member mentioned = event.getMessage().getMentionedMembers().get(0);
								
								if(arguments[1].length() == mentioned.getAsMention().length() || arguments[1].length() == mentioned.getUser().getAsMention().length()) {
									if(!mentioned.getUser().isBot()) {
										storage.getUserProfile(mentioned.getUser().getId());
										
										if(guild.get().getMemberCount() < guild.get().getMemberLimit()) {
											if(!guild.get().getMembers().contains(mentioned.getUser().getId())) {
												if(!guild.get().getInvites().contains(mentioned.getUser().getId())) {
													guild.get().getInvites().add(mentioned.getUser().getId());
													storage.updateGuild(guild.get(), "invites");
													
													EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
													builder.addField("User Invited", "You have invited " + mentioned.getAsMention() + " to your guild.", false);
													event.reply(builder.build());
												} else {
													EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
													builder.addField("Oh no!", "You have already invited this user to your guild.", false);
													event.reply(builder.build());
												}
											} else {
												EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
												builder.addField("Oh no!", "This user is already part of your guild.", false);
												event.reply(builder.build());
											}
										} else {
											EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
											builder.addField("Oh no!", "Your guild has reached its maximum capacity.", false);
											event.reply(builder.build());
										}
									} else {
										EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
										builder.addField("Hey there!", "You can't invite a bot.", false);
										event.reply(builder.build());
									}
								} else {
									EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
									builder.addField("Oh no!", "You need to specify a user to invite.", false);
									event.reply(builder.build());
								}
							} else {
								EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
								builder.addField("Oh no!", "You need to specify a user to invite.", false);
								event.reply(builder.build());
							}
						} else {
							EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
							builder.addField("Oh no!", "Your guild is not in Invite Only mode.", false);
							event.reply(builder.build());
						}
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Oh no!", "You do not have the permissions to invite users to the guild.", false);
						event.reply(builder.build());
					}
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "You must be part of a guild.", false);
					event.reply(builder.build());
				}
			} else {
				event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
			}
		} else if(arguments.length >= 3) {
			if(arguments[0].equalsIgnoreCase("edit")) {
				if(guild.isPresent()) {
					if(guild.get().getOwner().equals(user.getId())) {
						if(arguments[1].equalsIgnoreCase("inviteonly")) {
							if(arguments.length == 3) {
								if(arguments[2].equalsIgnoreCase("yes")) {
									guild.get().setInviteOnly(true);
									storage.updateGuild(guild.get(), "inviteOnly");
											
									EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
									builder.addField("Invite Only Changed", "Invite Only changed to **" + (guild.get().isInviteOnly() ? "Yes" : "No") + "**", false);
									event.reply(builder.build());
								} else if(arguments[2].equalsIgnoreCase("no")) {
									guild.get().setInviteOnly(false);
									guild.get().getInvites().clear();
									storage.updateGuild(guild.get(), "inviteOnly", "invites");
											
									EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
									builder.addField("Invite Only Changed", "Invite Only changed to **" + (guild.get().isInviteOnly() ? "Yes" : "No") + "**", false);
									event.reply(builder.build());
								} else {
									EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
									builder.addField("Oh no!", "**Invalid Invite Only state.**\n_Valid States: Yes, No_", false);
									event.reply(builder.build());
								}
							} else {
								EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
								builder.addField("Oh no!", "**Invalid Invite Only state.**\n_Valid States: Yes, No_", false);
								event.reply(builder.build());
							}
						} else if(arguments[1].equalsIgnoreCase("description")) {
							StringBuilder newDescription = new StringBuilder();
							for(int i = 2; i < arguments.length; i++) newDescription.append(arguments[i] + " ");
							newDescription.setLength(newDescription.length() - 1);
								
							if(newDescription.length() <= 128) {
								guild.get().setDescription(newDescription.toString());
								storage.updateGuild(guild.get(), "description");
										
								EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
								builder.addField("Description Changed", "Description changed to **" + guild.get().getDescription() + "**", false);
								event.reply(builder.build());
							} else {
								EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
								builder.addField("Oh no!", "Descriptions have a maximum length of 128 characters.", false);
								event.reply(builder.build());
							}
						} else {
							EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
							builder.addField("Oh no!", "**Invalid guild option.**\n_Valid Options: inviteonly, description_", false);
							event.reply(builder.build());
						}
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Oh no!", "You must be the owner of the guild to change its settings.", false);
						event.reply(builder.build());
					}
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "You must be part of a guild.", false);
					event.reply(builder.build());
				}
			} else if(arguments[0].equalsIgnoreCase("officers") || arguments[0].equalsIgnoreCase("officer")) {
				if(guild.isPresent()) {
					if(guild.get().getOwner().equals(user.getId())) {
						if(arguments[1].equalsIgnoreCase("add")) {
							if(event.getMessage().getMentionedMembers().size() == 1) {
								Member mentioned = event.getMessage().getMentionedMembers().get(0);
								
								if(arguments[2].length() == mentioned.getAsMention().length() || arguments[2].length() == mentioned.getUser().getAsMention().length()) {
									if(!mentioned.getUser().isBot()) {
										if(guild.get().getMembers().contains(mentioned.getUser().getId())) {
											if(!guild.get().getOfficers().contains(mentioned.getUser().getId())) {
												guild.get().getOfficers().add(mentioned.getUser().getId());
												storage.updateGuild(guild.get(), "officers");
												
												EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
												builder.addField("Officer Added", "**" + Reference.getUsername(mentioned.getUser()) + "** has been added to the officers.", false);
												event.reply(builder.build());
											} else {
												EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
												builder.addField("Hey there!", "This user is already an officer.", false);
												event.reply(builder.build());
											}
										} else {
											EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
											builder.addField("Hey there!", "This user is not in your guild.", false);
											event.reply(builder.build());
										}
									} else {
										EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
										builder.addField("Hey there!", "You can't select a bot.", false);
										event.reply(builder.build());
									}
								} else {
									EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
									builder.addField("Oh no!", "You need to specify a user.", false);
									event.reply(builder.build());
								}
							} else {
								EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
								builder.addField("Oh no!", "You need to specify a user.", false);
								event.reply(builder.build());
							}
						} else if(arguments[1].equalsIgnoreCase("remove")) {
							if(event.getMessage().getMentionedMembers().size() == 1) {
								Member mentioned = event.getMessage().getMentionedMembers().get(0);
								
								if(arguments[2].length() == mentioned.getAsMention().length() || arguments[2].length() == mentioned.getUser().getAsMention().length()) {
									if(!mentioned.getUser().isBot()) {
										if(guild.get().getMembers().contains(mentioned.getUser().getId())) {
											if(guild.get().getOfficers().contains(mentioned.getUser().getId())) {
												guild.get().getOfficers().remove(mentioned.getUser().getId());
												storage.updateGuild(guild.get(), "officers");
												
												EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
												builder.addField("Officer Removed", "**" + Reference.getUsername(mentioned.getUser()) + "** has been removed from the officers.", false);
												event.reply(builder.build());
											} else {
												EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
												builder.addField("Hey there!", "This user is not an officer.", false);
												event.reply(builder.build());
											}
										} else {
											EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
											builder.addField("Hey there!", "This user is not in your guild.", false);
											event.reply(builder.build());
										}
									} else {
										EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
										builder.addField("Hey there!", "You can't select a bot.", false);
										event.reply(builder.build());
									}
								} else {
									EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
									builder.addField("Oh no!", "You need to specify a user.", false);
									event.reply(builder.build());
								}
							} else {
								EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
								builder.addField("Oh no!", "You need to specify a user.", false);
								event.reply(builder.build());
							}
						} else {
							EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
							builder.addField("Oh no!", "**Invalid action.**\n_Valid Actions: add, remove_", false);
							event.reply(builder.build());
						}
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Oh no!", "You must be the owner of the guild to  add/remove officers.", false);
						event.reply(builder.build());
					}
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "You must be part of a guild.", false);
					event.reply(builder.build());
				}
			} else {
				event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}

}
