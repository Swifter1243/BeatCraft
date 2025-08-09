package com.beatcraft.client.audio;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class SpectrogramAnalyzer {
    private final int numBars;
    private final int sampleRate;
    private final int channels;
    private final float duration;
    private final File audioFile;
    private final boolean loadedSuccessfully;

    private static final int FFT_SIZE = 1024;

    private static final float MAGNITUDE_MULTIPLIER = 0.00001f;

    private long decoder;

    public SpectrogramAnalyzer(File audioFile, int numBars) {
        this.numBars = numBars;
        this.audioFile = audioFile;

        int tempSampleRate = 44100;
        int tempChannels = 2;
        float tempDuration = 0;
        boolean success = false;

        if (audioFile.exists()) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer error = stack.mallocInt(1);

                decoder = STBVorbis.stb_vorbis_open_filename(audioFile.getAbsolutePath(), error, null);

                if (decoder != 0) {
                    STBVorbisInfo info = STBVorbisInfo.malloc(stack);
                    STBVorbis.stb_vorbis_get_info(decoder, info);

                    tempChannels = info.channels();
                    tempSampleRate = info.sample_rate();
                    tempDuration = STBVorbis.stb_vorbis_stream_length_in_seconds(decoder);
                    success = true;
                }
            } catch (Exception e) {
                if (decoder != 0) {
                    STBVorbis.stb_vorbis_close(decoder);
                    decoder = 0;
                }
            }
        }

        this.channels = tempChannels;
        this.sampleRate = tempSampleRate;
        this.duration = tempDuration;
        this.loadedSuccessfully = success;
    }

    public float[] getLevels(float timeInSeconds) {
        if (!loadedSuccessfully || decoder == 0) {
            return new float[numBars];
        }

        timeInSeconds = Math.max(0, Math.min(duration, timeInSeconds));

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ShortBuffer audioBuffer = stack.mallocShort(FFT_SIZE * channels);

            STBVorbis.stb_vorbis_seek_frame(decoder, (int)(timeInSeconds * sampleRate));

            int samplesRead = STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, audioBuffer);

            if (samplesRead <= 0) {
                return new float[numBars];
            }

            double[] windowedData = new double[FFT_SIZE];

            for (int i = 0; i < FFT_SIZE && i < samplesRead; i++) {
                short sample;

                if (channels == 2) {
                    if (i*2+1 < audioBuffer.capacity()) {
                        sample = (short)((audioBuffer.get(i*2) + audioBuffer.get(i*2+1)) / 2);
                    } else {
                        sample = 0;
                    }
                } else {
                    if (i < audioBuffer.capacity()) {
                        sample = audioBuffer.get(i);
                    } else {
                        sample = 0;
                    }
                }

                double hammingWindow = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (FFT_SIZE - 1));
                windowedData[i] = sample * hammingWindow;
            }

            for (int i = samplesRead; i < FFT_SIZE; i++) {
                windowedData[i] = 0;
            }

            FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
            Complex[] spectrum = fft.transform(windowedData, TransformType.FORWARD);

            int numBins = FFT_SIZE / 2;
            float[] amplitudes = new float[numBins];

            for (int i = 0; i < numBins; i++) {
                amplitudes[i] = (float)spectrum[i].abs() * MAGNITUDE_MULTIPLIER;
            }

            float[] bars = new float[numBars];
            int binsPerBar = numBins / numBars;

            for (int i = 0; i < numBars; i++) {
                float sum = 0f;
                for (int j = 0; j < binsPerBar; j++) {
                    int binIndex = i * binsPerBar + j;
                    if (binIndex < amplitudes.length) {
                        sum += amplitudes[binIndex];
                    }
                }
                bars[i] = sum / binsPerBar;
            }

            return bars;
        } catch (Exception e) {
            return new float[numBars];
        }
    }

    public float getDuration() {
        return this.duration;
    }

}