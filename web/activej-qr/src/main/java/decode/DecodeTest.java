package decode;
import utils.*;


public class DecodeTest {
    public static void main(String[] args) {
        PairQR[] zigzagPairsV2 = ZigzagPair.getZigzagPairs(2);
        PairQR[] zigzagPairsV1 = ZigzagPair.getZigzagPairs(1);
        int[] logisticBitArray = LogisticBitArray.getLogisticBitArray();

        String text32 = "0x2b23d6830x15a0de0d0x744784010x29e880700xfe1adf5c0xb96061290x1127b67c0x311690430xc63153140xf6e00650x92d3960b0xf59a79070x704e73d40x977fd8090xf516e98a0x3e0c19f10xac626d040x6a3e58650xca85aa3e0x6266b640x842ddcb40x4e7c879c0x85dd21240x3afae3dc0xe07908a70x664685970xb38246f70x511908330x40a111ee0xc12c8fd10x82984c520x4ddee6f6";
        QRMapDecode jj = new QRMapDecode(text32, logisticBitArray);

        int[][] mapFound = {{2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0},
                {1, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0},
                {0, 1, 0, 0, 1, 0, 2, 1, 1, 1, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0, 0},
                {1, 1, 0, 0, 1, 1, 2, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0, 1, 2, 1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 1, 1},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0}};

        if (mapFound.length == 21) {
            HexOutput hexOutput = new HexOutput(mapFound, zigzagPairsV1);
            //System.out.println(hexOutput.toString());
        } else {
            HexOutput hexOutput = new HexOutput(mapFound, zigzagPairsV2);
            //System.out.println(hexOutput.toString());
        }

        /*int[][] hhhh=jj.getQrMap32();

        for(int row=0;row<32;row++){
            for(int col=0;col<32;col++){
                if(hhhh[row][col]==0){
                    System.out.print(" "+" ");
                }
                else{
                    System.out.print("X"+" ");
                }

            }
            System.out.println();
        }

        Pattern pattern = Pattern.compile("x[0-9a-f]*");
        Matcher matcher = pattern.matcher("0xfe373fc10x38106e990x8bb740050xdba002ec0x10c907fa0xaafe00090xed8880x58d9a8c40x984117080xe20000x3240020b0xbc4140080xa8600ecc0xf80019440xff902b100x48918ba00xfd5d0070x6ee861410x42a22fe0x3800");
        System.out.println(matcher.groupCount());
        while (matcher.find()) {
            System.out.println(matcher.group());
        }*/
    }
}
