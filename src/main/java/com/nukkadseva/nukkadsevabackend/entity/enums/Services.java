package com.nukkadseva.nukkadsevabackend.entity.enums;

import lombok.Getter;

@Getter
public enum Services {
    PLUMBING("Plumbing"),
    CLEANING("Cleaning"),
    ELECTRICAL("Electrical"),
    PAINTING("Painting"),
    REPAIRS("Repairs"),
    APPLIANCE_REPAIRS("Appliance Repairs"),
    CARPENTRY("Carpentry"),
    COOKING_SERVICES("Cooking Services");

    private final String displayName;

    Services(String displayName) {
        this.displayName = displayName;
    }
}
