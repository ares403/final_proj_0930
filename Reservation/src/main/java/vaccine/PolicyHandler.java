package vaccine;

import vaccine.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverVaccineCanceled_Cancel(@Payload VaccineCanceled vaccineCanceled){

        if(!vaccineCanceled.validate()) return;

        System.out.println("\n\n##### listener Cancel : " + vaccineCanceled.toJson() + "\n\n");

        // Sample Logic //
        Reservation reservation = reservationRepository.findByCustomerId(vaccineCanceled.getId());
        reservation.setCustomerId(vaccineCanceled.getId());
        reservation.setVaccineId(vaccineCanceled.getVaccineId());
        reservation.setReserveStatus("NO");
        reservation.setCustomerName(vaccineCanceled.getName());

        reservationRepository.save(reservation);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}