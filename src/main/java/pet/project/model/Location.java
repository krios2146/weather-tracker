package pet.project.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany
    @JoinColumn(name = "user_id")
    private List<User> userId;

    private Double latitude;

    private Double longitude;

    public Location(Long id, String name, List<User> userId, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.userId = userId;
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

    public List<User> getUserId() {
        return userId;
    }

    public void setUserId(List<User> userId) {
        this.userId = userId;
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