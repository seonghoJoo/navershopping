package com.sparta.springcore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor // 기본 생성자를 만들어줍니다.
public class Folder {

    @Id
    @GeneratedValue
    @Column(name="folder_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;


    public Folder(String name, User user) {
        this.user = user;
        this.name = name;
    }
}