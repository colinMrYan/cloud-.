package com.inspur.emmcloud.util.privates.audioformat;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by libaochao on 2018/11/27.
 * 功能描述：
 *    实现MP3转pcm
 * 应用方法：
 *    实例化本类（单例模式）
 *    设置源文件路径scrPath及目标文件路径despath,
 *    设置的返回成功和失败监听
 *    开启转码
 */
public class AudioMp3ToPcm {

    private String srcPath;
    private String dstPath;
    private MediaCodec mediaDecode;
    private MediaCodec mediaEncode;
    private MediaExtractor mediaExtractor;
    private ByteBuffer[] decodeInputBuffers;
    private ByteBuffer[] decodeOutputBuffers;
    private MediaCodec.BufferInfo decodeBufferInfo;
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private ArrayList<byte[]> chunkPCMDataContainer;
    private OnCompleteListener onCompleteListener;
    private long fileTotalSize;
    private long decodeSize;
    private int  currentSimpleRate=0;
    private int  orderSampleRate=0;
    private boolean codeOver = false;

    public static AudioMp3ToPcm newInstance() {
        return new AudioMp3ToPcm();
    }

    /**
     *返回pcm数据流
     * */
    public ArrayList<byte[]>  returnPcmList() {
        return chunkPCMDataContainer;
    }

    /**
     * 设置输入输出文件位置
     * @param srcPath 源文件路径
     * @param dstPath 目标文件路径
     * @param sampleRate  要求采样率(仅支持16k, )
     */
    public void setIOPath(String srcPath, String dstPath,int sampleRate) {
        this.srcPath=srcPath;
        this.dstPath=dstPath;
        this.orderSampleRate =sampleRate;
        prepare();
    }

    /**
     * 设置输入输出文件位置(采样率默认为8K)
     * @param srcPath
     * @param dstPath
     */
    public void setIOPath(String srcPath, String dstPath) {
        this.srcPath=srcPath;
        this.dstPath=dstPath;
        prepare();
    }

    /**
     * 此类已经过封装
     * 调用prepare方法 会初始化Decode 、输入输出流 等一些列操作
     */
    private void prepare() {
        if (srcPath == null) {
            throw new IllegalArgumentException("srcPath can't be null");
        }

        if (dstPath == null) {
            throw new IllegalArgumentException("dstPath can't be null");
        }

        File file = new File(srcPath);
        fileTotalSize=file.length();
        chunkPCMDataContainer= new ArrayList<>();
        initMediaDecode();//解码器
    }

