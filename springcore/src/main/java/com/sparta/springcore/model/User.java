package com.sparta.springcore.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter // get 함수를 일괄적으로 만들어줍니다.
@NoArgsConstructor // 기본 생성자를 만들어줍니다.
@Entity // DB 테이블 역할을 합니다.
public class User {

    // ID가 자동으로 생성 및 증가합니다.
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    // nullable: null 허용 여부
// unique: 중복 허용 여부 (false 일때 중복 허용)
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    // 같은 kakaoId 사용자를 허용해 주지 않는다.
    @Column(nullable = true, unique = true)
    private Long kakaoId;

    // 일반 Form 로그인 사용자
    public User(String username, String password, String email, UserRoleEnum role, String nickname) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.nickname = nickname;
        this.kakaoId = null;
    }

    // 카카오 로그인 사용자
    public User(String nickname, String password, String email, UserRoleEnum role, Long kakaoId) {
        this.username = nickname;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.role = role;
        this.kakaoId = kakaoId;
    }
}