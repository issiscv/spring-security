package io.security.corespringsecurity.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {

    @Id @GeneratedValue
    @Column(name = "account_id")
    private Long Id;

    private String username;

    private String password;

    private String email;

    private String age;

    private String role;
}
