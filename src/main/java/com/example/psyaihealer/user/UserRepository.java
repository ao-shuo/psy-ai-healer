package com.example.psyaihealer.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    List<User> findByEnabledFalse();

    boolean existsByRole(Role role);

    long countByRoleAndEnabledTrue(Role role);

    @Query("select u from User u where " +
            "lower(u.username) like lower(concat('%', :q, '%')) or " +
            "lower(coalesce(u.fullName, '')) like lower(concat('%', :q, '%')) or " +
            "lower(coalesce(u.email, '')) like lower(concat('%', :q, '%'))")
    List<User> search(@Param("q") String q);
}
