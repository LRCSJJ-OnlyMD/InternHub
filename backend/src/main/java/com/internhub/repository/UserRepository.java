package com.internhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.internhub.model.Role;
import com.internhub.model.Sector;
import com.internhub.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleAndSectorsContaining(Role role, Sector sector);

    Optional<User> findByActivationToken(String activationToken);

    /**
     * Count users by role.
     */
    Long countByRole(Role role);
}
