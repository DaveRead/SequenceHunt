package com.monead.games.android.sequence.util;

import android.view.KeyEvent;

/**
 * Converts string value of a character to the keycode.
 * 
 * Created to allow definition of "key" in the strings.xml file
 * 
 * @author David Read
 * 
 */
public final class KeyCodeConverter {
    /**
     * No instances of this class should be created.
     */
    private KeyCodeConverter() {

    }

    /**
     * For any supplied letter (case insensitive), the corresponding keycode, as
     * defined in the KeyEvent class, is returned.
     * 
     * @param character
     *            The letter (A-Z)
     * 
     * @return The keycode for the letter or -1 if any other character is
     *         supplied
     */
    public static int getKeyCode(final char character) {
        switch (character) {
            case 'A':
            case 'a':
                return KeyEvent.KEYCODE_A;
            case 'B':
            case 'b':
                return KeyEvent.KEYCODE_B;
            case 'C':
            case 'c':
                return KeyEvent.KEYCODE_C;
            case 'D':
            case 'd':
                return KeyEvent.KEYCODE_D;
            case 'E':
            case 'e':
                return KeyEvent.KEYCODE_E;
            case 'F':
            case 'f':
                return KeyEvent.KEYCODE_F;
            case 'G':
            case 'g':
                return KeyEvent.KEYCODE_G;
            case 'H':
            case 'h':
                return KeyEvent.KEYCODE_H;
            case 'I':
            case 'i':
                return KeyEvent.KEYCODE_I;
            case 'J':
            case 'j':
                return KeyEvent.KEYCODE_J;
            case 'K':
            case 'k':
                return KeyEvent.KEYCODE_K;
            case 'L':
            case 'l':
                return KeyEvent.KEYCODE_L;
            case 'M':
            case 'm':
                return KeyEvent.KEYCODE_M;
            case 'N':
            case 'n':
                return KeyEvent.KEYCODE_N;
            case 'O':
            case 'o':
                return KeyEvent.KEYCODE_O;
            case 'P':
            case 'p':
                return KeyEvent.KEYCODE_P;
            case 'Q':
            case 'q':
                return KeyEvent.KEYCODE_Q;
            case 'R':
            case 'r':
                return KeyEvent.KEYCODE_R;
            case 'S':
            case 's':
                return KeyEvent.KEYCODE_S;
            case 'T':
            case 't':
                return KeyEvent.KEYCODE_T;
            case 'U':
            case 'u':
                return KeyEvent.KEYCODE_U;
            case 'V':
            case 'v':
                return KeyEvent.KEYCODE_V;
            case 'W':
            case 'w':
                return KeyEvent.KEYCODE_W;
            case 'X':
            case 'x':
                return KeyEvent.KEYCODE_X;
            case 'Y':
            case 'y':
                return KeyEvent.KEYCODE_Y;
            case 'Z':
            case 'z':
                return KeyEvent.KEYCODE_Z;
            default:
                // Unknown key - do not process
        }

        return -1;
    }
}
