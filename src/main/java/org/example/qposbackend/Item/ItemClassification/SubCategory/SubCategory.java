package org.example.qposbackend.Item.ItemClassification.SubCategory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Item.ItemClassification.Category.Category;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_subCategory_category", columnNames = {"sub_category", "category_id"})
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String subCategoryName;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
