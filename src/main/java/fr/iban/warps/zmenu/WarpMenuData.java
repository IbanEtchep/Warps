package fr.iban.warps.zmenu;

import fr.iban.warps.model.Warp;
import fr.iban.warps.model.enums.SortingTime;
import fr.iban.warps.model.enums.WarpTag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Predicate;

public class WarpMenuData {

    private final OfflinePlayer target;
    private boolean onlyFavorites = false;
    private WarpTag tagFilter = null;
    private SortingTime sortingTime = SortingTime.MONTH;

    public WarpMenuData(OfflinePlayer target) {
        this.target = target;
    }

    public OfflinePlayer getTarget() {
        return target;
    }

    public boolean isOnlyFavorites() {
        return onlyFavorites;
    }

    public void setOnlyFavorites(boolean onlyFavorites) {
        this.onlyFavorites = onlyFavorites;
    }

    @Nullable
    public WarpTag getTagFilter() {
        return tagFilter;
    }

    public void setTagFilter(WarpTag tagFilter) {
        this.tagFilter = tagFilter;
    }

    @NotNull
    public SortingTime getSortingTime() {
        return sortingTime;
    }

    public void setSortingTime(SortingTime sortingTime) {
        this.sortingTime = sortingTime;
    }

    public Predicate<Warp> getWarpFilterPredicate() {
        return warp -> {
            boolean include = true;
            if(onlyFavorites) {
                include = warp.getVotes().containsKey(target.getUniqueId().toString());
            }

            if(tagFilter != null) {
                include = include && warp.getTags().contains(tagFilter.getTag());
            }

            return include;
        };
    }

    public Comparator<Warp> getWarpSortingComparator() {
        return Comparator.comparingInt(warp -> warp.getVotesIn(sortingTime.getTimeMillis()));
    }
}
