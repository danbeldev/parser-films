package ru.mtc.parser.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity(name = "films")
public class FilmEntity {

    @Id
    private Long id;

    private String name;
    private Float rating;
    private Integer ratingsCount;
    private Integer year;
    private Integer duration;
    private String posterUrl;
    private Boolean isAvailableOnline;

    @ManyToOne(fetch = FetchType.LAZY)
    private CountryEntity country;

    @ManyToOne(fetch = FetchType.LAZY)
    private GenreEntity genre;

    @ManyToOne(fetch = FetchType.LAZY)
    private PersonEntity director;

    @JoinTable(
            name = "film_actors",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    @ManyToMany(fetch = FetchType.LAZY)
    private List<PersonEntity> actors;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        FilmEntity film = (FilmEntity) o;
        return getId() != null && Objects.equals(getId(), film.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "rating = " + rating + ", " +
                "ratingsCount = " + ratingsCount + ", " +
                "year = " + year + ", " +
                "duration = " + duration + ", " +
                "posterUrl = " + posterUrl + ", " +
                "isAvailableOnline = " + isAvailableOnline + ")";
    }
}
