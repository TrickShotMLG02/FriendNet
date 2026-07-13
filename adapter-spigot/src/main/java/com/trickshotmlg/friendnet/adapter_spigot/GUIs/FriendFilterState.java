package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

public class FriendFilterState {
    private boolean favoritesOnly;
    private boolean onlineOnly;
    private boolean sortByName;
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

    public boolean isSortByName() {
        return sortByName;
    }

    public void setSortByName(boolean sortByName) {
        this.sortByName = sortByName;
        if (sortByName) {
            sortByRecentlySeen = false;
        }
    }

    public boolean isSortByRecentlySeen() {
        return sortByRecentlySeen;
    }

    public void setSortByRecentlySeen(boolean sortByRecentlySeen) {
        this.sortByRecentlySeen = sortByRecentlySeen;
        if (sortByRecentlySeen) {
            sortByName = false;
        }
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
        sortByName = false;
        sortByRecentlySeen = false;
        reverseSort = false;
    }
}
