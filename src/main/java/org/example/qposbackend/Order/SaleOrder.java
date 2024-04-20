package org.example.qposbackend.Order;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.qposbackend.Integrity.IntegrityAttributes;
import org.example.qposbackend.Order.OrderItem.OrderItem;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class SaleOrder extends IntegrityAttributes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn
    private List<OrderItem> orderItems;
    private double discount;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();
    private String modeOfPayment;
    private Double amountInCash;
    private Double amountInMpesa;
}
