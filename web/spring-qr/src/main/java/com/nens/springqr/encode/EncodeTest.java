package com.nens.springqr.encode;

import com.nens.springqr.utils.LogisticBitArray;
import com.nens.springqr.utils.PairQR;
import com.nens.springqr.utils.ZigzagPair;

import java.util.ArrayList;

public class EncodeTest {
    public static void main(String[] args) {

        PairQR[] zigzagPairsV2 = ZigzagPair.getZigzagPairs(2);
        PairQR[] zigzagPairsV1 = ZigzagPair.getZigzagPairs(1);
        int[] logisticBitArray = LogisticBitArray.getLogisticBitArray();
        ArrayList<QRMap> qrMaps = new ArrayList<>();

        String textInput = "CC Team";
        if (textInput.length() > 13) {
            QRMap qrMap = new QRMap(textInput, zigzagPairsV2, logisticBitArray);
            System.out.println(qrMap.convertHex());
        } else {
            QRMap qrMap = new QRMap(textInput, zigzagPairsV1.clone(), logisticBitArray.clone());
            qrMaps.add(qrMap);
            System.out.println(qrMap.convertHex());
        }

        if (textInput.length() > 13) {
            QRMap qrMap = new QRMap(textInput, zigzagPairsV2, logisticBitArray);
            System.out.println(qrMap.convertHex());
        } else {
            QRMap qrMap = new QRMap(textInput, zigzagPairsV1, logisticBitArray);
            qrMaps.add(qrMap);
            System.out.println(qrMap.convertHex());
        }


        /*PayloadArray jj=new PayloadArray("Test");

        int[] hhhhh=new int[2];
        System.out.println(hhhhh[1]);
        System.out.println(jj.bitArray.length);

        for(int i=0;i<8;i++){
            System.out.print(jj.bitArray[i]);
        }
        System.out.println();


        int j=16;
        for(int num=0;num<14;num++){
            j=num*16;
            for(int i=8+j;i<16+j;i++){
                System.out.print(jj.bitArray[i]);
            }
            System.out.println();
            for(int i=16+j;i<24+j;i++){
                System.out.print(jj.bitArray[i]);
            }
            System.out.println();
            System.out.println("\n"+num);
        }*/
    }
}
