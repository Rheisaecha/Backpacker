package advprog.example.bot.hospital;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class Hospital implements Comparable<Hospital> {
    @Getter private String name;
    @Getter private String address;
    @Getter private double latitude;
    @Getter private double longitude;
    @Getter private String phone;
    @Getter private String imageLink;
    @Getter private String description;
    @Getter @Setter private int distance = -1;


    @Override
    public int compareTo(@NotNull Hospital hospital) {
        return this.distance - hospital.distance;
    }
}
