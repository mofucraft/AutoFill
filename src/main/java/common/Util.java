package common;

import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Util {
    private static final String BASE64WORDS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

    public static boolean hasValidPermission(Player p, String permission){
        return p.isPermissionSet(permission) && p.hasPermission(permission);
    }

    public static String ByteToString(byte[] bytes){
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    public static byte[] StringToByte(String text){
        return Base64.getUrlDecoder().decode(text);
    }

    public static String IntToBase64NumberString(int value){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BASE64WORDS.charAt(value / 1073741824));
        value %= 1073741824;
        stringBuilder.append(BASE64WORDS.charAt(value / 16777216));
        value %= 16777216;
        stringBuilder.append(BASE64WORDS.charAt(value / 262144));
        value %= 262144;
        stringBuilder.append(BASE64WORDS.charAt(value / 4096));
        value %= 4096;
        stringBuilder.append(BASE64WORDS.charAt(value / 64));
        value %= 64;
        stringBuilder.append(BASE64WORDS.charAt(value));
        return stringBuilder.toString();
    }

    public static int Base64NumberStringToInt(String text){
        int num = 0;
        num += BASE64WORDS.indexOf(text.charAt(0)) * 1073741824;
        num += BASE64WORDS.indexOf(text.charAt(1)) * 16777216;
        num += BASE64WORDS.indexOf(text.charAt(2)) * 262144;
        num += BASE64WORDS.indexOf(text.charAt(3)) * 4096;
        num += BASE64WORDS.indexOf(text.charAt(4)) * 64;
        num += BASE64WORDS.indexOf(text.charAt(5));
        return num;
    }

    public static byte[] compress(byte[] bytes){
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        byte[] compressedBytes = new byte[bytes.length];
        int compressedLength;
        try {
            deflater.setInput(bytes);
            deflater.finish();
            compressedLength = deflater.deflate(compressedBytes);
            return Arrays.copyOf(compressedBytes, compressedLength);
        } finally {
            deflater.end();
        }
    }

    public static byte[] deCompress(byte[] bytes, int length) throws DataFormatException {
        Inflater inflater = new Inflater();
        byte[] deCompressedBytes = new byte[length];
        try {
            inflater.setInput(bytes);
            inflater.inflate(deCompressedBytes);
            return deCompressedBytes;
        } finally {
            inflater.end();
        }
    }
}
