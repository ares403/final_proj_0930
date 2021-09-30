package vaccine.external;

import org.springframework.stereotype.Service;

@Service
public class ReservationServiceImpl implements ReservationService{
    /**
     * Pay fallback
     * @return
     */
    public boolean reserve(Reservation reservation) {
        System.out.println("@@@@@@@ 예약중입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 예약중입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 예약중입니다. @@@@@@@@@@@@");
        return false;
    }
}
