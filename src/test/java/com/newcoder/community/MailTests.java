package com.newcoder.community;

import com.newcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        mailClient.sendMail("1150194271@qq.com","Two Messages For Patrick",
                "1. Ever tried. Ever failed. No matter. Try again. Fail again. Fail better. 2. Si vis pacem, para bellum\n");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","Patrick");

        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);

        mailClient.sendMail("1150194271@qq.com","TestHtmlMail",content);
    }
}
