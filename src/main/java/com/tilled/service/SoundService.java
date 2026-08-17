package com.tilled.service;

import com.tilled.data.StepSoundGroup;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;

public class SoundService {
    private static final Logger log = LoggerFactory.getLogger(SoundService.class);
    private long device;
    private long context;
    private final Map<String, Integer> soundBuffers = new HashMap<>();
    private int stepSource;

    public SoundService() {
        this.init();
        for (StepSoundGroup type : StepSoundGroup.values()) {
            int bufferId = loadOgg(type.sound1());
            int bufferId2 = loadOgg(type.sound2());
            int bufferId3 = loadOgg(type.sound3());
            int bufferId4 = loadOgg(type.sound4());
            soundBuffers.put(type.name(), bufferId);
            soundBuffers.put(type.name(), bufferId2);
            soundBuffers.put(type.name(), bufferId3);
            soundBuffers.put(type.name(), bufferId4);
        }
    }

    public void init() {
        device = alcOpenDevice((CharSequence) null);
        if (device == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to open audio device!");
        }

        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        context = alcCreateContext(device, (int[]) null);
        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);
        stepSource = alGenSources();
    }

    public int getStepSource() {
        return stepSource;
    }

    public Map<String, Integer> getSoundBuffers() {
        return soundBuffers;
    }

    public void cleanup() {
        alDeleteSources(stepSource);
        soundBuffers.values().forEach(AL10::alDeleteBuffers);
        alcMakeContextCurrent(MemoryUtil.NULL);
        alcDestroyContext(context);
        alcCloseDevice(device);
    }

    public int loadOgg(String resourcePath) {
        if (resourcePath == null) return -1;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                log.error("Audio resource not found in classpath: {}", resourcePath);
                return -1;
            }

            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes);
            buffer.flip();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer channelsBuffer = stack.mallocInt(1);
                IntBuffer sampleRateBuffer = stack.mallocInt(1);

                ShortBuffer rawAudioBuffer = stb_vorbis_decode_memory(buffer, channelsBuffer,
                        sampleRateBuffer);

                MemoryUtil.memFree(buffer);
                if (rawAudioBuffer == null) {
                    log.error("Failed to decode OGG memory: {}", resourcePath);
                    return -1;
                }

                int channels = channelsBuffer.get(0);
                int sampleRate = sampleRateBuffer.get(0);
                int format = (channels == 1) ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

                int bufferId = alGenBuffers();
                alBufferData(bufferId, format, rawAudioBuffer, sampleRate);
                MemoryUtil.memFree(rawAudioBuffer);

                return bufferId;
            }
        } catch (Exception e) {
            log.error("Error loading OGG sound {}: {}", resourcePath, e.getMessage());
            return -1;
        }
    }

    public void playStepSound(StepSoundGroup group) {
        if (group == null || group == StepSoundGroup.SILENT) return;
        int variant = (int) (Math.random() * 4) + 1;
        String key = (variant == 1) ? group.name() : group.name() + variant;
        stepFX(key);
    }

    public void stepFX(String soundKey) {
        Integer bufferId = soundBuffers.get(soundKey);
        if (bufferId == null || bufferId <= 0) {
            return;
        }

        alSourceStop(stepSource);
        alSourcei(stepSource, AL_BUFFER, bufferId);
        alSourcef(stepSource, AL_PITCH, 0.95f + (float) Math.random() * 0.1f);
        alSourcePlay(stepSource);
    }
}