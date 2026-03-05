package vn.dichvuangia.management.repository;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import vn.dichvuangia.management.common.ProductType;
import vn.dichvuangia.management.entity.Product;
import vn.dichvuangia.management.entity.ProductSpec;
import vn.dichvuangia.management.entity.ProductSpecValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification động cho Product — hỗ trợ filter theo:
 *  - productType (MACHINE / FILTER)
 *  - brandId
 *  - specKey + specValue  (EAV filter — JOIN product_spec_values + product_specs)
 *  - isDeleted = false (luôn áp dụng)
 */
@RequiredArgsConstructor
public class ProductSpecification implements Specification<Product> {

    private final ProductType productType;
    private final Long brandId;
    private final String specKey;
    private final String specValue;

    @Override
    public Predicate toPredicate(Root<Product> root,
                                 CriteriaQuery<?> query,
                                 CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        // Luôn lọc soft-deleted
        predicates.add(cb.isFalse(root.get("isDeleted")));

        if (productType != null) {
            predicates.add(cb.equal(root.get("productType"), productType));
        }

        if (brandId != null) {
            predicates.add(cb.equal(root.get("brand").get("id"), brandId));
        }

        // EAV filter: JOIN product_spec_values → product_specs
        if (specKey != null && !specKey.isBlank() && specValue != null && !specValue.isBlank()) {
            query.distinct(true);  // DISTINCT để tránh duplicate khi JOIN nhiều spec

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ProductSpecValue> psvRoot = subquery.from(ProductSpecValue.class);
            Join<ProductSpecValue, ProductSpec> psJoin = psvRoot.join("spec");

            subquery.select(psvRoot.get("product").get("id"))
                    .where(
                            cb.equal(psJoin.get("specKey"), specKey),
                            cb.equal(psvRoot.get("specValue"), specValue)
                    );

            predicates.add(root.get("id").in(subquery));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
