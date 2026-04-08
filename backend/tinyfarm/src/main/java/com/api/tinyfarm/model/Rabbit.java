package com.api.tinyfarm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rabbit")
@PrimaryKeyJoinColumn(name = "aid")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rabbit extends Animal {

    public enum RabbitTypeEnum {
        lapereau,
        lapin,
    }

    @NotNull(message = "The name is obligatory")
    @Column(name = "name", length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "rabbit_type", columnDefinition = "rabbittypeenum")
    private RabbitTypeEnum rabbitType;

    @PrePersist
    @Override
    public void prePersist() {
        super.prePersist();
        if (this.rabbitType == null) {
            this.rabbitType = RabbitTypeEnum.lapereau;
        }
    }
}
