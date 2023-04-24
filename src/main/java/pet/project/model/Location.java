package pet.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "locations", uniqueConstraints = {@UniqueConstraint(columnNames = {"latitude", "longitude"})})
public class Location {
    @Id
    @SequenceGenerator(name = "locations_seq", sequenceName = "locations_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locations_seq")
    private Long id;

    private String name;

    @ManyToMany
    @JoinColumn(name = "user_id")
    private List<User> users;

    private Double latitude;

    private Double longitude;

    public Location(String name, List<User> users, Double latitude, Double longitude) {
        this.name = name;
        this.users = users;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
