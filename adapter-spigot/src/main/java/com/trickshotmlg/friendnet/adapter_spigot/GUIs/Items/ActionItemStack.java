package com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.Consumer;

public class ActionItemStack extends InteractableItemStack {

    private final Runnable onAction;
    private final Player player;

    private final String displayName;
    private final List<String> lore;

    public ActionItemStack(Material material, Player player) {
        this(material, player, null);
    }

    public ActionItemStack(Material material, Player player, Runnable onAction) {
        super(new ItemStack(material));
        this.player = player;
        this.onAction = onAction;

        this.displayName = FriendNetPlugin.LocaleManager.getMessage(
                player.getUniqueId(),
                "gui",
                "interactables.action.on.diplayName"
        );

        this.lore = SpigotUtils.parseStringList(
                FriendNetPlugin.LocaleManager.getMessage(
                        player.getUniqueId(),
                        "gui",
                        "interactables.action.on.lore"
                )
        );

        refresh();
    }


    @Override
    public void onClick() {
        if (onAction != null) onAction.run();
    }
}
