package net.phoenixvine.essentials.api;

public enum EssentialsFeatureState {

    DISABLED,
    VISIBLE,
    ENABLED;

    public boolean atLeast(EssentialsFeatureState minimum) {
        return ordinal() >= minimum.ordinal();
    }
}
