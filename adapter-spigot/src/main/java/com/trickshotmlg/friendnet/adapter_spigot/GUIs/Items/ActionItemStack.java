package com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ActionItemStack extends InteractableItemStack {

    public enum SoundProfile {
        ACTION,
        NAVIGATION
    }

    private final Runnable onAction;
    private final Player player;
    private final SoundProfile soundProfile;

    private String displayName = "";
    private List<String> lore = List.of();

    public ActionItemStack(Material material, Player player, String type, String displayNameKey, String loreKey) {
        this(material, player, type, displayNameKey, loreKey, null);
    }

    public ActionItemStack(Material material, Player player, String type, String displayNameKey, String loreKey, Runnable onAction) {
        this(new ItemStack(material), player, type, displayNameKey, loreKey, onAction);
    }

    public ActionItemStack(ItemStack itemStack, Player player, String type, String displayNameKey, String loreKey) {
        this(itemStack, player, type, displayNameKey, loreKey, null);
    }

    public ActionItemStack(ItemStack itemStack, Player player, String type, String displayNameKey, String loreKey, Runnable onAction) {
        this(itemStack, player, type, displayNameKey, loreKey, onAction, SoundProfile.ACTION);
    }

    public ActionItemStack(ItemStack itemStack, Player player, String type, String displayNameKey, String loreKey, Runnable onAction, SoundProfile soundProfile) {
        this(itemStack, player, onAction, soundProfile);
        this.displayName = FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), type, displayNameKey);
        this.lore = SpigotUtils.parseStringList(FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), type, loreKey));
    }

    public ActionItemStack(ItemStack itemStack, Player player, Runnable onAction) {
        this(itemStack, player, onAction, SoundProfile.ACTION);
    }

    public ActionItemStack(ItemStack itemStack, Player player, Runnable onAction, SoundProfile soundProfile) {
        super(itemStack);
        this.player = player;
        this.onAction = onAction;
        this.soundProfile = soundProfile;
    }


    @Override
    public void onClick() {
        if (soundProfile == SoundProfile.NAVIGATION) {
            playNavigationSound(player);
        } else {
            playActivateSound(player);
        }
        if (onAction != null) onAction.run();
    }
}
