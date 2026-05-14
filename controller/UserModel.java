package com.veloura.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserModel {

    private final IntegerProperty userId;
    private final StringProperty username;
    private final StringProperty role;
    private final BooleanProperty active;
    private final BooleanProperty resetRequested;

    public UserModel(int userId, String username, String role, boolean active, boolean resetRequested) {
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.role = new SimpleStringProperty(role);
        this.active = new SimpleBooleanProperty(active);
        this.resetRequested = new SimpleBooleanProperty(resetRequested);
    }

    public int getUserId() {
        return userId.get();
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public String getRole() {
        return role.get();
    }

    public StringProperty roleProperty() {
        return role;
    }

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public boolean isResetRequested() {
        return resetRequested.get();
    }

    public BooleanProperty resetRequestedProperty() {
        return resetRequested;
    }

    public String getActiveText() {
        return isActive() ? "Active" : "Inactive";
    }

    public String getResetRequestText() {
        return isResetRequested() ? "Pending" : "None";
    }
}