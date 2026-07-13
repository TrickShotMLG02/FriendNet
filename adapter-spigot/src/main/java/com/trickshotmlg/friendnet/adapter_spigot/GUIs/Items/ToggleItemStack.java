package com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
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
    protected final String onTexture;
    protected final String offTexture;
    protected final Consumer<Boolean> onToggle;
    protected final Player player;

    protected final String displayNameOn;
    protected final List<String> loreOn;
    protected final String displayNameOff;
    protected final List<String> loreOff;

    public ToggleItemStack(Player player) {
        this(false, player, null);
    }

    public ToggleItemStack(Player player, Consumer<Boolean> onToggle) {
        this(false, player, onToggle);
    }

    public ToggleItemStack(boolean initialState, Player player, Consumer<Boolean> onToggle) {
        this(
                initialState,
                Material.PLAYER_HEAD,
                Material.PLAYER_HEAD,
                GUIUtils.CHECK_TEXTURE,
                GUIUtils.RED_X_TEXTURE,
                player,
                onToggle
        );
    }

    public ToggleItemStack(boolean initialState, Material onMaterial, Material offMaterial, Player player, Consumer<Boolean> onToggle) {
        this(initialState, onMaterial, offMaterial, null, null, player, onToggle);
    }

    protected ToggleItemStack(
            boolean initialState,
            Material onMaterial,
            Material offMaterial,
            String onTexture,
            String offTexture,
            Player player,
            Consumer<Boolean> onToggle
    ) {
        this(
                initialState,
                onMaterial,
                offMaterial,
                onTexture,
                offTexture,
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
            String onTexture,
            String offTexture,
            Player player,
            String type,
            String displayNameKeyOn,
            String displayNameKeyOff,
            String loreKeyOn,
            String loreKeyOff,
            Consumer<Boolean> onToggle
    ) {
        super(createToggleItem(initialState, onMaterial, offMaterial, onTexture, offTexture));
        this.state = initialState;
        this.onMaterial = onMaterial;
        this.offMaterial = offMaterial;
        this.onTexture = onTexture;
        this.offTexture = offTexture;
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
        if (state) {
            playActivateSound(player);
        } else {
            playDeactivateSound(player);
        }
        refresh();
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
        itemStack = createToggleItem(state, onMaterial, offMaterial, onTexture, offTexture);
        updateMeta();
    }

    public boolean getState() {
        return state;
    }

    private static ItemStack createToggleItem(boolean state, Material onMaterial, Material offMaterial, String onTexture, String offTexture) {
        String texture = state ? onTexture : offTexture;
        if (texture != null) {
            return SpigotUtils.createCustomPlayerHead(texture, null, null);
        }

        return new ItemStack(state ? onMaterial : offMaterial);
    }
}
