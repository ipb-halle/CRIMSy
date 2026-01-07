package de.ipb_halle.lbac.entity;

import java.lang.reflect.Member;
import java.time.LocalDateTime;

import de.ipb_halle.lbac.admission.MemberEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "user_sessions", schema = "lbac")
public class UserSessionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private MemberEntity user;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "last_seen", nullable = false)
    private LocalDateTime lastSeen;

    public UserSessionsEntity() {
        this.lastSeen = LocalDateTime.now();
    }

    public UserSessionsEntity(MemberEntity user, String token) {
        this.user = user;
        this.token = token;
        this.lastSeen = LocalDateTime.now();
    }

    // getters and setters
    public Integer getId() {
        return id;
    }

    public MemberEntity getUser() {
        return user;
    }

    public void setUser(MemberEntity user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

}
