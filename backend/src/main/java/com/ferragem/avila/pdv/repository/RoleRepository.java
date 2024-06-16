package com.ferragem.avila.pdv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ferragem.avila.pdv.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByNameIgnoreCase(String name);
}