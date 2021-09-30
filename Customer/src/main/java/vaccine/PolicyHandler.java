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
    @Autowired CustomerRepository customerRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverVaccineSucceeded_VaccineOk(@Payload VaccineSucceeded vaccineSucceeded){

        if(!vaccineSucceeded.validate()) return;

        System.out.println("\n\n##### listener VaccineOk : " + vaccineSucceeded.toJson() + "\n\n");

        Customer customer = customerRepository.findByVaccineId(vaccineSucceeded.getId());
        customer.setVaccineId(vaccineSucceeded.getId());
        customer.setVaccineName(vaccineSucceeded.getName());
        customer.setReserveStatus("OK");

        customerRepository.save(customer);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverVaccinefailed_VaccineNo(@Payload Vaccinefailed vaccinefailed){

        if(!vaccinefailed.validate()) return;

        System.out.println("\n\n##### listener VaccineNo : " + vaccinefailed.toJson() + "\n\n");

        Customer customer = customerRepository.findByVaccineId(vaccinefailed.getId());
        customer.setVaccineId(vaccinefailed.getId());
        customer.setVaccineName(vaccinefailed.getName());
        customer.setReserveStatus("NO");

        customerRepository.save(customer);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}