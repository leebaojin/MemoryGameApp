package iss.workshop.memorygameapp;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundPlayer {

    private static SoundPool soundPool;
    private static int matchedSound;
    private static int unmatchedSound;

    public SoundPlayer(Context context) {
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);

        matchedSound = soundPool.load(context, R.raw.match, 1);
        unmatchedSound = soundPool.load(context, R.raw.unmatch, 1);
    }

    public void playMatchedSound() {
        soundPool.play(matchedSound, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    public void playUnmatchedSound() {
        soundPool.play(unmatchedSound, 1.0f, 1.0f, 1, 0, 1.0f);
    }
}
