package com.sap.bulletinboard.ads.models;

import javax.persistence.*;

import org.hibernate.validator.constraints.NotBlank;

import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "advertisements")
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "mytitle")
    @NotBlank
    private String title;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    @Version
    private long version;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @PrePersist
    public void creationTimestamp() {
        createdAt = now();
    }

    @PreUpdate
    public void updatingTimestamp() {
        updatedAt = now();
    }

    protected Timestamp now() {                       // use java.sql.Timestamp
        return new Timestamp((new Date()).getTime()); // use java.util.Date
    }
}
