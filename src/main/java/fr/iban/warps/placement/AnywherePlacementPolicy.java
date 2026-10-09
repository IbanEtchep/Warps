package fr.iban.warps.placement;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Politique par défaut quand aucun plugin de protection n'est installé : warp autorisé partout.
 */
public class AnywherePlacementPolicy implements WarpPlacementPolicy {

    @Override
    public boolean canPlaceWarp(Player player, Location location) {
        return true;
    }

    @Override
    public String denyMessage() {
        return "";
    }
}
