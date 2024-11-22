package fr.iban.warps.zmenu;

import fr.iban.warps.WarpsPlugin;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.Inventory;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.event.events.ButtonLoaderRegisterEvent;
import fr.maxlego08.menu.button.loader.NoneLoader;
import fr.maxlego08.menu.exceptions.InventoryException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

public class ZMenuManager implements Listener {

    private final WarpsPlugin plugin;
    private ButtonManager buttonManager;
    private InventoryManager inventoryManager;

    private Inventory warpsMainMenu;
    private NoneLoader playerWarpPaginationLoader;
    private NoneLoader tagFilterButtonLoader;
    private NoneLoader likedWarpsFilter;

    public ZMenuManager(WarpsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onButtonLoad(ButtonLoaderRegisterEvent event) {
        this.buttonManager = event.getButtonManager();
        this.inventoryManager = event.getInventoryManager();

        this.registerButtons();
        this.loadInventories();
    }

    public void loadZMenu() {
        inventoryManager = getProvider(InventoryManager.class);
        buttonManager = getProvider(ButtonManager.class);

        if (inventoryManager == null || buttonManager == null) {
            plugin.getLogger().warning("ZMenu is not installed !");
            return;
        }

        this.registerButtons();
        this.loadInventories();
    }

    public void unloadZMenu() {
        buttonManager.unregister(playerWarpPaginationLoader);
        buttonManager.unregister(tagFilterButtonLoader);
        buttonManager.unregister(likedWarpsFilter);

        inventoryManager.deleteInventories(plugin);
    }

    private void registerButtons() {
        this.playerWarpPaginationLoader = new NoneLoader(plugin, PlayerWarpPaginatedButton.class, "player_warp_pagination");
        buttonManager.register(playerWarpPaginationLoader);

        this.tagFilterButtonLoader = new NoneLoader(plugin, TagFilterButton.class, "warp_tag_filter");
        buttonManager.register(tagFilterButtonLoader);

        this.likedWarpsFilter = new NoneLoader(plugin, LikedWarpsFilterButton.class, "liked_warp_filter");
        this.buttonManager.register(likedWarpsFilter);
    }

    private void loadInventories() {
        try {
            warpsMainMenu = this.inventoryManager.loadInventoryOrSaveResource(this.plugin, "inventories/warps_menu.yml");
        } catch (InventoryException exception) {
            exception.printStackTrace();
        }
    }

    private <T> @Nullable T getProvider(Class<T> classProvider) {
        RegisteredServiceProvider<T> provider = Bukkit.getServer().getServicesManager().getRegistration(classProvider);
        return provider == null ? null : provider.getProvider();
    }

    public void openWarpsMainMenu(Player player) {
        this.inventoryManager.openInventory(player, warpsMainMenu);
    }

    public void update(Player player) {
        this.inventoryManager.updateInventory(player);
    }
}
