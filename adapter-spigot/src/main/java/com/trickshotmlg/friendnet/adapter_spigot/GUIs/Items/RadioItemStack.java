package com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items;

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
        this(group, initialState, Material.LIME_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, player, onToggle);
    }

    public RadioItemStack(RadioGroup group, Player player, Consumer<Boolean> onToggle) {
        this(group, Material.LIME_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, player, onToggle);
    }

    public RadioItemStack(RadioGroup group, Material onMaterial, Material offMaterial, Player player, Consumer<Boolean> onSelect) {
        this(group, false, onMaterial, offMaterial, player, onSelect);
    }

    public RadioItemStack(RadioGroup group, boolean initialState, Material onMaterial, Material offMaterial, Player player, Consumer<Boolean> onSelect) {
        super(initialState, onMaterial, offMaterial, player, onSelect);
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
        player.sendMessage("Selected " + selected);
        itemStack.setType(selected ? onMaterial : offMaterial);
        updateMeta();
    }

    public boolean isSelected() {
        return state;
    }
}
