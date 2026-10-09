package fr.iban.warps.placement;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.model.land.Land;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Adaptateur MSLands : le joueur doit avoir la permission SET_WARP sur le claim.
 * Seule classe de Warps qui référence MSLands ; elle n'est chargée que si MSLands est actif.
 */
public class LandsPlacementPolicy implements WarpPlacementPolicy {

    @Override
    public boolean canPlaceWarp(Player player, Location location) {
        Land land = LandsPlugin.getInstance().getLandRepository().getLandAt(location.getChunk());
        return land != null && land.isBypassing(player, Action.SET_WARP);
    }

    @Override
    public String denyMessage() {
        return "§cVous devez vous trouver dans un de vos claims.";
    }
}
