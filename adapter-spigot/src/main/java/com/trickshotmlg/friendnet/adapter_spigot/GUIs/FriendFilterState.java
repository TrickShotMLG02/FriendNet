package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

public class FriendFilterState {
    private boolean favoritesOnly;
    private boolean onlineOnly;
    private String nameSearchQuery = "";
    private boolean sortByRecentlySeen;
    private boolean reverseSort;

    public boolean isFavoritesOnly() {
        return favoritesOnly;
    }

    public void setFavoritesOnly(boolean favoritesOnly) {
        this.favoritesOnly = favoritesOnly;
    }

    public boolean isOnlineOnly() {
        return onlineOnly;
    }

    public void setOnlineOnly(boolean onlineOnly) {
        this.onlineOnly = onlineOnly;
    }

    public String getNameSearchQuery() {
        return nameSearchQuery;
    }

    public void setNameSearchQuery(String nameSearchQuery) {
        this.nameSearchQuery = nameSearchQuery == null ? "" : nameSearchQuery.trim();
    }

    public boolean hasNameSearchQuery() {
        return !nameSearchQuery.isBlank();
    }

    public boolean isActive() {
        return isActive(true);
    }

    public boolean isActive(boolean includeFavoritesOnly) {
        return (includeFavoritesOnly && favoritesOnly)
                || onlineOnly
                || hasNameSearchQuery()
                || sortByRecentlySeen
                || reverseSort;
    }

    public void clearNameSearchQuery() {
        nameSearchQuery = "";
    }

    public boolean isSortByRecentlySeen() {
        return sortByRecentlySeen;
    }

    public void setSortByRecentlySeen(boolean sortByRecentlySeen) {
        this.sortByRecentlySeen = sortByRecentlySeen;
    }

    public boolean isReverseSort() {
        return reverseSort;
    }

    public void setReverseSort(boolean reverseSort) {
        this.reverseSort = reverseSort;
    }

    public void reset() {
        favoritesOnly = false;
        onlineOnly = false;
        nameSearchQuery = "";
        sortByRecentlySeen = false;
        reverseSort = false;
    }
}
