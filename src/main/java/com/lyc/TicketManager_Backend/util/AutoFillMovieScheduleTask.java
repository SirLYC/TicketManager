package com.lyc.TicketManager_Backend.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class AutoFillMovieScheduleTask implements CommandLineRunner {

    @Resource
    private FakeDataUtil fakeDataUtil;


    @Scheduled(cron = "0 0 0 1/1 * ? ")
    public void doEveryDay() {
        fakeDataUtil.fillMovieAfter2Days();
    }

    @Override
    public void run(String... args) throws Exception {
        String filePath = null;
        if (args != null && args.length > 0) {
            filePath = args[0];
        }
        fakeDataUtil.runFirstTime(filePath);
    }
}
