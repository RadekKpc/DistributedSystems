package database.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Satellite {

    @Id
    private Integer id;

    private Integer errors;


    public Satellite(Integer id, Integer errors) {
        this.id = id;
        this.errors = errors;
    }

    public Satellite() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getErrors() {
        return errors;
    }

    public void setErrors(Integer errors) {
        this.errors = errors;
    }
}
