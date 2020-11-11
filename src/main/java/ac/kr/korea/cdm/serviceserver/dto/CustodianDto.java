package ac.kr.korea.cdm.serviceserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CustodianDto {
    private int id;
    private String name;
    private String password;


    public boolean check() {
        if (name != null && password != null) {
            if (name.trim().equals("") || password.trim().equals("")) return false;
            else return true;
        } else return false;
    }
}
