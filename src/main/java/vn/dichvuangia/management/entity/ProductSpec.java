package vn.dichvuangia.management.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "product_specs")
public class ProductSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spec_key", nullable = false, unique = true, length = 100)
    private String specKey;       // code key, VD: 'max_flow_rate'

    @Column(name = "spec_name", nullable = false)
    private String specName;      // display, VD: 'Lưu lượng tối đa'

    @Column(name = "unit", length = 50)
    private String unit;          // VD: 'gal/min', 'psi'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private SpecGroup group;

    @Column(name = "display_order")
    private Integer displayOrder;
}
