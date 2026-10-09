package fr.iban.warps.placement;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Logger;

public final class WarpPlacementPolicies {

    private static final String LANDS_PLUGIN = "MSLands";

    private WarpPlacementPolicies() {
    }

    /**
     * Choisit la politique selon les plugins présents :
     * MSLands actif → claims ; MSLands présent mais désactivé → refus ; absent → partout.
     */
    public static WarpPlacementPolicy resolve(PluginManager pluginManager, Logger logger) {
        Plugin lands = pluginManager.getPlugin(LANDS_PLUGIN);

        if (lands == null) {
            logger.info("MSLands absent : les warps joueurs peuvent être placés partout.");
            return new AnywherePlacementPolicy();
        }

        if (!lands.isEnabled()) {
            logger.severe("MSLands est installé mais désactivé : création et déplacement de warps joueurs bloqués.");
            return new DenyAllPlacementPolicy();
        }

        logger.info("MSLands détecté : les warps joueurs doivent être placés dans un claim autorisé.");
        return new LandsPlacementPolicy();
    }
}
