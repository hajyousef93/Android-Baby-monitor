package com.example.haj.babymonitor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Haj on 28/08/17.
 */

public class MicrophoneInput extends AppCompatActivity {
    private static final int[] frequencyArray = {44100,22050,11025,8000};
    public int frequency = MicrophoneInput.frequencyArray[3];
    private static int channel = AudioFormat.CHANNEL_IN_MONO;//////هون غيرناااااااا اي لوصنااااCHANNEL_CONFIGURATION_MONO
    private static int encoding = AudioFormat.ENCODING_PCM_16BIT;
    private static int bufferSize;
    private short[] audioData;
    private int currentAmplitude;
    private AudioRecord recorder;

    public MicrophoneInput()
    {
        try
        {
            bufferSize = AudioRecord.getMinBufferSize(frequency, channel, encoding);
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,	frequency, channel, encoding, bufferSize * 5);
        }
        catch (Exception e)
        {
            System.out.println("Error in startRecording(): " + e.toString());
            currentAmplitude = -1;
        }
    }
    protected int getCurrentLoudness() {
        // already initialized? (e.g. RECORD.AUDIO not set)
        if (recorder.getState() != android.media.AudioRecord.STATE_INITIALIZED)
        {
            return -3;
        }
        if (recorder.getRecordingState() == android.media.AudioRecord.RECORDSTATE_STOPPED)
        {
            recorder.startRecording(); //check to see if the Recorder has stopped or is not recording, and make it record.
        }
        try
        {
            // read
            audioData = new short[1000];
            int noOfResults = recorder.read(audioData,0,1000); //read the PCM audio data into the audioData array

            if (noOfResults < 1){
                return -4;
            }
        }
        catch(Exception e){
            System.out.println("Error with recorder.read() " + e.toString());
            e.printStackTrace();
            currentAmplitude = -5;
        }
        try
        {
            // TODO calculate "real" mean amplitude

            // calculate geometric mean
            float mean;
            int sum = 0;
            int numberOfElements = 0;

            for (short i=0; i<1000; i++)
            {
                if (audioData[i] != 0)
                {
                    numberOfElements++;
                    if (audioData[i] > 0)
                        sum = sum + audioData[i];
                    else
                        sum = sum + (-1 * audioData[i]);
                }
            }

            mean = sum / numberOfElements;

            currentAmplitude = (int) mean;
        }
        catch (Exception e)
        {
            System.out.println("Error in calculating amplitude's mean: " + e.toString());
            e.printStackTrace();
            currentAmplitude = -6;
        }

        return currentAmplitude;
    }
    public void stop(){
        recorder.stop();
        recorder.release();
        recorder = null;
        int dummy=0;
    }
}
