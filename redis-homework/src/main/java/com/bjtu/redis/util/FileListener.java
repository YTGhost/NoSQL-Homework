package com.bjtu.redis.util;

import com.bjtu.redis.Main;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;

public class FileListener extends FileAlterationListenerAdaptor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onFileChange(File file) {
        Main.lock.compareAndSet(false, true);
        Main.loadConfigJson();
        Main.lock.set(false);
    }
}
