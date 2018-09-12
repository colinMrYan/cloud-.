package com.inspur.emmcloud.util.common.audioformat;

import com.inspur.emmcloud.util.common.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import it.sauronsoftware.jave.InputFormatException;


/**
 * 格式转换类 此类仅实现了部分音频格式的转换，如需更多格式转换，
 * 可参考JAVE官方文档：http://www.sauronsoftware.it/projects/jave/manual.php。
 * 或参考：http://blog.csdn.net/qllinhongyu/article/details/29817297
 * https://github.com/li-yuqi/AudioFormatConvertUtil
 *
 * @author liyuqi
 * @date 2017年1月11日
 */
public class ConvertAudioFileFormatUtil {
    // 科大讯飞语音合成APPID
    private static String PCM = ".pcm";
    private static String WAV = ".wav";
    private static String MP3 = ".mp3";
    private static String AMR = ".amr";
    // 文字转语音文件路径，仅写到文件名前缀，后面应补齐文件名及格式后缀

    /**
     * wav转amr，默认单声道。格式转换成功后立即删除源文件。
     * 注意：1、仅支持单声道wav文件转amr；2、除非特殊指定，否则比特率与采样率都不要修改，使用默认值即可！
     *
     * @param source         源文件
     * @param target         转换后的文件
     * @param isDeleteSource 是否删除源文件，true 删除，false 保留
     * @param bitRate        音频比特率，默认12.2Kbit/s
     * @param samplingRate   采样率，默认8000Hz
     * @throws InputFormatException
     * @throws IllegalArgumentException
     * @author liyuqi
     * @date 2017年1月11日
     */
    public static void wavToAmr(File source, File target, boolean isDeleteSource, Integer bitRate, Integer samplingRate)
            throws IllegalArgumentException, InputFormatException {
        AudioAttributes audio = new AudioAttributes();
//		audio.setCodec("libamr_nb");//该处经测试放开无法在linux执行成功，暂时注掉
        audio.setBitRate(bitRate == null ? 12200 : bitRate);
        audio.setSamplingRate(samplingRate == null ? 8000 : samplingRate);
        audio.setChannels(1);
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("amr");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(source, target, attrs);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        System.out.println("****wav 转 amr转换成功****");
        if (isDeleteSource) {
            source.delete();// 删除源文件
            System.out.println("******删除源成功******");
        }
    }

    /**
     * mp3转wav，格式转换成功后立即删除源文件
     *
     * @param source         源文件
     * @param target         转换后得到的文件
     * @param isDeleteSource 是否删除源文件，true 删除，false 保留
     * @throws EncoderException
     * @throws InputFormatException
     * @throws IllegalArgumentException
     * @author liyuqi
     * @date 2017年1月11日
     */
    public static void mp3ToWav(File source, File target, boolean isDeleteSource)
            throws IllegalArgumentException, InputFormatException, EncoderException {
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("wav");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        encoder.encode(source, target, attrs);
        LogUtils.YfcDebug("****mp3 转 wav转换成功****");
        if (isDeleteSource) {
            source.delete();// 删除源文件
            LogUtils.YfcDebug("******删除源成功******");
        }
    }

    /**
     * wav转mp3，格式转换成功后立即删除源文件
     *
     * @param source         源文件
     * @param target         转换后得到的文件
     * @param isDeleteSource 是否删除源文件，true 删除，false 保留
     * @param bitRate        音频比特率，默认128kbit/s
     * @param channels       声道，默认双声道
     * @param samplingRate   采样率，默认44100Hz
     * @throws EncoderException
     * @throws InputFormatException
     * @throws IllegalArgumentException
     * @author liyuqi
     * @date 2017年1月11日
     */
    public static void wavToMp3(File source, File target, boolean isDeleteSource, Integer bitRate, Integer channels, Integer samplingRate) {
        try {
            if(target.exists()){
                target.delete();
            }
            target.createNewFile();
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libmp3lame");
            audio.setBitRate(bitRate == null ? new Integer(128000) : bitRate);
            audio.setChannels(channels == null ? new Integer(2) : channels);
            audio.setSamplingRate(samplingRate == null ? new Integer(44100) : samplingRate);
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setFormat("mp3");
            attrs.setAudioAttributes(audio);
            Encoder encoder = new Encoder();
            encoder.encode(source, target, attrs);
            LogUtils.YfcDebug("****wav 转 mp3转换成功****");
            if (isDeleteSource) {
                source.delete();// 删除源文件
                LogUtils.YfcDebug("******删除源成功******");
            }
        } catch (Exception e) {
			LogUtils.YfcDebug("异常："+e.getMessage());
        }

    }

