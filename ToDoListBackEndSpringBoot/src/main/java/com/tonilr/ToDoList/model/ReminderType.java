package com.tonilr.ToDoList.model;

public enum ReminderType {
    DUE_DATE("Recordatorio de fecha de vencimiento"),
    FOLLOW_UP("Recordatorio de seguimiento"),
    CUSTOM("Recordatorio personalizado");

    private final String description;

    ReminderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
