package qr_utils;

public class ZigzagPair {
    public static int[][] zigVersion1 = {{1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 0, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 0, 1, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 0, 1, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0},
            {2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2}};
    public static int[][] zigVersion2 = {{1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0},
            {2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 1, 2, 2, 2, 2},
            {1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 1, 0, 1, 0, 1, 2, 2, 2, 2},
            {1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 1, 2, 2, 2, 2},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2}};

    public static PairQR[] getZigzagPairs(int version) {
        PairQR[] pairQR;
        int col;
        int colLeft;
        int colRight;
        if (version == 2) {
            col = 12;
            pairQR = new PairQR[370];
            int count = 0;
            for (int zig_col = col; zig_col > 0; zig_col--) {
                if (zig_col < 4) {
                    colLeft = zig_col * 2 - 2;
                    colRight = zig_col * 2 - 1;
                } else {
                    colLeft = zig_col * 2 - 1;
                    colRight = zig_col * 2;
                }
                if (zig_col % 2 == 0) {
                    for (int row = 24; row > -1; row--) {
                        if ((zigVersion2[row][colLeft] + zigVersion2[row][colRight]) == 4) {
                            PairQR pairRight = new PairQR(row, colRight);
                            pairQR[count] = pairRight;
                            count++;
                            PairQR pairLeft = new PairQR(row, colLeft);
                            pairQR[count] = pairLeft;
                            count++;
                        }
                    }
                } else {
                    for (int row = 0; row < 25; row++) {
                        if ((zigVersion2[row][colLeft] + zigVersion2[row][colRight]) == 4) {
                            PairQR pairRight = new PairQR(row, colRight);
                            pairQR[count] = pairRight;
                            count++;
                            PairQR pairLeft = new PairQR(row, colLeft);
                            pairQR[count] = pairLeft;
                            count++;
                        }
                    }
                }
            }
        } else {
            col = 10;
            pairQR = new PairQR[224];
            int count = 0;
            for (int zig_col = col; zig_col > 0; zig_col--) {
                if (zig_col < 4) {
                    colLeft = zig_col * 2 - 2;
                    colRight = zig_col * 2 - 1;
                } else {
                    colLeft = zig_col * 2 - 1;
                    colRight = zig_col * 2;
                }
                if (zig_col % 2 == 0) {
                    for (int row = 20; row > -1; row--) {
                        if ((zigVersion1[row][colLeft] + zigVersion1[row][colRight]) == 4) {
                            PairQR pairRight = new PairQR(row, colRight);
                            pairQR[count] = pairRight;
                            count++;
                            PairQR pairLeft = new PairQR(row, colLeft);
                            pairQR[count] = pairLeft;
                            count++;

                        }
                    }
                } else {
                    for (int row = 0; row < 21; row++) {
                        if ((zigVersion1[row][colLeft] + zigVersion1[row][colRight]) == 4) {
                            PairQR pairRight = new PairQR(row, colRight);
                            pairQR[count] = pairRight;
                            count++;
                            PairQR pairLeft = new PairQR(row, colLeft);
                            pairQR[count] = pairLeft;
                            count++;
                        }
                    }
                }
            }
        }
        return pairQR;
    }


}
