package com.inspur.emmcloud.web.plugin.invoice;

import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.tencent.mm.opensdk.modelbiz.ChooseCardFromWXCardPackage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

public class InvoiceService extends ImpPlugin {
    @Override
    public void execute(String action, JSONObject paramsObject) {
        if (action.equals("open")) {
            initInvoice();
        }
    }

    private void initInvoice() {
        String appId = "wx4eb8727ea9c26495";
        IWXAPI api = WXAPIFactory.createWXAPI(getFragmentContext(), "wx4eb8727ea9c26495", true);
        api.registerApp("wx4eb8727ea9c26495");

        ChooseCardFromWXCardPackage.Req req = new ChooseCardFromWXCardPackage.Req();
        req.appId = appId;
        req.cardType = "INVOICE";
        req.cardSign = "";
        req.nonceStr = "";
        req.timeStamp = System.currentTimeMillis() + "";
        req.signType = "SHA256";

        if (req.checkArgs()) {
            api.sendReq(req);
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        return null;
    }

    @Override
    public void onDestroy() {

    }
}
