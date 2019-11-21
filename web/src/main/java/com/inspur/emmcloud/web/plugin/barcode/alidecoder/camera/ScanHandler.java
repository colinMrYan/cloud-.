package com.inspur.emmcloud.web.plugin.barcode.alidecoder.camera;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;

import com.alipay.mobile.bqcscanservice.BQCScanEngine;
import com.alipay.mobile.bqcscanservice.BQCScanService;
import com.alipay.mobile.mascanengine.impl.MaScanEngineImpl;
import com.inspur.emmcloud.web.R;

/**
 * author : leilei.yll
 * date : 8/30/16.
 * email : leilei.yll@alibaba-inc.com
 * 旺旺 : 病已
 */
public class ScanHandler {

    private HandlerThread scanHandlerThread;
    private Handler scanHandler;
    private Context context;
    private ScanResultCallbackProducer scanResultCallbackProducer;
    private MediaPlayer shootMP;
    private BQCScanService bqcScanService;

    public ScanHandler() {
        scanHandlerThread = new HandlerThread("Scan-Recognized", Thread.MAX_PRIORITY);
        scanHandlerThread.start();
        scanHandler = new Handler(scanHandlerThread.getLooper());
    }

    public void destroy() {
        scanHandlerThread.quit();
    }

    public void setBqcScanService(final BQCScanService bqcScanService) {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                ScanHandler.this.bqcScanService = bqcScanService;
            }
        });
    }

    public void registerAllEngine() {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                if (scanResultCallbackProducer == null) {
                    return;
                }

                bqcScanService.regScanEngine(ScanType.SCAN_MA.toBqcScanType(), MaScanEngineImpl.class,
                        scanResultCallbackProducer.makeScanResultCallback(ScanType.SCAN_MA));
            }
        });
    }

    public void enableScan() {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                bqcScanService.setScanEnable(true);
            }
        });
    }

    public void setScanType(final ScanType mScanType) {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                bqcScanService.setScanType(mScanType.toBqcScanType());
            }
        });
    }

    public void disableScan() {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                bqcScanService.setScanEnable(false);
            }
        });
    }

    public void shootSound() {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                if (context == null) {
                    return;
                }
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    int volume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                    if (volume != 0) {
                        if (shootMP == null) {
                            shootMP = MediaPlayer.create(context, R.raw.web_qrcode_beep);
                        }
                        if (shootMP != null) {
                            shootMP.start();
                        }
                    }
                }
            }
        });
    }

    public void removeContext() {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                context = null;
                scanResultCallbackProducer = null;
                if (shootMP != null) {
                    shootMP.release();
                    shootMP = null;
                }
            }
        });
    }

    public void setContext(final Context context, final ScanResultCallbackProducer scanResultCallbackProducer) {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                ScanHandler.this.context = context;
                ScanHandler.this.scanResultCallbackProducer = scanResultCallbackProducer;
            }
        });
    }

    public interface ScanResultCallbackProducer {
        BQCScanEngine.EngineCallback makeScanResultCallback(ScanType type);
    }
}
