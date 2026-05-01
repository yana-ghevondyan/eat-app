package com.example.yanagh.data;

/**
 * Gson model for persisting the diet screen day plan (online + optional local slots).
 */
public class DietDayPlan {
    public DietSlotEntry breakfast;
    public DietSlotEntry lunch;
    public DietSlotEntry dinner;
    public DietSlotEntry snack;

    public DietSlotEntry get(String slotId) {
        if (slotId == null) return null;
        switch (slotId) {
            case "breakfast":
                return breakfast;
            case "lunch":
                return lunch;
            case "dinner":
                return dinner;
            case "snack":
                return snack;
            default:
                return null;
        }
    }

    public void put(String slotId, DietSlotEntry e) {
        if (slotId == null || e == null) return;
        switch (slotId) {
            case "breakfast":
                breakfast = e;
                break;
            case "lunch":
                lunch = e;
                break;
            case "dinner":
                dinner = e;
                break;
            case "snack":
                snack = e;
                break;
            default:
                break;
        }
    }
}
