package com.nens.springqr.encode;

public class PayloadArray {
    public char[] charArray;
    public int[] bitArray;

    public PayloadArray(String str) {
        charArray = str.toCharArray();
        int len = charArray.length;
        if (len > 13) {
            bitArray = new int[376];
            int count = 7;
            bitArray[count] = len % 2;
            len = (int) len / 2;
            count--;
            while (len > 0) {
                bitArray[count] = len % 2;
                len = (int) len / 2;
                count--;
            }
            LoadToBit(2);
        } else {
            bitArray = new int[232];
            int count = 7;
            bitArray[count] = len % 2;
            len = (int) len / 2;
            count--;
            while (len > 0) {
                bitArray[count] = len % 2;
                len = (int) len / 2;
                count--;
            }
            LoadToBit(1);
        }
    }

    public void LoadToBit(int version) {
        int byteIndex = 0;
        for (char chr : charArray) {
            int asciiInt = (int) chr;
            WriteChar(asciiInt, byteIndex);
            byteIndex++;
        }
        if (version == 2) {
            for (int i = byteIndex; i < 23; i++) {
                bitArray[8 + i * 16] = 1;
                bitArray[8 + i * 16 + 1] = 1;
                bitArray[8 + i * 16 + 2] = 1;
                bitArray[8 + i * 16 + 4] = 1;
                bitArray[8 + i * 16 + 5] = 1;
                bitArray[8 + i * 16 + 11] = 1;
                bitArray[8 + i * 16 + 15] = 1;
            }
        }
        if (version == 1) {
            for (int i = byteIndex; i < 14; i++) {
                bitArray[8 + i * 16] = 1;
                bitArray[8 + i * 16 + 1] = 1;
                bitArray[8 + i * 16 + 2] = 1;
                bitArray[8 + i * 16 + 4] = 1;
                bitArray[8 + i * 16 + 5] = 1;
                bitArray[8 + i * 16 + 11] = 1;
                bitArray[8 + i * 16 + 15] = 1;
            }
        }

    }

    public void WriteChar(int asciiInt, int byteIndex) {
        int count = 7;
        int preXor;
        bitArray[byteIndex * 16 + count + 8] = asciiInt % 2;
        preXor = asciiInt % 2;
        asciiInt = (int) asciiInt / 2;
        count--;
        while (asciiInt > 0) {
            bitArray[byteIndex * 16 + count + 8] = asciiInt % 2;
            preXor = preXor ^ (asciiInt % 2);
            asciiInt = (int) asciiInt / 2;
            count--;
        }
        for (int i = count; i > -1; i--) {
            preXor = preXor ^ 0;
        }
        bitArray[byteIndex * 16 + 7 + 8 + 8] = preXor;
    }

    public int[] getBitArray() {
        return bitArray;
    }
}
