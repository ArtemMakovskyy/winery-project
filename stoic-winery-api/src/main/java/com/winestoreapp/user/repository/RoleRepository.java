package com.winestoreapp.user.repository;

import com.winestoreapp.user.model.Role;
import com.winestoreapp.user.model.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import io.micrometer.observation.annotation.Observed;

@Observed
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleName);
}
