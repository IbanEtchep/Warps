package fr.iban.warps.model.enums;

import fr.iban.warps.model.Warp;

import java.util.function.Predicate;

public enum WarpTag {

        SHOP("#shop"),
        CITY("#ville"),
        FARM("#farm");

    private final String tag;

    WarpTag(String tag) {
        this.tag = tag;
    }

    public Predicate<Warp> hasTag() {
        return warp -> warp.getTags().contains(tag);
    }

    public String getTag() {
        return tag;
    }
}
