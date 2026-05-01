package com.example.yanagh.data;

/**
 * One meal slot: either a local catalog recipe id or a full remote recipe from TheMealDB.
 * {@code categoryHint} is TheMealDB category name used when swapping meals.
 */
public class DietSlotEntry {
    public String kind;
    public String localId;
    public RemoteRecipe remote;
    public String categoryHint;
}