    /**
     * 初始化解码器
     */
    private void initMediaDecode() {
        try {
            mediaExtractor=new MediaExtractor();//此类可分离视频文件的音轨和视频轨道
            mediaExtractor.setDataSource(srcPath);
            for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {//遍历媒体轨道 此处我们传入的是音频文件，所以也就只有一条轨道
                MediaFormat format = mediaExtractor.getTrackFormat(i);
                currentSimpleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio")) {//获取音频轨道
                    mediaExtractor.selectTrack(i);//选择此音频轨道
                    mediaDecode = MediaCodec.createDecoderByType(mime);//创建Decode解码器
                    mediaDecode.configure(format, null, null, 0);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mediaDecode == null) {
            Log.e("lbc", "create mediaDecode failed");
            return;
        }
        mediaDecode.start();
        decodeInputBuffers=mediaDecode.getInputBuffers();//MediaCodec在此ByteBuffer[]中获取输入数据
        decodeOutputBuffers=mediaDecode.getOutputBuffers();//MediaCodec将解码后的数据放到此ByteBuffer[]中 我们可以直接在这里面得到PCM数据
        decodeBufferInfo=new MediaCodec.BufferInfo();//用于描述解码得到的byte[]数据的相关信息
    }

    /**
     * 开始转码
     * 音频数据{@link #srcPath}先解码成PCM  PCM数据在编码成想要得到的{@link}音频格式
     * mp3->PCM->aac
     */
    public void startAsync() {
        new Thread(new DecodeRunnable()).start();
    }

    /**
     * 将PCM数据存入{@link #chunkPCMDataContainer}
     * @param pcmChunk PCM数据块
     */
    private void putPCMData(byte[] pcmChunk) {
        synchronized (AudioMp3ToPcm.class) {//记得加锁
            chunkPCMDataContainer.add(pcmChunk);
        }
    }

    /**
     * 解码{@link #srcPath}音频文件 得到PCM数据块
     * @return 是否解码完所有数据
     */
    private void srcAudioFormatToPCM() {
        for (int i = 0; i < decodeInputBuffers.length-1; i++) {
            int inputIndex = mediaDecode.dequeueInputBuffer(-1);//获取可用的inputBuffer -1代表一直等待，0表示不等待 建议-1,避免丢帧
            if (inputIndex < 0) {
                codeOver =true;
                return;
            }
            ByteBuffer inputBuffer = decodeInputBuffers[inputIndex];
            inputBuffer.clear();//清空之前传入inputBuffer内的数据
            int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);//MediaExtractor读取数据到inputBuffer中
            if (sampleSize <0) {
                codeOver=true;
            }else {
                mediaDecode.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0);//通知MediaDecode解码刚刚传入的数据
                mediaExtractor.advance();//MediaExtractor移动到下一取样处
                decodeSize+=sampleSize;
            }
        }
        int outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, 10000);
        ByteBuffer outputBuffer;
        byte[] chunkPCM;
        while (outputIndex >= 0) {
            outputBuffer = decodeOutputBuffers[outputIndex];
            chunkPCM = new byte[decodeBufferInfo.size];
            outputBuffer.get(chunkPCM);
            outputBuffer.clear();
            putPCMData(chunkPCM);
            mediaDecode.releaseOutputBuffer(outputIndex, false);
            outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, 10000);
        }

    }

    /**
     * 释放资源
     */
    public void release() {
        try {
            if (bos != null) {
                bos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    bos=null;
                }
            }
        }
        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            fos=null;
        }
        if (mediaEncode != null) {
            mediaEncode.stop();
            mediaEncode.release();
            mediaEncode=null;
        }
        if (mediaDecode != null) {
            mediaDecode.stop();
            mediaDecode.release();
            mediaDecode=null;
        }
        if (mediaExtractor != null) {
            mediaExtractor.release();
            mediaExtractor=null;
        }
        if (onCompleteListener != null) {
            onCompleteListener=null;
        }
    }

    /**
     * 解码线程
     */
    class DecodeRunnable implements Runnable{
        @Override
        public void run() {
            while (!codeOver) {
                srcAudioFormatToPCM();
            }
            try {
                saveFile(returnPcmList(),dstPath,currentSimpleRate);
                if (onCompleteListener != null) {
                    onCompleteListener.returnSuccess(dstPath);
                    release();
                }
            } catch (Exception e ){
                if (onCompleteListener != null) {
                    onCompleteListener.returnError(e.getMessage());
                    release();
                }

            }
        }
    }

    /**
     * 存储文件
     * @param CurrentSimpleRate
     * @param fileName
     * @param pcdata
     * */
    private void saveFile(ArrayList<byte[]> pcdata , String fileName,int CurrentSimpleRate) {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream outStream = new FileOutputStream(file);
            for(int i=0;i<pcdata.size();i++) {
                if(8000==CurrentSimpleRate&&16000==orderSampleRate){
                    outStream.write(doubleSamplingRate(pcdata.get(i)));
                } else {
                    outStream.write(pcdata.get(i));
                }
            }
            // 最后关闭文件输出流
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 转码完成回调接口
     */
    public interface OnCompleteListener{
       void returnSuccess(String path);
        void returnError(String Error);
    }

    /**
     * 设置解码完成监听器
     * @param onCompleteListener 转码完成监听设置
     */
    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener=onCompleteListener;
    }

    /**
     * 采样率增加一倍
     * @param inputData  输入数据流
     * @return  返回16K数据
     * */
    private  byte[]  doubleSamplingRate(byte[] inputData) {
        byte[] outputData = new byte[inputData.length * 2];
        Log.d("lbc",inputData.length+"size++=="+outputData.length);
        for (int i = 0; i < inputData.length; i++) {
            outputData[2 * i] = inputData[i];
            outputData[2 * i + 1] = inputData[i];
        }
        return  outputData;
    }
}