    /**
     * pcm转wav，格式转换成功后立即删除源文件，得到的wav文件头信息为：16位声双道 8000 hz
     *
     * @param source         源文件
     * @param target         目标文件
     * @param isDeleteSource 是否删除源文件，true 删除，false 保留
     * @throws IOException
     * @throws Exception   抛出错误
     * @author liyuqi
     * @date 2017年1月11日
     */
    public static void pcmToWav(File source, File target, boolean isDeleteSource)
            throws IOException {
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(target);
        // 计算长度
        byte[] buf = new byte[1024 * 4];
        int size = fis.read(buf);
        int PCMSize = 0;
        while (size != -1) {
            PCMSize += size;
            size = fis.read(buf);
        }
        fis.close();
        // 填入参数，比特率等等。这里用的是16位声双道 8000 hz
        WaveHeader header = new WaveHeader();
        // 长度字段 = 内容的大小（PCMSize) + 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
        header.fileLength = PCMSize + (44 - 8);
        header.FmtHdrLeth = 16;// 头字节数，16或18，如果是18则又附加信息
        header.BitsPerSample = 16;// 每个采样需要的bit数，相当于64K，计算方式为16位(16bit)则代表2的16次方=65536 / 1024 =64K
        header.Channels = 1;// 声道，单声道为1，双声道为2
        header.FormatTag = 0x0001;// 编码方式，一般为0x0001
        header.SamplesPerSec = 16000;// 采样频率
        header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);// 数据块对齐单位(每个采样需要的字节数)
        header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;// 每秒所需字节数
        header.DataHdrLeth = PCMSize;// 采样数据字节长度
        byte[] head = header.getHeader();
        assert head.length == 44; // WAV标准，头部应该是44字节
        // 写入wav头信息
        fos.write(head, 0, head.length);
        // 写入数据流
        fis = new FileInputStream(source);
        size = fis.read(buf);
        while (size != -1) {
            fos.write(buf, 0, size);
            size = fis.read(buf);
        }
        fis.close();
        fos.close();
        System.out.println("****pcm 转 wav格式转换成功****");
        if (isDeleteSource) {
            source.delete();// 删除源文件
            System.out.println("******删除源成功******");
        }
    }

