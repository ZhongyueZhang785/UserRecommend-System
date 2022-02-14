import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;

import java.util.concurrent.Executor;

import static io.activej.http.HttpMethod.GET;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.awt.*;
import java.util.ArrayList;
import java.util.*;




public final class activejQrApplication extends HttpServerLauncher {

    @Provides
    Executor executor() {
        return newSingleThreadExecutor();
    }

    @Provides
    AsyncServlet servlet(Executor executor) {
        return RoutingServlet.create()
                .map(GET, "/qrcode", request -> {
                    String type = request.getQueryParameter("type");
                    String data = request.getQueryParameter("data");
                    final utils.PairQR[] zigzagPairsV2 = utils.ZigzagPair.getZigzagPairs(2);
                    final utils.PairQR[] zigzagPairsV1 = utils.ZigzagPair.getZigzagPairs(1);
                    final int[] logisticBitArray = utils.LogisticBitArray.getLogisticBitArray();
                    if (type.equals("encode")) {
//            String data = "CC Team";
                        if (data.length() > 13) {
                            encode.QRMap qrMap = new encode.QRMap(data, zigzagPairsV2, logisticBitArray);
                            //System.out.println(qrMap.convertHex());
                            return HttpResponse.ok200().withPlainText(qrMap.convertHex());
                        } else {
                            encode.QRMap qrMap = new encode.QRMap(data, zigzagPairsV1, logisticBitArray);
                            //System.out.println(qrMap.convertHex());
                            return HttpResponse.ok200().withPlainText(qrMap.convertHex());
                        }

                    } else if (type.equals("decode")) {
//            String data = "0x2b23d6830x15a0de0d0x744784010x29e880700xfe1adf5c0xb96061290x1127b67c0x311690430xc63153140xf6e00650x92d3960b0xf59a79070x704e73d40x977fd8090xf516e98a0x3e0c19f10xac626d040x6a3e58650xca85aa3e0x6266b640x842ddcb40x4e7c879c0x85dd21240x3afae3dc0xe07908a70x664685970xb38246f70x511908330x40a111ee0xc12c8fd10x82984c520x4ddee6f6";
                        decode.QRMapDecode qrMapDecode = new decode.QRMapDecode(data, logisticBitArray);
                        int[][] data32 = qrMapDecode.qrMap32;
                        decode.BinarySquare binarySquare = new decode.BinarySquare(data32);
                        binarySquare.findQRCode();
                        int[][] mapFound = binarySquare.data;
                        if (mapFound.length == 21) {
                            decode.HexOutput hexOutput = new decode.HexOutput(mapFound, zigzagPairsV1);
                            //System.out.println(hexOutput.toString());
                            return HttpResponse.ok200().withPlainText(hexOutput.toString());
                        } else {
                            decode.HexOutput hexOutput = new decode.HexOutput(mapFound, zigzagPairsV2);
                            //System.out.println(hexOutput.toString());
                            return HttpResponse.ok200().withPlainText(hexOutput.toString());
                        }
                    } else {
                        //System.out.println(String.format("Illegal request, type:%s, data:%s", type, data));
                        return HttpResponse.ok200().withPlainText(String.format("Illegal request, type:%s, data:%s", type, data));
                    }

                });

    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new activejQrApplication();
        launcher.launch(args);
    }
}