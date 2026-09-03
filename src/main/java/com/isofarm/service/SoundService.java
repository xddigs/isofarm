package com.isofarm.service;

import com.isofarm.data.Singleton;
import com.isofarm.data.SoundGroup;
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
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;

@Singleton
public class SoundService implements Service<SoundGroup> {
    public static final SoundService fx = new SoundService();
    private static final Logger log = LoggerFactory.getLogger(SoundService.class);
    private long device;
    private long context;
    private final Map<String, Integer> soundBuffers = new HashMap<>();

    private int stepSource;
    private int breakSource;
    private int placeSource;
    private int entitySource;
    private int backgroundSource;
    private int loopingSource;
    private int useSource;

    private String currentBackgroundSound;
    private boolean hasLoaded = false;

    public SoundService() {
        soundBuffers.clear();
        if (!hasLoaded) {
            this.init();
            hasLoaded = true;
        }

        for (SoundGroup group : SoundGroup.values()) {
            loadSoundArray(group.getStepSounds());
            loadSoundArray(group.getBreakSounds());
            loadSoundArray(group.getPlaceSounds());
            loadSoundArray(group.getEntitySounds());
            loadSoundArray(group.getBackgroundSounds());
            loadSoundArray(group.getLoopingSounds());
            loadSoundArray(group.getUseSounds());
        }
    }

    private void loadSoundArray(String[] paths) {
        if (paths == null) return;
        for (String path : paths) {
            if (path != null && !soundBuffers.containsKey(path)) {
                int bufferId = loadOgg(path);
                soundBuffers.put(path, bufferId);
            }
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
        breakSource = alGenSources();
        placeSource = alGenSources();
        entitySource = alGenSources();
        backgroundSource = alGenSources();
        loopingSource = alGenSources();
        useSource = alGenSources();

        alSourcef(breakSource, AL_GAIN, 1.0f);
        alSourcef(stepSource, AL_GAIN, 1.0f);
        alSourcef(placeSource, AL_GAIN, 1.0f);
        alSourcef(entitySource, AL_GAIN, 1.0f);
        alSourcef(backgroundSource, AL_GAIN, 1.0f);
        alSourcef(loopingSource, AL_GAIN, 1.0f);
        alSourcef(useSource, AL_GAIN, 1.0f);
    }

    public void playStepSound(SoundGroup group) {
        playSound(stepSource, group != null ? group.getStepSounds() : null,
                0.95f, 0.1f, 1.0f);
    }

    public void playBreakSound(SoundGroup group) {
        playSound(breakSource, group != null ? group.getBreakSounds() : null,
                0.8f, 0.2f, 1.0f);
    }

    public void playPlaceSound(SoundGroup group) {
        playSound(breakSource, group != null ? group.getPlaceSounds() : null,
                0.75f, 0.2f, 1.0f);
    }

    public void playEntitySound(SoundGroup group) {
        playSound(entitySource, group != null ? group.getEntitySounds() : null,
                1.0f, 0.2f, 1.2f);
    }

    public void playLoopingSound(SoundGroup group) {
        if (group == null) return;
        String[] sounds = group.getLoopingSounds();
        if (sounds == null || sounds.length == 0) return;
        playSound(loopingSource, sounds, 1.0f, 0.0f, 1.0f);
    }

    public void playUseSound(SoundGroup group) {
        if (group == null) return;
        String[] sounds = group.getUseSounds();
        if (sounds == null || sounds.length == 0) return;
        playSound(useSource, sounds, 1.0f, 0.0f, 1.0f);
    }

    public void setBackgroundSound(SoundGroup group) {
        if (group == null) {
            stopBackgroundSound();
            return;
        }

        String[] sounds = group.getBackgroundSounds();

        if (sounds == null || sounds.length == 0) {
            stopBackgroundSound();
            return;
        }

        String soundPath = sounds[0];

        if (soundPath.equals(currentBackgroundSound)
                && alGetSourcei(backgroundSource, AL_SOURCE_STATE) == AL_PLAYING) {
            return;
        }

        Integer bufferId = soundBuffers.get(soundPath);

        if (bufferId == null || bufferId <= 0) {
            return;
        }

        alSourceStop(backgroundSource);

        alSourcei(backgroundSource, AL_BUFFER, bufferId);
        alSourcei(backgroundSource, AL_LOOPING, AL_TRUE);
        alSourcef(backgroundSource, AL_PITCH, 1.0f);
        alSourcef(backgroundSource, AL_GAIN, 0.8f);

        currentBackgroundSound = soundPath;

        alSourcePlay(backgroundSource);
    }

    private void playSound(int source, String[] sounds, float basePitch,
                           float pitchVariation, float volume) {
        if (sounds == null || sounds.length == 0) return;
        String selectedSoundPath = sounds[(int) (Math.random() * sounds.length)];
        Integer bufferId = soundBuffers.get(selectedSoundPath);

        if (bufferId != null && bufferId > 0) {
            alSourceStop(source);
            alSourcei(source, AL_BUFFER, bufferId);
            alSourcef(source, AL_PITCH, basePitch + (float) Math.random() * pitchVariation);
            alSourcef(source, AL_GAIN, volume);
            alSourcePlay(source);
        }
    }

    public void stopBackgroundSound() {
        if (alGetSourcei(backgroundSource, AL_SOURCE_STATE) == AL_PLAYING) {
            alSourceStop(backgroundSource);
        }

        alSourcei(backgroundSource, AL_BUFFER, 0);
        currentBackgroundSound = null;
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

                ShortBuffer rawAudioBuffer = stb_vorbis_decode_memory(
                        buffer, channelsBuffer, sampleRateBuffer);

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

    public void cleanup() {
        alDeleteSources(stepSource);
        alDeleteSources(breakSource);
        alDeleteSources(placeSource);
        alDeleteSources(entitySource);
        alDeleteSources(backgroundSource);
        soundBuffers.values().forEach(AL10::alDeleteBuffers);
        alcMakeContextCurrent(MemoryUtil.NULL);
        alcDestroyContext(context);
        alcCloseDevice(device);
    }
}