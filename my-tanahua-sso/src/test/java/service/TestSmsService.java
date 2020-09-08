package service;

import com.tanhua.sso.MyApplication;
import com.tanhua.sso.service.SmsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyApplication.class)
public class TestSmsService {

    @Autowired
    private SmsService smsService;

    @Test
    public void testSend(){
        String code = this.smsService.sendAliSms("18023194448");
        System.out.println(code);
    }

}
