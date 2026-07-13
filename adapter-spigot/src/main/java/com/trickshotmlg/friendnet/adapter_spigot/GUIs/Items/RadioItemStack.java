package com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items;

import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.RadioGroup;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class RadioItemStack extends ToggleItemStack {

    private final RadioGroup group;

    public RadioItemStack(RadioGroup group, boolean initialState, Player player) {
        this(group, initialState, player, null);
    }

    public RadioItemStack(RadioGroup group, Player player) {
        this(group, player, null);
    }

    public RadioItemStack(RadioGroup group, boolean initialState, Player player, Consumer<Boolean> onToggle) {
        super(
                initialState,
                Material.PLAYER_HEAD,
                Material.PLAYER_HEAD,
                GUIUtils.CHECK_TEXTURE,
                GUIUtils.RED_X_TEXTURE,
                player,
                "gui",
                "buttons.selector.active.displayName",
                "buttons.selector.inactive.displayName",
                "buttons.selector.active.lore",
                "buttons.selector.inactive.lore",
                onToggle
        );
        this.group = group;
        group.add(this);
    }

    public RadioItemStack(RadioGroup group, Player player, Consumer<Boolean> onToggle) {
        this(group, false, player, onToggle);
    }

    public RadioItemStack(RadioGroup group, Material onMaterial, Material offMaterial, Player player, Consumer<Boolean> onSelect) {
        this(group, false, onMaterial, offMaterial, player, onSelect);
    }

    public RadioItemStack(RadioGroup group, boolean initialState, Material onMaterial, Material offMaterial, Player player, Consumer<Boolean> onSelect) {
        super(
                initialState,
                onMaterial,
                offMaterial,
                null,
                null,
                player,
                "gui",
                "buttons.selector.active.displayName",
                "buttons.selector.inactive.displayName",
                "buttons.selector.active.lore",
                "buttons.selector.inactive.lore",
                onSelect
        );
        this.group = group;
        group.add(this);
    }

    @Override
    public void onClick() {
        // Only update if not already selected
        if (!state) {
            group.select(this);
            onToggle.accept(true);
        }
    }

    public void setSelected(boolean selected) {
        this.state = selected;
        refresh();
    }

    public boolean isSelected() {
        return state;
    }
}
