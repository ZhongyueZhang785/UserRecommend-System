package com.nens.springqr;

import com.nens.springqr.decode.BinarySquare;
import com.nens.springqr.decode.HexOutput;
import com.nens.springqr.decode.QRMapDecode;
import com.nens.springqr.encode.QRMap;
import com.nens.springqr.utils.LogisticBitArray;
import com.nens.springqr.utils.PairQR;
import com.nens.springqr.utils.ZigzagPair;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.util.ArrayList;

@SpringBootApplication
@RestController
public class SpringQrApplication {
    final PairQR[] zigzagPairsV2 = ZigzagPair.getZigzagPairs(2);
    final PairQR[] zigzagPairsV1 = ZigzagPair.getZigzagPairs(1);
    final int[] logisticBitArray = LogisticBitArray.getLogisticBitArray();

    @GetMapping("/")
    public String readRoot() {
        return String.format("spring-qr: Hello World!");
    }

    @GetMapping("/qrcode")
    public String qrcode(@RequestParam(value = "type") String type, @RequestParam(value = "data") String data) {
        if (type.equals("encode")) {
//            String data = "CC Team";
            if (data.length() > 13) {
                QRMap qrMap = new QRMap(data, zigzagPairsV2, logisticBitArray);
                System.out.println(qrMap.convertHex());
                return qrMap.convertHex();
            } else {
                QRMap qrMap = new QRMap(data, zigzagPairsV1, logisticBitArray);
                System.out.println(qrMap.convertHex());
                return qrMap.convertHex();
            }

        } else if (type.equals("decode")) {
//            String data = "0x2b23d6830x15a0de0d0x744784010x29e880700xfe1adf5c0xb96061290x1127b67c0x311690430xc63153140xf6e00650x92d3960b0xf59a79070x704e73d40x977fd8090xf516e98a0x3e0c19f10xac626d040x6a3e58650xca85aa3e0x6266b640x842ddcb40x4e7c879c0x85dd21240x3afae3dc0xe07908a70x664685970xb38246f70x511908330x40a111ee0xc12c8fd10x82984c520x4ddee6f6";
            QRMapDecode qrMapDecode = new QRMapDecode(data, logisticBitArray);
            int[][] data32 = qrMapDecode.qrMap32;
            BinarySquare binarySquare = new BinarySquare(data32);
            binarySquare.findQRCode();
            int[][] mapFound = binarySquare.data;
            if (mapFound.length == 21) {
                HexOutput hexOutput = new HexOutput(mapFound, zigzagPairsV1);
                System.out.println(hexOutput.toString());
                return hexOutput.toString();
            } else {
                HexOutput hexOutput = new HexOutput(mapFound, zigzagPairsV2);
                System.out.println(hexOutput.toString());
                return hexOutput.toString();
            }
        } else {
            System.out.println(String.format("Illegal request, type:%s, data:%s", type, data));
            return String.format("Illegal request, type:%s, data:%s", type, data);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringQrApplication.class, args);
    }

}
