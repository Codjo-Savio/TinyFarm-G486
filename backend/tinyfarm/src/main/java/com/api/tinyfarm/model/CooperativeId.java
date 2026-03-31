import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CooperativeId implements Serializable{
    @Column(name = "uid")
    private Long userId;
    @Column(name = "productID")
    private Long productId;

}