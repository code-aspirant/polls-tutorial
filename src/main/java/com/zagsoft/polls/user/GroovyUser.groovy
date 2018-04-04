package com.zagsoft.polls.user

import com.zagsoft.polls.role.Role
import org.hibernate.annotations.NaturalId

import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

//@Entity
@Table(name = "users", uniqueConstraints = [
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
])
class GroovyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id

    @NotBlank
    @Size(max = 40)
    private String name

    @NotBlank
    @Size(max = 15)
    private String username

    @NaturalId
    @NotBlank
    @Size(max = 40)
    @Email
    private String email

    @NotBlank
    @Size(max = 100)
    private String password

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>()
}
