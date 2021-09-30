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
    @Autowired VaccineRepository vaccineRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverVaccineBacked_BackVaccine(@Payload VaccineBacked vaccineBacked){

        if(!vaccineBacked.validate()) return;

        System.out.println("\n\n##### listener BackVaccine : " + vaccineBacked.toJson() + "\n\n");

        // Sample Logic //
        Vaccine vaccine = vaccineRepository.findById(vaccineBacked.getVaccineId()).orElseThrow(null);
        vaccine.setQty(vaccine.getQty()+1);
        vaccine.setStatus("OK");
        vaccineRepository.save(vaccine);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverVaccineRequested_CheckVaccine(@Payload VaccineRequested vaccineRequested){

        if(!vaccineRequested.validate()) return;

        System.out.println("\n\n##### listener CheckVaccine : " + vaccineRequested.toJson() + "\n\n");

        // Sample Logic //
        Vaccine vaccine = vaccineRepository.findById(vaccineRequested.getVaccineId()).orElseThrow(null);
        vaccine.setQty(vaccine.getQty()-1);

        if(vaccine.getQty()>=0) {
            vaccine.setStatus("OK");
        }
        else{
            vaccine.setStatus("NO");
        }
        vaccineRepository.save(vaccine);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}