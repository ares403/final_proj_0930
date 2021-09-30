package vaccine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

 @RestController
 @RequestMapping("/reservation")
 public class ReservationController {

  @Autowired
  ReservationRepository reservationRepository;

  @PostMapping("/reserve")
  public boolean reserveVaccine(@RequestBody Reservation reservation){
   Reservation savedReservation = reservationRepository.save(reservation);
   return true;
  }
 }