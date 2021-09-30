package vaccine.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="reservation", url="${api.url.reservation}", fallback = ReservationServiceImpl.class)
public interface ReservationService {
    @RequestMapping(method= RequestMethod.POST, path="/reserve")
    public boolean reserve(@RequestBody Reservation reservation);

}

