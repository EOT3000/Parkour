package me.A5H73Y.Parkour.Listeners;

import me.A5H73Y.Parkour.Managers.QuestionManager;
import me.A5H73Y.Parkour.Parkour;
import me.A5H73Y.Parkour.Player.PlayerInfo;
import me.A5H73Y.Parkour.Player.PlayerMethods;
import me.A5H73Y.Parkour.Utilities.Static;
import me.A5H73Y.Parkour.Utilities.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!Parkour.getSettings().isChatPrefix())
            return;

        String finalMessage;
        String rank = PlayerInfo.getRank(event.getPlayer());

        // should we completely override the chat format
        if (Parkour.getSettings().isChatPrefixOverride()) {
            finalMessage = Utils.colour(Utils.getTranslation("Event.Chat", false)
                    .replace("%RANK%", rank)
                    .replace("%PLAYER%", event.getPlayer().getDisplayName())
                    .replace("%MESSAGE%", event.getMessage()));
        } else {
            // or do we use the existing format, just replacing the Parkour variables
            finalMessage = Utils.colour(event.getFormat()
                    .replace("%RANK%", rank)
                    .replace("%PLAYER%", event.getPlayer().getDisplayName()));
        }

        event.setFormat(finalMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        boolean commandIsPa = event.getMessage().startsWith("/pa ")
                || event.getMessage().startsWith("/parkour ")
                || event.getMessage().startsWith("/pkr ");

        Player player = event.getPlayer();

        if (commandIsPa && QuestionManager.getInstance().hasPlayerBeenAskedQuestion(player.getName())) {
            String[] args = event.getMessage().split(" ");
            if (args.length <= 1) {
                player.sendMessage(Static.getParkourString() + "Invalid answer.");
                player.sendMessage("Please use either " + ChatColor.GREEN + "/pa yes" + ChatColor.WHITE + " or " + ChatColor.AQUA + "/pa no");
            } else {
                QuestionManager.getInstance().answerQuestion(player, args[1]);
            }
            event.setCancelled(true);
        }

        if (!commandIsPa && PlayerMethods.isPlaying(player.getName())) {
            if (!Parkour.getSettings().isDisableCommandsOnCourse())
                return;

            if (player.hasPermission("Parkour.Admin.*") || player.hasPermission("Parkour.*"))
                return;

            boolean allowed = false;
            for (String word : Parkour.getSettings().getWhitelistedCommands()) {
                if (event.getMessage().startsWith("/" + word + " ") || (event.getMessage().equalsIgnoreCase("/" + word))) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                event.setCancelled(true);
                player.sendMessage(Utils.getTranslation("Error.Command"));
            }
        }
    }
}
