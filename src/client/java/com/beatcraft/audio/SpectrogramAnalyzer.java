package com.beatcraft.audio;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HammingWindow;
import be.tarsos.dsp.util.fft.WindowFunction;

import java.io.File;
import java.util.Arrays;

public class SpectrogramAnalyzer {
    private final int numBars;
    private final float sampleRate;
    private final int bufferSize;
    private final float[][] spectrogram;
    private final float frameDuration;
    private final int totalFrames;

    public SpectrogramAnalyzer(File oggFile, int numBars) {
        this.numBars = numBars;
        this.sampleRate = 44100f;
        this.bufferSize = 1024;
        this.frameDuration = bufferSize / sampleRate;

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(
            oggFile.getAbsolutePath(),
            (int) sampleRate,
            bufferSize,
            0
        );

        FFT fft = new FFT(bufferSize);
        WindowFunction window = new HammingWindow();
        int numBins = bufferSize / 2;
        final float[][][] tempSpectrogram = {new float[4096][numBars]};
        int[] frameCounter = {0};

        dispatcher.addAudioProcessor(new AudioProcessor() {
            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] originalBuffer = audioEvent.getFloatBuffer();
                float[] buffer = Arrays.copyOf(originalBuffer, originalBuffer.length); // Safe copy

                window.apply(buffer);
                float[] amplitudes = new float[numBins];
                fft.forwardTransform(buffer);
                fft.modulus(buffer, amplitudes);

                float[] bars = new float[numBars];
                int binPerBar = numBins / numBars;
                for (int i = 0; i < numBars; i++) {
                    float sum = 0f;
                    for (int j = 0; j < binPerBar; j++) {
                        int binIndex = i * binPerBar + j;
                        if (binIndex < amplitudes.length)
                            sum += amplitudes[binIndex];
                    }
                    bars[i] = sum / binPerBar;
                }

                if (frameCounter[0] >= tempSpectrogram[0].length) {
                    tempSpectrogram[0] = Arrays.copyOf(tempSpectrogram[0], tempSpectrogram[0].length * 2);
                }

                tempSpectrogram[0][frameCounter[0]++] = bars;
                return true;
            }

            @Override
            public void processingFinished() {}
        });

        dispatcher.run();

        totalFrames = frameCounter[0];
        spectrogram = Arrays.copyOf(tempSpectrogram[0], totalFrames);
    }

    public float[] getLevels(float timeInSeconds) {
        int frameIndex = (int) (timeInSeconds / frameDuration);
        if (frameIndex < 0 || frameIndex >= totalFrames) {
            return new float[numBars]; // silence
        }
        return spectrogram[frameIndex];
    }
}
