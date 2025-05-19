package com.beatcraft.audio;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HammingWindow;
import be.tarsos.dsp.util.fft.WindowFunction;
import net.minecraft.client.sound.OggAudioStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

        try {
            var path = oggFile.getAbsolutePath();
            // if (path.endsWith(".egg")) {
            //     // Create a temp file with .ogg extension
            //     File tempOggFile = File.createTempFile("beatcraft_audio_", ".ogg");
            //     Files.copy(oggFile.toPath(), tempOggFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            //     tempOggFile.deleteOnExit(); // Clean up later
            //
            //     oggFile = tempOggFile;
            // }

            AudioInputStream originalStream = AudioSystem.getAudioInputStream(oggFile);
            AudioFormat baseFormat = originalStream.getFormat();

            AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
            );

            // Decode the audio to PCM format
            AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, originalStream);

            // Convert javax format to TarsosDSP format manually
            TarsosDSPAudioFormat tarsosFormat = new TarsosDSPAudioFormat(
                decodedFormat.getSampleRate(),
                16,
                decodedFormat.getChannels(),
                true,   // signed
                false   // little endian
            );

            // Wrap in a UniversalAudioInputStream
            UniversalAudioInputStream tarsosStream = new UniversalAudioInputStream(pcmStream, tarsosFormat);

            // Create dispatcher
            AudioDispatcher dispatcher = new AudioDispatcher(tarsosStream, bufferSize*2, bufferSize);




            FFT fft = new FFT(bufferSize * 2);
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
                            if (binIndex < numBins)
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
                public void processingFinished() {
                }
            });

            dispatcher.run();

            totalFrames = frameCounter[0];
            spectrogram = Arrays.copyOf(tempSpectrogram[0], totalFrames);
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public float[] getLevels(float timeInSeconds) {
        int frameIndex = (int) (timeInSeconds / frameDuration);
        if (frameIndex < 0 || frameIndex >= totalFrames) {
            return new float[numBars]; // silence
        }
        return spectrogram[frameIndex];
    }
}
