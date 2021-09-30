package vaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Customer_table")
public class Customer {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String name;
    private Long vaccineId;
    private String vaccineName;
    private String reserveStatus;

    @PostPersist
    public void onPostPersist(){

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        vaccine.external.Reservation reservation = new vaccine.external.Reservation();
        // mappings goes here
        reservation.setCustomerId(this.id);
        reservation.setCustomerName(this.name);
        //reservation.setReserveStatus("OK");
        reservation.setVaccineId(this.vaccineId);

        boolean result = CustomerApplication.applicationContext.getBean(vaccine.external.ReservationService.class)
            .reserve(reservation);

        if(result) {
            System.out.println("########## 예약성공 ############");
        } else {
            System.out.println("########## 예약실패 ############");
        }

        VaccineReserved vaccineReserved = new VaccineReserved();
        BeanUtils.copyProperties(this, vaccineReserved);
        vaccineReserved.publishAfterCommit();

        vaccineReserved.saveJasonToPvc(vaccineReserved.toJson());

    }

    @PostUpdate
    public void onPostUpdate() {
        if (this.reserveStatus.equals("NO")) {
            VaccineCanceled vaccineCanceled = new VaccineCanceled();
            BeanUtils.copyProperties(this, vaccineCanceled);
            vaccineCanceled.publishAfterCommit();

            vaccineCanceled.saveJasonToPvc(vaccineCanceled.toJson());
        }
    }

    /*
    @PostRemove
    public void onPostRemove(){
        VaccineCanceled vaccineCanceled = new VaccineCanceled();
        BeanUtils.copyProperties(this, vaccineCanceled);
        vaccineCanceled.publishAfterCommit();

    }
    */

    @PrePersist
    public void onPrePersist(){
    }
    @PreRemove
    public void onPreRemove(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Long getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
    }
    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }
    public String getReserveStatus() {
        return reserveStatus;
    }

    public void setReserveStatus(String reserveStatus) {
        this.reserveStatus = reserveStatus;
    }




}