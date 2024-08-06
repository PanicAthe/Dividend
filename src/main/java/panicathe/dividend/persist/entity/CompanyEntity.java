package panicathe.dividend.persist.entity;

import lombok.*;
import panicathe.dividend.model.Company;

import javax.persistence.*;

@Entity(name = "COMPANY")
@Getter
@ToString
@NoArgsConstructor
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String ticker;

    private String name;

    public CompanyEntity(Company company) {

        this.ticker = company.getTicker();
        this.name = company.getName();
    }
}
