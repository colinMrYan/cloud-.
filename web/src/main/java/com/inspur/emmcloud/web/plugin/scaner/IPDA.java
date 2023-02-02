package com.inspur.emmcloud.web.plugin.scaner;

public interface IPDA {

    void init();

    void registerScanHeaderReceiver();

    void unregisterScanHeaderReceiver();

    void destroy();

}
