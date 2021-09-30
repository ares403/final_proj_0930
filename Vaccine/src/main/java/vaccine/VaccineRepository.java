package vaccine;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="vaccines", path="vaccines")
public interface VaccineRepository extends PagingAndSortingRepository<Vaccine, Long>{


}
