package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConfirmationGUI extends AbstractGUI {
    private final String promptDisplayNameKey;
    private final String promptLoreKey;
    private final Map<String, Object> placeholders;
    private final Consumer<Boolean> onResult;
    private boolean resolved = false;

    public ConfirmationGUI(
            JavaPlugin plugin,
            Player player,
            String titleKey,
            String promptDisplayNameKey,
            String promptLoreKey,
            Map<String, Object> placeholders,
            Consumer<Boolean> onResult
    ) {
        super(plugin, player, 9 * 3, titleKey);
        this.promptDisplayNameKey = promptDisplayNameKey;
        this.promptLoreKey = promptLoreKey;
        this.placeholders = placeholders != null ? placeholders : Map.of();
        this.onResult = onResult;
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        inventory.setItem(13, SpigotUtils.createItem(
                Material.PAPER,
                locale(promptDisplayNameKey),
                SpigotUtils.parseStringList(locale(promptLoreKey))
        ));

        setInteractableItem(11, new ActionItemStack(
                GUIUtils.CreateLocalizedHead(
                        player,
                        GUIUtils.CHECK_TEXTURE,
                        "gui",
                        "confirmationGUI.buttons.yes.displayName",
                        "confirmationGUI.buttons.yes.lore"
                ),
                player,
                () -> resolve(true)
        ));

        setInteractableItem(15, new ActionItemStack(
                GUIUtils.CreateLocalizedHead(
                        player,
                        GUIUtils.RED_X_TEXTURE,
                        "gui",
                        "confirmationGUI.buttons.no.displayName",
                        "confirmationGUI.buttons.no.lore"
                ),
                player,
                () -> resolve(false),
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        fillEmptySlots();
    }

    @Override
    public void onClose() {
        if (!resolved) {
            resolved = true;
            if (onResult != null) {
                onResult.accept(false);
            }
        }
    }

    private void resolve(boolean result) {
        if (resolved) {
            return;
        }

        resolved = true;
        if (onResult != null) {
            onResult.accept(result);
        }

        if (AbstractGUI.getOpenGUI(player) == this) {
            goBack();
        }
    }

    private String locale(String key) {
        return FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), "gui", key, placeholders);
    }

    private void fillEmptySlots() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }
}
