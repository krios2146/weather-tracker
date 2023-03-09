package pet.project.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "locations")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (!id.equals(location.id)) return false;
        if (!name.equals(location.name)) return false;
        if (!Objects.equals(users, location.users)) return false;
        if (!latitude.equals(location.latitude)) return false;
        return longitude.equals(location.longitude);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (users != null ? users.hashCode() : 0);
        result = 31 * result + latitude.hashCode();
        result = 31 * result + longitude.hashCode();
        return result;
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
