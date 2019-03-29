package com.tek.cookiebaker.main;

import java.util.stream.Collectors;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.tek.cookiebaker.commands.BakeCommand;
import com.tek.cookiebaker.commands.GameCommand;
import com.tek.cookiebaker.commands.GuildCommand;
import com.tek.cookiebaker.commands.HelpCommand;
import com.tek.cookiebaker.commands.InviteCommand;
import com.tek.cookiebaker.commands.LeaderboardCommand;
import com.tek.cookiebaker.commands.ManualCommand;
import com.tek.cookiebaker.commands.OvenCommand;
import com.tek.cookiebaker.commands.PackageCommand;
import com.tek.cookiebaker.commands.ProfileCommand;
import com.tek.cookiebaker.commands.RanksCommand;
import com.tek.cookiebaker.commands.RepairCommand;
import com.tek.cookiebaker.commands.SampleCommand;
import com.tek.cookiebaker.commands.SellCommand;
import com.tek.cookiebaker.commands.ServerCommand;
import com.tek.cookiebaker.commands.SettingsCommand;
import com.tek.cookiebaker.commands.ShopCommand;
import com.tek.cookiebaker.commands.TransferCommand;
import com.tek.cookiebaker.commands.UpgradesCommand;
import com.tek.cookiebaker.commands.VoteCommand;
import com.tek.cookiebaker.commands.admin.AdminClearCommand;
import com.tek.cookiebaker.commands.admin.AdminProfileCommand;
import com.tek.cookiebaker.commands.admin.AdminShutdownCommand;
import com.tek.cookiebaker.commands.admin.AdminTopCommand;
import com.tek.cookiebaker.commands.admin.AdminUpdateDonationsCommand;
import com.tek.cookiebaker.jda.CBCommandClientBuilder;
import com.tek.cookiebaker.jda.MessageWaiter;
import com.tek.cookiebaker.listeners.RoleUpdater;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DiscordBot {
	
	private ShardManager shardManager;
	private EventWaiter waiter;
	private MessageWaiter messageWaiter;
	
	public void connect(String token) throws LoginException, InterruptedException {
		DefaultShardManagerBuilder shardBuilder = new DefaultShardManagerBuilder();
		shardBuilder.setToken(CookieBaker.getInstance().getConfig().getToken());
		
		CBCommandClientBuilder commandBuilder = new CBCommandClientBuilder();
		
		commandBuilder.setPrefix((server) -> {
			ServerProfile profile = CookieBaker.getInstance().getStorage().getServerProfile(server);
			return profile.getPrefix();
		});
		
		commandBuilder.setOwnerId(Reference.OWNER_ID);
		commandBuilder.addCommands(new ProfileCommand(), new BakeCommand()
								, new SellCommand(), new TransferCommand()
								, new ShopCommand(), new LeaderboardCommand()
								, new OvenCommand(), new RanksCommand()
								, new RepairCommand(), new SampleCommand()
								, new UpgradesCommand(), new PackageCommand()
								, new GameCommand(), new GuildCommand()
								, new InviteCommand(), new VoteCommand()
								, new ServerCommand(), new ManualCommand()
								, new SettingsCommand(), new AdminTopCommand()
								, new AdminProfileCommand(), new AdminClearCommand()
								,new AdminUpdateDonationsCommand(), new AdminShutdownCommand());
		commandBuilder.setHelpConsumer(new HelpCommand());
		
		commandBuilder.setCanCommand(user -> {
			UserProfile profile = CookieBaker.getInstance().getStorage().getUserProfile(user);
			return !profile.isDisabled();
		});
		
		waiter = new EventWaiter();
		messageWaiter = new MessageWaiter(waiter);
		
		RoleUpdater roleUpdater = new RoleUpdater();
		commandBuilder.setGenericCommand(roleUpdater);
		
		shardBuilder.addEventListeners(commandBuilder.build(), waiter);
		shardBuilder.addEventListeners(CookieBaker.getInstance().getGameManager().getGames().stream().map(game -> (ListenerAdapter)game).collect(Collectors.toList()));
		
		shardManager = shardBuilder.build();
	}
	
	public void shutdown() {
		if(shardManager != null) shardManager.shutdown();
	}
	
	public ShardManager getShardManager() {
		return shardManager;
	}
	
	public EventWaiter getWaiter() {
		return waiter;
	}
	
	public MessageWaiter getMessageWaiter() {
		return messageWaiter;
	}
	
}
