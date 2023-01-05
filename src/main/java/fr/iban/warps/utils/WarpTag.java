package fr.iban.warps.utils;

import fr.iban.warps.objects.Warp;

import java.util.function.Predicate;

public enum WarpTag {

        SHOP("#shop"),
        VILLE("#ville"),
        FARM("#farm");

    private final String tag;

    WarpTag(String tag) {
        this.tag = tag;
    }

    public Predicate<Warp> hasTag() {
        return warp -> warp.getTags().contains(tag);
    }
}
