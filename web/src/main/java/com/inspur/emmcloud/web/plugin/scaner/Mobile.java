package com.inspur.emmcloud.web.plugin.scaner;

public class Mobile implements IPDA {
    private final PDAService mPDAService;

    public Mobile(PDAService pdaService){
        mPDAService = pdaService;
    }

    @Override
    public void init() {
        mPDAService.callJsError("android mobile not support");
    }

    @Override
    public void registerScanHeaderReceiver() {
        mPDAService.callJsError("android mobile not support");
    }

    @Override
    public void unregisterScanHeaderReceiver() {
        mPDAService.callJsError("android mobile not support");
    }

    @Override
    public void destroy() {

    }
}
