package org.alixar.daw2.Alejandro.dwese_ticket_logger_webapp.dao;

import java.util.List;
import org.alixar.daw2.Alejandro.dwese_ticket_logger_webapp.entities.Province;
public interface ProvinceDAO {
    List<Province> listAllProvinces() ;
    void insertProvince(Province province);
    void updateProvince(Province province);
    void deleteProvince(long id);
    Province getProvinceById(long id);
    boolean existsProvinceByCode(String code);
    boolean existsProvinceByCodeAndNotId(String code, long id);
}
