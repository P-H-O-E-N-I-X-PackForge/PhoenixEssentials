package net.phoenixvine.essentials.data;

import java.util.ArrayList;
import java.util.List;

public class KitDefinition {

    public String name = "";
    public List<KitItemEntry> items = new ArrayList<>();
    public int cooldownSeconds = 0;

    public KitDefinition() {}

    public KitDefinition(String name) {
        this.name = name;
    }
}
