package vaccine;

public class VaccineRequested extends AbstractEvent {

    private Long id;
    private Long vaccineId;

    public VaccineRequested(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
    }
}