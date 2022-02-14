package com.nens.springqr.decode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QRMapDecode {
    public String hexString;
    public int[][] qrMap32 = new int[32][32];

    public QRMapDecode(String hexInput, int[] logisticBitArray) {
        String hexInputTail = hexInput + "0";
        this.hexToBit(hexInputTail, logisticBitArray);
    }

    public void hexToBit(String hexInput, int[] logisticBitArray) {
        String hex32Bit = "";
        int endIndex;
        long int32Bits;
        Pattern pattern = Pattern.compile("x[0-9a-f]*");
        Matcher matcher = pattern.matcher(hexInput);
        int rowCount = 0;
        int colCount = 31;
        while (matcher.find()) {
            hex32Bit = matcher.group();
            endIndex = hex32Bit.length() - 1;
            hex32Bit = "0" + hex32Bit.substring(0, endIndex);
            int32Bits = Long.decode(hex32Bit);
            qrMap32[rowCount][colCount] = (int) (int32Bits % 2);
            int32Bits = int32Bits / 2;
            colCount--;
            while (int32Bits > 0) {
                qrMap32[rowCount][colCount] = (int) (int32Bits % 2);
                int32Bits = int32Bits / 2;
                colCount--;
            }
            rowCount++;
            colCount = 31;
        }

        int logisticCount = 0;
        int xorValue;
        for (int row = 0; row < 32; row++) {
            for (int col = 0; col < 32; col++) {
                xorValue = (int) (logisticBitArray[logisticCount] ^ qrMap32[row][col]);
                qrMap32[row][col] = xorValue;
                logisticCount++;
            }
        }
    }
    //(int) logisticBitArray[logisticCount]^(


    public int[][] getQrMap32() {
        return qrMap32;
    }
}
