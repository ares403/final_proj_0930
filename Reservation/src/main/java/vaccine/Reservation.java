package vaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String reserveStatus;
    private Long customerId;
    private Long vaccineId;
    private String customerName;

    @PostPersist
    public void onPostPersist() {
        if (this.reserveStatus.equals("OK")) {
            VaccineRequested vaccineRequested = new VaccineRequested();
            BeanUtils.copyProperties(this, vaccineRequested);
            vaccineRequested.publishAfterCommit();

            vaccineRequested.saveJasonToPvc(vaccineRequested.toJson());
        }
    }

    @PostUpdate
    public void onPostUpdate(){
        if (this.reserveStatus.equals("NO")) {
            VaccineBacked vaccineBacked = new VaccineBacked();
            BeanUtils.copyProperties(this, vaccineBacked);
            vaccineBacked.publishAfterCommit();

            vaccineBacked.saveJasonToPvc(vaccineBacked.toJson());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getReserveStatus() {
        return reserveStatus;
    }

    public void setReserveStatus(String reserveStatus) {
        this.reserveStatus = reserveStatus;
    }
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public Long getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
    }
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }




}