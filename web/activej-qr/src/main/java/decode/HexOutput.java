package decode;

import utils.PairQR;

public class HexOutput {
    int[][] bitArray;
    PairQR[] zigzagPairs;

    public HexOutput(int[][] bitArray, PairQR[] zigzagPairs) {
        this.bitArray = bitArray;
        this.zigzagPairs = zigzagPairs;
    }

    public int findValue(PairQR pairQR) {
        return bitArray[pairQR.getRow()][pairQR.getCol()];
    }

    public String toString() {
        StringBuilder hexString = new StringBuilder();
        int byteInt = 0;
        char chr = 'a';
        //find the total number of char
        for (int i = 0; i < 8; i++) {
            byteInt = (byteInt * 2) + this.findValue(zigzagPairs[i]);
        }

        int charCount = byteInt;

        for (int charIndex = 0; charIndex < charCount; charIndex++) {
            byteInt = 0;
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                byteInt = (byteInt * 2) + this.findValue(zigzagPairs[bitIndex + charIndex * 16 + 8]);
            }
            chr = (char) byteInt;
            hexString.append(chr);
        }
        String hexQRString = hexString.toString();

        return hexQRString;
    }
}
