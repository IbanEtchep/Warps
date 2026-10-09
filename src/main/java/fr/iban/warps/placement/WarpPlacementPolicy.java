package fr.iban.warps.placement;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Décide si un joueur peut placer son warp à une position donnée.
 * Permet à Warps de fonctionner sans dépendre d'un plugin de protection précis.
 */
public interface WarpPlacementPolicy {

    boolean canPlaceWarp(Player player, Location location);

    /**
     * Message envoyé au joueur quand {@link #canPlaceWarp} refuse.
     */
    String denyMessage();
}
