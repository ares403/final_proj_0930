package vaccine;

import vaccine.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardViewHandler {


    @Autowired
    private DashboardRepository dashboardRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenVaccineReserved_then_CREATE_1 (@Payload VaccineReserved vaccineReserved) {
        try {

            if (!vaccineReserved.validate()) return;

            // view 객체 생성
            Dashboard dashboard = new Dashboard();
            // view 객체에 이벤트의 Value 를 set 함
            dashboard.setCustomerId(vaccineReserved.getId());
            dashboard.setCustomerName(vaccineReserved.getName());
            dashboard.setVaccineId(vaccineReserved.getVaccineId());
            // view 레파지 토리에 save
            dashboardRepository.save(dashboard);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenVaccineSucceeded_then_UPDATE_1(@Payload VaccineSucceeded vaccineSucceeded) {
        try {
            if (!vaccineSucceeded.validate()) return;
                // view 객체 조회

                    List<Dashboard> dashboardList = dashboardRepository.findByVaccineId(vaccineSucceeded.getId());
                    for(Dashboard dashboard : dashboardList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    dashboard.setReserveStatus("OK");
                // view 레파지 토리에 save
                dashboardRepository.save(dashboard);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenVaccineFailed_then_UPDATE_2(@Payload VaccineFailed vaccineFailed) {
        try {
            if (!vaccineFailed.validate()) return;
                // view 객체 조회

                    List<Dashboard> dashboardList = dashboardRepository.findByVaccineId(vaccineFailed.getId());
                    for(Dashboard dashboard : dashboardList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    dashboard.setReserveStatus("NO");
                // view 레파지 토리에 save
                dashboardRepository.save(dashboard);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenVaccineCanceled_then_DELETE_1(@Payload VaccineCanceled vaccineCanceled) {
        try {
            if (!vaccineCanceled.validate()) return;
            // view 레파지 토리에 삭제 쿼리
            dashboardRepository.deleteByCustomerId(vaccineCanceled.getId());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

