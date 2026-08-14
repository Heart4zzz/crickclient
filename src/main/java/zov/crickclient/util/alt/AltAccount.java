package zov.crickclient.util.alt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AltAccount {
    private String name;
    private boolean favorite;

    public AltAccount(String name) {
        this.name = name;
    }
}
