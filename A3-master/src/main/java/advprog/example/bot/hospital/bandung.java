package advprog.example.bot.bandung;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class Bandung implements Comparable<Bandung> {
    @Getter private String name;
    @Getter private String address;
    @Getter private double latitude;
    @Getter private double longitude;
    @Getter private String phone;
    @Getter private String imageLink;
    @Getter private String description;
    @Getter private String harga;
    @Getter @Setter private int distance = -1;


    @Override
    public int compareTo(@NotNull Bandung bandung) {
        return this.distance - hospital.distance;
    }
}
