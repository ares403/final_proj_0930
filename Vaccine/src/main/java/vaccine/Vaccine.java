package vaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Vaccine_table")
public class Vaccine {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String name;
    private Long qty;
    private String status;

    @PostUpdate
    public void onPostUpdate(){
        if(this.status.equals("OK")) {
            VaccineSucceeded vaccineSucceeded = new VaccineSucceeded();
            BeanUtils.copyProperties(this, vaccineSucceeded);
            vaccineSucceeded.publishAfterCommit();
        }

        if(this.status.equals("OK")) {
            Vaccinefailed vaccinefailed = new Vaccinefailed();
            BeanUtils.copyProperties(this, vaccinefailed);
            vaccinefailed.publishAfterCommit();
        }
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
    public Long getQty() {
        return qty;
    }

    public void setQty(Long qty) {
        this.qty = qty;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}