//	/**
//	 * 文字转amr语音。
//	 * 注意：拿到合成的语音文件全路径后，最好先等待一段时间再去操作合成的语音文件，
//	 * 否则会存在问题。至于等待时间长短与要合成语音的文字量成正比。
//	 * 先生成pcm，然后转为wav，最终转为amr文件， 最终得到的文件全路径为path.mp3
//	 *
//	 * @author liyuqi
//	 * @date 2017年1月12日
//	 * @param text 要转为语音的文字
//	 * @param fileSign 文件标记
//	 * @param voiceType 发音人类型，默认为0。范围 0~6 均支持中英文，3 是粤语，4 是湾湾，其他均为普通话， 0~4 是女声， 5~6 是男声。
//	 * @param volume 音量，默认为50，范围 0~100
//	 * @return amr文件全路径
//	 */
//	public static String TextToAmrFile(String text, String fileSign, Integer voiceType, Integer volume) {
//		// 1、初始化语音合成引擎
//		SpeechUtility.createUtility(SpeechConstant.APPID + "=" + KE_DA_XUN_FEI_APPID);
//		// 2、合成监听器，匿名内部类实现接口
//		SynthesizeToUriListener synthesizeToUriListener = new SynthesizeToUriListener() {
//			/**
//			 * 会话合成完成回调函数
//			 * @author liyuqi
//			 * @param pcmPath 合成文件路径
//			 * @param error 错误信息
//			 */
//			@Override
//			public void onSynthesizeCompleted(String pcmPath, SpeechError error) {
//				if (error == null) {
//					System.out.println("*****文字转语音成功，合成pcm路径：" + pcmPath);
//					try {
//						// pcm转wav
//						String wavPath = pcmPath.replace(PCM, WAV);
//						File wavFile = new File(wavPath);
//						pcmToWav(new File(pcmPath), wavFile, true);
//						// wav转amr
//						String amrPath = wavPath.replace(WAV, AMR);
//						System.out.println("amr路径：" + amrPath);
//						File amrFile = new File(amrPath);
//						wavToAmr(wavFile, amrFile, true, null, null);
//						// wav转mp3
//						// String mp3Path = wavPath.replace(WAV,MP3);
//						// System.out.println("mp3路径：" + mp3Path);
//						// File amrFile = new File(mp3Path);
//						// wavToMp3(wavFile, mp3File, true, null, null, null);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				} else {
//					System.out.println("****错误码："+ error.getErrorCode() + "***错误描述：" + error.getErrorDesc());
//				}
//			}
//
//			// progress为合成进度0~100
//			@Override
//			public void onBufferProgress(int progress) {
//				System.out.println("*****合成进度：" + progress);
//			}
//		};
//		// 3、创建 SpeechSynthesizer 对象
//		SpeechSynthesizer synthesizer = SpeechSynthesizer.createSynthesizer();
//		// 4、合成参数设置
//		synthesizer.setParameter(SpeechConstant.SPEED, "50");// 设置语速，范围 0~100
//		synthesizer.setParameter(SpeechConstant.PITCH, "50");// 设置语调，范围 0~100
//		int type = voiceType != null && voiceType >= 0 && voiceType <= 6 ? voiceType : 0;
//		synthesizer.setParameter(SpeechConstant.VOICE_NAME,VOICE_NAME[type]);// 设置发声人
//		System.out.println("发音人 |    类别   |   属性  |    语言       |参数名称 \n"
//				+ "0小燕    |青年女声 |中英文 |普通话        |xiaoyan 小燕\n"
//				+ "1小研    |青年女声 |中英文 |普通话        |vixy 小研\n"
//				+ "2小琪    |青年女声 |中英文 |普通话        |vixq 小琪\n"
//				+ "3小梅    |青年女声 |中英文 |粤语           |vixm 小梅\n"
//				+ "4小莉    |青年女声 |中英文 |台湾普通话 |vixl 小莉\n"
//				+ "5小宇    |青年男声 |中英文 |普通话        |xiaoyu 小宇\n"
//				+ "6小峰    |青年男声 |中英文 |普通话        |vixf 小峰\n");
//		System.out.println("******实际发音人位置下标：" + type + "，发音人：" + VOICE_NAME[type]);
//		synthesizer.setParameter(SpeechConstant.VOLUME, String.valueOf(volume != null && volume >= 0 && volume <= 100 ? volume : 50));// 设置音量，范围：0~100
//		System.out.println("******音量大小：" + volume);
//		// 5、开始合成
//		// 设置合成音频保存位置（可自定义保存位置），默认保存在“./iflytek.pcm”
//		String path = PATH + fileSign + "/" + System.currentTimeMillis() + PCM;
//		synthesizer.synthesizeToUri(text, path, synthesizeToUriListener);
//		return path.replace(PCM, AMR);
//	}


    /**
     * amr转mp3
     * @param sourcePath
     * @param targetPath
     */
    public static void amrToMp3(String sourcePath, String targetPath) {
        File source = new File(sourcePath);
        File target = new File(targetPath);
        AudioAttributes audio = new AudioAttributes();
        Encoder encoder = new Encoder();
        audio.setCodec("libmp3lame");
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);
        try {
            encoder.encode(source, target, attrs);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InputFormatException e) {
            e.printStackTrace();
        } catch (EncoderException e) {
            e.printStackTrace();
        }
    }

}
