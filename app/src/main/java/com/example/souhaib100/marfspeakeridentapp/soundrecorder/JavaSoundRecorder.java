package com.example.souhaib100.marfspeakeridentapp.soundrecorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class JavaSoundRecorder {

    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static int frequency = 8000;
    private AudioRecord audioRecord = null;
    private int recBufSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private int RECORDDING_TIME = 1;
    private String filepath = "";
    private String recordNameFile = "";


    private String getFilename() {
        //File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        File file = new File(filepath);
        if (file.exists()) {
            file.delete();
        }

        return (file.getAbsolutePath() + File.separator + recordNameFile);
    }

    private String getTempFilename() {
        //File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        File file = new File(filepath);
        if (!file.exists()) {
            file.mkdirs();
        }

        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

//        if(tempFile.exists())
//            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    public void startRecord() {

        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);
        if (tempFile.exists())
            tempFile.delete();

        createAudioRecord();
        audioRecord.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");

        recordingThread.start();
        Log.i("Audio record", "START");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                stopRecord();
            }
        }, RECORDDING_TIME);
    }

    private void writeAudioDataToFile() {
        byte data[] = new byte[recBufSize];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;

        if (null != os) {
            while (isRecording) {
                read = audioRecord.read(data, 0, recBufSize);

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRecord() {
        if (null != audioRecord) {
            isRecording = false;

            audioRecord.stop();
            audioRecord.release();

            audioRecord = null;
            recordingThread = null;

            copyWaveFile(getTempFilename(), getFilename());
            //deleteTempFile();
        }


        Log.i("Audio record", "STOP");
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());

        file.delete();
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = frequency;
        int channels = 1;
        long byteRate = RECORDER_BPP * frequency * channels / 8;

        byte[] data = new byte[recBufSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            //AppLog.logString("File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (1 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    private void createAudioRecord() {
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        int encodingBitRate = AudioFormat.ENCODING_PCM_16BIT;
        recBufSize = AudioRecord.getMinBufferSize(frequency,
                channelConfiguration, encodingBitRate);

        //Log.e("Buffer Size: ",String.valueOf(recBufSize));

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                channelConfiguration, encodingBitRate, recBufSize);
        System.out.println("AudioRecord");
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public void setRecordNameFile(String recordNameFile) {
        this.recordNameFile = recordNameFile;
    }

    public void setRECORDDING_TIME(int RECORDDING_TIME) {
        this.RECORDDING_TIME = RECORDDING_TIME;
    }
}

