package com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.Consumer;

public class ToggleItemStack extends InteractableItemStack {

    private boolean state;
    private final Material onMaterial;
    private final Material offMaterial;
    private final Consumer<Boolean> onToggle;
    private final Player player;

    private final String displayNameOn;
    private final List<String> loreOn;
    private final String displayNameOff;
    private final List<String> loreOff;

    public ToggleItemStack(Player player) {
        this(false, Material.LIME_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, player, null);
    }

    public ToggleItemStack(Player player, Consumer<Boolean> onToggle) {
        this(false, Material.LIME_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, player, onToggle);
    }

    public ToggleItemStack(boolean initialState, Player player, Consumer<Boolean> onToggle) {
        this(initialState, Material.LIME_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, player, onToggle);
    }

    public ToggleItemStack(boolean initialState, Material onMaterial, Material offMaterial, Player player, Consumer<Boolean> onToggle) {
        super(new ItemStack(initialState ? onMaterial : offMaterial));
        this.state = initialState;
        this.onMaterial = onMaterial;
        this.offMaterial = offMaterial;
        this.onToggle = onToggle;
        this.player = player;

        this.displayNameOn = FriendNetPlugin.LocaleManager.getMessage(
                player.getUniqueId(),
                "gui",
                "interactables.toggle.on.diplayName"
        );

        this.loreOn = SpigotUtils.parseStringList(
                FriendNetPlugin.LocaleManager.getMessage(
                        player.getUniqueId(),
                        "gui",
                        "interactables.toggle.on.lore"
                )
        );

        this.displayNameOff = FriendNetPlugin.LocaleManager.getMessage(
                player.getUniqueId(),
                "gui",
                "interactables.toggle.off.diplayName"
        );

        this.loreOff = SpigotUtils.parseStringList(
                FriendNetPlugin.LocaleManager.getMessage(
                        player.getUniqueId(),
                        "gui",
                        "interactables.toggle.off.lore"
                )
        );

        refresh();
    }


    @Override
    public void onClick() {
        state = !state;
        itemStack.setType(state ? onMaterial : offMaterial);
        updateMeta();
        if (onToggle != null) onToggle.accept(state);
    }

    private void updateMeta() {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(state ? displayNameOn : displayNameOff);
        meta.setLore(state ? loreOn : loreOff);
        itemStack.setItemMeta(meta);
    }

    @Override
    public void refresh() {
        itemStack.setType(state ? onMaterial : offMaterial);
        updateMeta();
    }

    public boolean getState() {
        return state;
    }
}
