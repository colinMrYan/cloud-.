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
    private ArrayList<byte[]> chunkPCMDataContainer;//PCM数据块容器
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
     * 调用prepare方法 会初始化Decode 、Encode 、输入输出流 等一些列操作
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
            mediaExtractor.setDataSource(srcPath);//媒体文件的位置
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
        mediaDecode.start();//启动MediaCodec ，等待传入数据
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
            ByteBuffer inputBuffer = decodeInputBuffers[inputIndex];//拿到inputBuffer
            inputBuffer.clear();//清空之前传入inputBuffer内的数据
            int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);//MediaExtractor读取数据到inputBuffer中
            if (sampleSize <0) {//小于0 代表所有数据已读取完成
                codeOver=true;
            }else {
                mediaDecode.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0);//通知MediaDecode解码刚刚传入的数据
                mediaExtractor.advance();//MediaExtractor移动到下一取样处
                decodeSize+=sampleSize;
            }
        }

        //获取解码得到的byte[]数据 参数BufferInfo上面已介绍 10000同样为等待时间 同上-1代表一直等待，0代表不等待。此处单位为微秒
        //此处建议不要填-1 有些时候并没有数据输出，那么他就会一直卡在这 等待
        int outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, 10000);
        ByteBuffer outputBuffer;
        byte[] chunkPCM;
        while (outputIndex >= 0) {//每次解码完成的数据不一定能一次吐出 所以用while循环，保证解码器吐出所有数据
            outputBuffer = decodeOutputBuffers[outputIndex];//拿到用于存放PCM数据的Buffer
            chunkPCM = new byte[decodeBufferInfo.size];//BufferInfo内定义了此数据块的大小
            outputBuffer.get(chunkPCM);//将Buffer内的数据取出到字节数组中
            outputBuffer.clear();//数据取出后一定记得清空此Buffer MediaCodec是循环使用这些Buffer的，不清空下次会得到同样的数据
            putPCMData(chunkPCM);//自己定义的方法，供编码器所在的线程获取数据,下面会贴出代码
            mediaDecode.releaseOutputBuffer(outputIndex, false);//此操作一定要做，不然MediaCodec用完所有的Buffer后 将不能向外输出数据
            outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, 10000);//再次获取数据，如果没有数据输出则outputIndex=-1 循环结束
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
                saveFile(returnPcmList(),dstPath,currentSimpleRate);   //存储文件
                if (onCompleteListener != null) {
                    onCompleteListener.completed(dstPath); //存储成功返回数据
                    release();
                }
            } catch (Exception e ){
                if (onCompleteListener != null) {
                    onCompleteListener.returnError(e.getMessage()); //存储失败返回
                    release();
                }

            }
        }
    }

    /**
     * 存储文件
     * */
    private void saveFile(ArrayList<byte[]> pcdata , String fileName,int CurrentSimpleRate) {
        try {
            File file = new File(fileName);
            // 如果文件不存在
            if (file.exists()) {
                // 创建新的空文件
                file.delete();
            }
            file.createNewFile();
            // 获取文件的输出流对象
            FileOutputStream outStream = new FileOutputStream(file);
            for(int i=0;i<pcdata.size();i++) {
                if(8000==CurrentSimpleRate&&16000==orderSampleRate){
                    outStream.write(samplingRate8kTo16k(pcdata.get(i)));
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
        void completed(String path);
        void returnError(String Error);
    }

    /**
     * 设置解码完成监听器
     * @param onCompleteListener
     */
    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener=onCompleteListener;
    }

    /**
     * 8k转16k
     * @param inputData  输入数据流
     * @return  返回16K数据
     * */
    private  byte[]  samplingRate8kTo16k(byte[] inputData) {
        byte[] outputData = new byte[inputData.length * 2];
        Log.d("lbc",inputData.length+"size++=="+outputData.length);
        for (int i = 0; i < inputData.length; i++) {
            outputData[2 * i] = inputData[i];
            outputData[2 * i + 1] = inputData[i];
        }
        return  outputData;
    }
}
