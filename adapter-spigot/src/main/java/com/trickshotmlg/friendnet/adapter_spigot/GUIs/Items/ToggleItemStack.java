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

    protected boolean state;
    protected final Material onMaterial;
    protected final Material offMaterial;
    protected final Consumer<Boolean> onToggle;
    protected final Player player;

    protected final String displayNameOn;
    protected final List<String> loreOn;
    protected final String displayNameOff;
    protected final List<String> loreOff;

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
        this(
                initialState,
                onMaterial,
                offMaterial,
                player,
                "gui",
                "buttons.toggle.active.displayName",
                "buttons.toggle.inactive.displayName",
                "buttons.toggle.active.lore",
                "buttons.toggle.inactive.lore",
                onToggle
        );
    }

    public ToggleItemStack(
            boolean initialState,
            Material onMaterial,
            Material offMaterial,
            Player player,
            String type,
            String displayNameKeyOn,
            String displayNameKeyOff,
            String loreKeyOn,
            String loreKeyOff,
            Consumer<Boolean> onToggle
    ) {
        super(new ItemStack(initialState ? onMaterial : offMaterial));
        this.state = initialState;
        this.onMaterial = onMaterial;
        this.offMaterial = offMaterial;
        this.onToggle = onToggle;
        this.player = player;

        this.displayNameOn = FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), type, displayNameKeyOn);

        this.loreOn = SpigotUtils.parseStringList(
                FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), type, loreKeyOn)
        );

        this.displayNameOff = FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), type, displayNameKeyOff);

        this.loreOff = SpigotUtils.parseStringList(
                FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), type, loreKeyOff)
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

    protected void updateMeta() {
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
