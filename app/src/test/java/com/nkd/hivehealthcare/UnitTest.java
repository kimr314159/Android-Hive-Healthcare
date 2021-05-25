package com.nkd.hivehealthcare;
import androidx.appcompat.app.AppCompatActivity;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UnitTest extends AppCompatActivity {

    private MainActivity mainActivity;

    @Before
    public void setUp() throws Exception {
        mainActivity= new MainActivity();
        mainActivity.setCredentials();
        mainActivity.createSession();
    }

    @Test
    public void sendMessageTest1() throws Exception {
        assertEquals(mainActivity.sendMessage("What is HIV?"),  getResources().getString(R.string.testResult1));
    }

    @Test
    public void sendMessageTest2() throws Exception {
        assertEquals(mainActivity.sendMessage("What are symptoms of HIV?"),  getResources().getString(R.string.testResult2));
    }

    @Test
    public void sendMessageTest3() throws Exception {
        assertEquals(mainActivity.sendMessage("How can I test for HIV?"),  getResources().getString(R.string.testResult3));
    }

    @Test
    public void sendMessageTest4() throws Exception {
        assertEquals(mainActivity.sendMessage("How does HIV affect the immune system?"),  getResources().getString(R.string.testResult4));
    }
}