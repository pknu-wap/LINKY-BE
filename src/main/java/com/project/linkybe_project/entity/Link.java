package com.project.linkybe_project.entity;

import jakarta.persistence.*;
import lombok.Setter;

@Entity
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String url;
    private String title;
    private String siteName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public void assignUser(User user) {
        this.user = user;
    }

}