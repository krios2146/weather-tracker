package pet.project.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locations_seq")
    @SequenceGenerator(name = "locations_seq", sequenceName = "locations_seq")
    private Long id;

    private String name;

    @OneToMany
    @JoinColumn(name = "user_id")
    private List<User> users;

    private Double latitude;

    private Double longitude;

    public Location(Long id, String name, List<User> users, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.users = users;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(String name, List<User> users, Double latitude, Double longitude) {
        this.name = name;
        this.users = users;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
