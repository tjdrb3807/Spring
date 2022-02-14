package jpql;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue
    private Long id;
    private String name;
    private int price;
    private int stockAmount;


}
