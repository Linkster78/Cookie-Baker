package com.tek.cookiebaker.listeners;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.enums.LevelRank;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

public class RoleUpdater implements Consumer<CommandEvent> {

	@Override
	public void accept(CommandEvent event) {
		Storage storage = CookieBaker.getInstance().getStorage();
		UserProfile profile = storage.getUserProfile(event.getAuthor());
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		
		updateRoles(serverProfile, profile, event.getGuild(), event.getMember(), event.getSelfMember());
	}
	
	public static void updateRoles(ServerProfile serverProfile, UserProfile userProfile, Guild guild, Member member, Member self) {
		Map<String, String> roleMap = serverProfile.getRoleMap();
		
		if(roleMap.isEmpty()) return;
		if(!self.hasPermission(Permission.MANAGE_ROLES)) return;
		
		Optional<Role> highestRole = self.getRoles().stream().max(Comparator.comparing(Role::getPosition));
		
		if(!highestRole.isPresent()) return;
		
		for(String roleId : roleMap.values()) {
			Role rankRole = guild.getRoleById(roleId);
			if(rankRole == null) return;
			if(highestRole.get().getPosition() < rankRole.getPosition()) return;
		}
		
		LevelRank rank = LevelRank.getLevelRank(userProfile.getLevel());
		List<Role> roles = member.getRoles();
		List<Role> toRemove = roles.stream()
				.filter(role -> roleMap.containsValue(role.getId()) 
						&& !roleMap.get(rank.getId() + "").equals(role.getId()))
				.collect(Collectors.toList());
		Role currentRole = guild.getRoleById(roleMap.get(rank.getId() + ""));
		List<Role> toAdd = Arrays.asList(currentRole);
		
		if(toRemove.isEmpty() && member.getRoles().contains(currentRole)) return;
		
		guild.getController().modifyMemberRoles(member, toAdd, toRemove).queue();
	}

}
