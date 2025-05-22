package org.example.qposbackend.Item.ItemClassification.Category;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Item.ItemClassification.MainCategory.MainCategory;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_categoryName_mainCategoryId", columnNames = {"category_name", "main_category_id"})
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String categoryName;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "main_category_id", nullable = false)
    @JsonIgnore
    private MainCategory mainCategory;
}
