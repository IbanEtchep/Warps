package fr.iban.warps.placement;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Politique de repli quand le plugin de protection est installé mais n'a pas pu démarrer :
 * on refuse plutôt que d'autoriser des warps hors claims.
 */
public class DenyAllPlacementPolicy implements WarpPlacementPolicy {

    @Override
    public boolean canPlaceWarp(Player player, Location location) {
        return false;
    }

    @Override
    public String denyMessage() {
        return "§cLa création de warps est temporairement indisponible.";
    }
}
