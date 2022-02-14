package com.inspur.emmcloud.servcieimpl;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.componentservice.download.ProgressCallback;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.componentservice.volume.VolumeService;
import com.inspur.emmcloud.componentservice.web.WebMediaCallback;
import com.inspur.emmcloud.componentservice.web.WebMediaCallbackImpl;
import com.inspur.emmcloud.componentservice.web.WebMediaService;
import com.inspur.emmcloud.util.privates.MediaPlayerManagerUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationManager;
import com.inspur.emmcloud.util.privates.audioformat.AndroidMp3ConvertUtils;
import com.inspur.emmcloud.widget.audiorecord.AudioRecordErrorCode;
import com.inspur.emmcloud.widget.audiorecord.AudioRecorderManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;

import lbc.com.denosex.denosexUtil;

public class MediaServiceImpl implements WebMediaService {
    private android.media.AudioManager audioManager;
    private AudioRecorderManager audioRecorderManager;

    @Override
    public void startAudioRecord() {
        if (VoiceCommunicationManager.getInstance().isVoiceBusy()) {
            ToastUtils.show(R.string.voice_communication_can_not_use_this_feature);
            return;
        }
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(BaseApplication.getInstance(), Permissions.RECORD_AUDIO, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                if (audioManager == null) {
                    audioManager = (android.media.AudioManager) BaseApplication.getInstance().getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                }
                audioManager.requestAudioFocus(null, android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                if (audioRecorderManager == null) {
                    audioRecorderManager = AudioRecorderManager.getInstance();
                    audioRecorderManager.setCallBack(new AudioRecorderManager.AudioDataCallBack() {
                        @Override
                        public void onDataChange(int volume, float duration) {
                        }

                        @Override
                        public void onWavAudioPrepareState(int state) {
                            switch (state) {
                                case AudioRecordErrorCode.SUCCESS:
                                    if (audioRecorderManager != null) {
                                        audioRecorderManager.startRecord();
                                    }
                                    break;
                                case AudioRecordErrorCode.E_NOSDCARD:
                                    ToastUtils.show(MyApplication.getInstance(), MyApplication.getInstance().getString(R.string.error_no_sdcard));
                                    break;
                                case AudioRecordErrorCode.E_ERROR:
                                default:
                                    break;
                            }
                        }
                    });
                }
                audioRecorderManager.stopRecord();
                audioRecorderManager.prepareWavAudioRecord();
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(BaseApplication.getInstance(), PermissionRequestManagerUtils.getInstance().getPermissionToast(BaseApplication.getInstance(), permissions));
            }
        });
    }

    @Override
    public void stopAudioRecord(WebMediaCallbackImpl callback) {
        if (audioRecorderManager == null || audioManager == null) return;
        audioManager.abandonAudioFocus(null);
        audioRecorderManager.stopRecord();
        getRecordAudioResultFile(audioRecorderManager.getCurrentFilePath(), callback);
    }

    @Override
    public void playAudio(final String fileResourcePath) {
        String sourceName;
        if (!fileResourcePath.startsWith("http")) return;
        sourceName = fileResourcePath.substring(fileResourcePath.lastIndexOf('/') + 1).replace(".mp3", ".wav");
        final String localSourcePath = getWavFilePath() + "11122.wav";
        if (MediaPlayerManagerUtils.getManager().isPlaying(localSourcePath)) {
            MediaPlayerManagerUtils.getManager().stop();
            return;
        }
        if (MediaPlayerManagerUtils.getManager().isPlaying()) {
            MediaPlayerManagerUtils.getManager().stop();
        }
        if (!FileUtils.isFileExist(localSourcePath)) {
            new DownLoaderUtils().startDownLoad(fileResourcePath, localSourcePath, new APIDownloadCallBack(fileResourcePath) {

                @Override
                public void callbackSuccess(File file) {
                    if (!MediaPlayerManagerUtils.getManager().isPlaying()) {
                        MediaPlayerManagerUtils.getManager().play(localSourcePath, null);
                    }
                }

                @Override
                public void callbackError(Throwable arg0, boolean arg1) {
                    ToastUtils.show(MyApplication.getInstance(), R.string.play_fail);
                }

                @Override
                public void onCancelled(CancelledException e) {
                }
            });
        } else {
            MediaPlayerManagerUtils.getManager().play(localSourcePath, null);
        }
    }

    @Override
    public void stopAudio(String path) {
        if (MediaPlayerManagerUtils.getManager().isPlaying(path)) {
            MediaPlayerManagerUtils.getManager().stop();
        }
    }

    @Override
    public void uploadAudioFile(String uploadPath, String sourcePath, WebMediaCallbackImpl callback) {
        File file = new File(sourcePath);
        if (!file.exists()) {
            callback.onFail();
            return;
        }
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            uploadFileToWebVolume(uploadPath, file, callback);
        }
    }

    /**
     * 上传文件
     *
     * @param fileUploadPath
     * @param sourceFile
     * @param callback
     */
    private void uploadFileToWebVolume(String fileUploadPath, File sourceFile, ProgressCallback callback) {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            VolumeFile mockVolumeFile = VolumeFile.getMockVolumeFile(sourceFile, "f55dbc534197d62aa35edb2ddde7c4e66601abbab82372c3d9bebd99864ba9fe");
            Router router = Router.getInstance();
            if (router.getService(VolumeService.class) != null) {
                VolumeService service = router.getService(VolumeService.class);
                service.uploadFile(mockVolumeFile, sourceFile.getPath(), callback);
            }
        }
    }

    private void getRecordAudioResultFile(final String filePathRaw, final WebMediaCallbackImpl callback) {
        final String[] convertPath = new String[1];
        /**文件操作**/
        if (FileUtils.getFileSize(filePathRaw) > 0) {
            try {
                String BasePath = MyAppConfig.LOCAL_CACHE_VOICE_PATH + "/";
                String id = UUID.randomUUID().toString();
                String namePcm = id + ".pcm";
                String filePcmNew = BasePath + namePcm;
                voiceAgc(filePathRaw, filePcmNew);
                deNoseX(filePathRaw, filePcmNew);
                if ((FileUtils.getFileSize(filePathRaw) <= 0)) {
                    if (callback != null) {
                        callback.onFail();
                    }
                    return;
                } else {
                    File tempFile = new File(filePcmNew);
                    String wavName = filePathRaw.replace(".raw", ".wav");
                    convertPcmToWav(filePathRaw, wavName, 16000, 1, 16);
                    tempFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AndroidMp3ConvertUtils.with(BaseApplication.getInstance()).setCallBack(new AndroidMp3ConvertUtils.AndroidMp3ConvertCallback() {
            @Override
            public void onSuccess(String mp3FilePath) {
                convertPath[0] = mp3FilePath;
                if (callback != null) {
                    callback.onRecordEnd(mp3FilePath);
                }
            }

            @Override
            public void onFailure(Exception e) {
                convertPath[0] = filePathRaw;
                if (callback != null) {
                    callback.onFail();
                }
            }
        }).setRawPathAndMp3Path(filePathRaw, filePathRaw.replace(".raw", ".mp3")).startConvert();
    }

    /**
     * 语音增强算法（原始数据文件pcm）
     **/
    private void voiceAgc(String rawAudioFilePath, String pcmAudioFilePath) {
        int createStatus = -1;
        denosexUtil agcUtils = null;
        try {
            agcUtils = new denosexUtil();
            createStatus = agcUtils.noseAgcCreate();
            File fileRaw = new File(rawAudioFilePath);
            File filePcm = new File(pcmAudioFilePath);
            if (!fileRaw.exists()) {
                return; //如果不存在 退出
            }
            FileInputStream fInt = new FileInputStream(rawAudioFilePath);
            FileOutputStream fOut = new FileOutputStream(pcmAudioFilePath);
            byte[] buffer = new byte[320];
            while (fInt.read(buffer) != -1) {
                short[] inputData = new short[160];
                short[] outData = new short[160];
                ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(inputData);
                outData = agcUtils.noseAgcProcess(createStatus, inputData, 160);
                fOut.write(toByteArray(outData));
            }
            fInt.close();
            fOut.close();
            fileRaw.delete();
            File fileNew = new File(rawAudioFilePath);
            filePcm.renameTo(fileNew);
            LogUtils.LbcDebug("new File Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (createStatus == 0 && agcUtils != null) {
            agcUtils.noseAgcFree();
        }
    }

    /**
     * 语音降噪算法 pcm文件
     **/
    private void deNoseX(String rawAudioFilePath, String pcmAudioFilePath) {
        int createStatus = -1;
        denosexUtil nsUtils = null;
        try {
            nsUtils = new denosexUtil();
            createStatus = nsUtils.denoseXCreate();  //去噪创建
            File fileRaw = new File(rawAudioFilePath);
            File filePcm = new File(pcmAudioFilePath);
            if (!fileRaw.exists()) {
                return; //如果不存在 退出
            }
            FileInputStream fInt = new FileInputStream(rawAudioFilePath);
            FileOutputStream fOut = new FileOutputStream(pcmAudioFilePath);
            byte[] buffer = new byte[640];
            while (fInt.read(buffer) != -1) {
                short[] inputData = new short[320];
                short[] outData = new short[320];
                ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(inputData);
                outData = nsUtils.denoseXProcess(createStatus, inputData);
                fOut.write(toByteArray(outData));
            }

            fInt.close();
            fOut.close();
            fileRaw.delete();
            File fileNew = new File(rawAudioFilePath);
            filePcm.renameTo(fileNew);
            LogUtils.LbcDebug("new File Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (createStatus == 0 && nsUtils != null) {
            nsUtils.denoseXFree();
        }
    }

    private byte[] toByteArray(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i]);
            dest[i * 2 + 1] = (byte) (src[i] >> 8);
        }
        return dest;
    }

    /**
     * PCM文件转WAV文件
     *
     * @param inPcmFilePath  输入PCM文件路径
     * @param outWavFilePath 输出WAV文件路径
     * @param sampleRate     采样率，例如15000
     * @param channels       声道数 单声道：1或双声道：2
     * @param bitNum         采样位数，8或16
     */
    public void convertPcmToWav(String inPcmFilePath, String outWavFilePath, int sampleRate,
                                int channels, int bitNum) {
        FileInputStream in = null;
        FileOutputStream out = null;
        byte[] data = new byte[1024];

        try {
            //采样字节byte率
            long byteRate = sampleRate * channels * bitNum / 8;

            in = new FileInputStream(inPcmFilePath);
            out = new FileOutputStream(outWavFilePath);

            //PCM文件大小
            long totalAudioLen = in.getChannel().size();

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate);

            int length = 0;
            while ((length = in.read(data)) > 0) {
                out.write(data, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 输出WAV文件
     *
     * @param out           WAV输出文件流
     * @param totalAudioLen 整个音频PCM数据大小
     * @param totalDataLen  整个数据大小
     * @param sampleRate    采样率
     * @param channels      声道数
     * @param byteRate      采样字节byte率
     * @throws IOException
     */
    private static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                            long totalDataLen, int sampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    private String getWavFilePath() {
        return MyAppConfig.LOCAL_CACHE_VOICE_PATH + "/";
    }
}
