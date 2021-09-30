package vaccine;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DashboardRepository extends CrudRepository<Dashboard, Long> {

    List<Dashboard> findByVaccineId(Long vaccineId);

        void deleteByCustomerId(Long customerId);
}