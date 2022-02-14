package com.nens.springqr.utils;

public class PairQR {
    public int col;
    public int row;

    public PairQR() {
        col = 0;
        row = 0;
    }

    public PairQR(int x, int y) {
        col = x;
        row = y;
    }

    public int getRow() {
        return col;
    }

    public int getCol() {
        return row;
    }
}
