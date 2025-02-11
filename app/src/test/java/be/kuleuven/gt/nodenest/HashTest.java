package be.kuleuven.gt.nodenest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class HashTest {

    String password = "passwd";
    int hashedPassword = password.hashCode();

    @Test
    public void addition_isCorrect() {
        assertEquals(password.hashCode(), hashedPassword);
        assertNotEquals((password+"0ab").hashCode(), hashedPassword);
    }
}
