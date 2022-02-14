package blockchain_utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.InflaterOutputStream;

/**
 * The {@code DecompressCompress} class can decode and encode response using
 *  zlib URL-safe Base64.For decoding, use base64 to decode the input string,
 *  then use zlib to decompress the result to obtain the JSON. For encoding,
 *  JSON is first zlib-compressed then base64-encoded

 * @author  Zhongyue Zhang
 * @version 1
 * @since 9 Oct.2021
 */



public class DecompressCompress {
    public static byte[] decompress(String compressedJson) {
        /*
        Base64 decoder and Zlib decompress
         */
        Base64.Decoder urlDecoder = Base64.getUrlDecoder();
        byte[] decoded = urlDecoder.decode(compressedJson);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (OutputStream os = new InflaterOutputStream(outputStream)) {
            os.write(decoded);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    public static byte[] compress(byte[] decompressData) {
        /*
        Zlib compress and Base64 encode
         */

        //Zlib compress
        byte[]outByte= new byte[0];
        Deflater compressor = new Deflater();
        compressor.reset();
        compressor.setInput(decompressData);
        compressor.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressData.length);
         byte[] buffer = new byte[2048];
         while (!compressor.finished()) {
                    int i = compressor.deflate(buffer);
                    outputStream.write(buffer, 0, i);
                }
         outByte= outputStream.toByteArray();
         compressor.end();


        //Base64 encode
         Base64.Decoder urlDecoder = Base64.getUrlDecoder();
         Base64.Encoder urlEncoder = Base64.getUrlEncoder();
         byte[] compressedJson = urlEncoder.encode(outByte);
         return compressedJson;
    }
}
