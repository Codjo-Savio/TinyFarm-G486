import java.io.Serializable;
import jakarta.persistence.Embeddable;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CooperativeId implements Serializable{
    private Long userId;
    private Long productId;

}