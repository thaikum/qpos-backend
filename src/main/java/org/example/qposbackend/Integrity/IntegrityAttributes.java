package org.example.qposbackend.Integrity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.qposbackend.Authorization.User.User;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@MappedSuperclass
@Data
@EntityListeners(AuditingEntityListener.class)
public class IntegrityAttributes {
    @CreatedBy
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "created_by_id")
    private User createdBy;
    @LastModifiedBy
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "last_modified_by_id")
    private User lastModifiedBy;
    @CreationTimestamp
    private Date creationTimestamp;
    @LastModifiedDate
    private Date lastModificationDate;
}
