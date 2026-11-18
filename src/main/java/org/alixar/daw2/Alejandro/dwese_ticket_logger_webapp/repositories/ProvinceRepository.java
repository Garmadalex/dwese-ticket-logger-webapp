package org.alixar.daw2.Alejandro.dwese_ticket_logger_webapp.repositories;

import org.alixar.daw2.Alejandro.dwese_ticket_logger_webapp.entities.Province;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProvinceRepository extends JpaRepository<Province, Long> {

    @Override
    @EntityGraph(attributePaths = "region")
    List<Province> findAll();

    boolean existsProvinceByCode(String code);

    @Query("SELECT COUNT(p) > 0 FROM Province p WHERE UPPER(p.code) = UPPER(:code) AND p.id != :id")
    boolean existsProvinceByCodeAndNotId(@Param("code") String code, @Param("id") Long id);
}