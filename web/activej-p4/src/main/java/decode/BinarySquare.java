package decode;

import qr_utils.Tuple;

import java.util.*;

public class BinarySquare {
    public int[][] data;
    int sizeX;
    int sizeY;

    public BinarySquare() {
        data = new int[32][32];
        sizeX = 32;
        sizeY = 32;
    }

    public BinarySquare(int[][] data) {
        this.data = data;
        sizeX = data.length;
        sizeY = data[0].length;
    }

    public BinarySquare(int sizeX, int sizeY) {
        data = new int[sizeX][sizeY];
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public void setSize(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public void setSize(Tuple<Integer, Integer> point) {
        setSize(point.x, point.y);
    }

    public void updateSize() {
        sizeX = data.length;
        sizeY = data[0].length;
    }

    public void initRandomData() {
        for (int xi = 0; xi < sizeX; xi++) {
            for (int yi = 0; yi < sizeY; yi++) {
                if (Math.random() >= 0.5) {
                    data[xi][yi] = 1;
                } else {
                    data[xi][yi] = 0;
                }
            }
        }
    }

    public void initDataV12WithRandom() {
        if (Math.random() >= 0.5) {
            initDataV1WithRandom();
        } else {
            initDataV2WithRandom();
        }
    }

    public void initDataV1WithRandom() {
        initRandomData();
        // pick random x,y in (3,15) as the upper left position detection center
        int x = (int) (Math.random() * 12 + 3);
        int y = (int) (Math.random() * 12 + 3);
        HashSet<Tuple<Integer, Integer>> wightArea = QRV1AllArea(new Tuple(x, y));
        shade(wightArea, 0);
        HashSet<Tuple<Integer, Integer>> shadedArea = QRV1Area(new Tuple(x, y));
        shade(shadedArea, 1);
        int[] angles = {0, 90, 180, 270};
        // pick a random angle and rotate
        rotate(angles[(int) (Math.random() * 4)]);
    }

    public void initDataV2WithRandom() {
        initRandomData();
        // pick random x,y in (3,11) as the upper left position detection center
        int x = (int) (Math.random() * 8 + 3);
        int y = (int) (Math.random() * 8 + 3);
        HashSet<Tuple<Integer, Integer>> wightArea = QRV2AllArea(new Tuple(x, y));
        shade(wightArea, 0);
        HashSet<Tuple<Integer, Integer>> shadedArea = QRV2Area(new Tuple(x, y));
        shade(shadedArea, 1);
        int[] angles = {0, 90, 180, 270};
        // pick a random angle and rotate
        rotate(angles[(int) (Math.random() * 4)]);
    }

    // input the position_detection_center, return the all shaded positions in a list
    private HashMap<String, HashSet<Tuple<Integer, Integer>>> positionDetectionArea(int x, int y) {
        HashSet<Tuple<Integer, Integer>> shadedArea = new HashSet();
        HashSet<Tuple<Integer, Integer>> wightArea = new HashSet();

        // Add center shaded area
        for (int xi = x - 1; xi < x + 2; xi++) {
            for (int yi = y - 1; yi < y + 2; yi++) {
                shadedArea.add(new Tuple(xi, yi));
            }
        }

        // Add outer shaded area
        for (int xi = x - 3; xi < x + 4; xi++) {
            // left
            shadedArea.add(new Tuple(xi, y - 3));
            // right
            shadedArea.add(new Tuple(xi, y + 3));
        }
        for (int yi = y - 3; yi < y + 4; yi++) {
            // upper
            shadedArea.add(new Tuple(x - 3, yi));
            // lower
            shadedArea.add(new Tuple(x + 3, yi));
        }

        // Add wight area
        for (int xi = x - 2; xi < x + 3; xi++) {
            // left
            wightArea.add(new Tuple(xi, y - 2));
            // right
            wightArea.add(new Tuple(xi, y + 2));
        }
        for (int yi = y - 2; yi < y + 3; yi++) {
            // upper
            wightArea.add(new Tuple(x - 2, yi));
            // lower
            wightArea.add(new Tuple(x + 2, yi));
        }

        return new HashMap<String, HashSet<Tuple<Integer, Integer>>>() {{
            put("shadedArea", shadedArea);
            put("wightArea", wightArea);
        }};
    }

    private HashMap<String, HashSet<Tuple<Integer, Integer>>> positionDetectionArea(Tuple<Integer, Integer> point) {
        return positionDetectionArea(point.x, point.y);
    }

    // check whether a point is a position detection center
    private boolean isPositionDetectionCenter(int x, int y) {
        HashMap result = positionDetectionArea(x, y);
        HashSet shadeCheckPositions = (HashSet) result.getOrDefault("shadedArea", new HashSet());
        HashSet wightCheckPositions = (HashSet) result.getOrDefault("wightArea", new HashSet());
        int shadedCount = 0;
        Iterator<Tuple<Integer, Integer>> shadeCheckPositionsIterator = shadeCheckPositions.iterator();
        while (shadeCheckPositionsIterator.hasNext()) {
            Tuple<Integer, Integer> pos = shadeCheckPositionsIterator.next();
            if (pos.x < sizeX && pos.y < sizeY) {
                if (data[pos.x][pos.y] == 1) {
                    shadedCount++;
                }
            }
        }
        if (shadedCount == 33) {
            int wightCount = 0;
            Iterator<Tuple<Integer, Integer>> wightCheckPositionsIterator = wightCheckPositions.iterator();
            while (wightCheckPositionsIterator.hasNext()) {
                Tuple<Integer, Integer> pos = wightCheckPositionsIterator.next();
                if (pos.x < sizeX && pos.y < sizeY) {
                    if (data[pos.x][pos.y] == 0) {
                        wightCount++;
                    }
                }
            }
            if (wightCount == 16) {
                return true;
            }
        }
        return false;
    }

    private boolean isPositionDetectionCenter(Tuple<Integer, Integer> point) {
        return isPositionDetectionCenter(point.x, point.y);
    }

    // input the upper left position detection center of a v1 qr code, return the all shaded positions in a list
    public HashSet<Tuple<Integer, Integer>> QRV1Area(int x, int y) {
        HashSet<Tuple<Integer, Integer>> positionDetectionCenters = new HashSet(Arrays.asList(new Tuple(x, y), new Tuple(x, y + 14), new Tuple(x + 14, y)));
        HashSet<Tuple<Integer, Integer>> totalShadedArea = new HashSet();
        for (Tuple<Integer, Integer> positionDetectionCenter : positionDetectionCenters) {
            HashMap result = positionDetectionArea(positionDetectionCenter);
            HashSet<Tuple<Integer, Integer>> shadedArea = (HashSet<Tuple<Integer, Integer>>) result.get("shadedArea");
            totalShadedArea.addAll(shadedArea);
        }
        return totalShadedArea;
    }

    public HashSet<Tuple<Integer, Integer>> QRV1Area(Tuple<Integer, Integer> upperLeftPositionDetectionCenter) {
        return QRV1Area(upperLeftPositionDetectionCenter.x, upperLeftPositionDetectionCenter.y);
    }

    public HashSet<Tuple<Integer, Integer>> QRV1AllArea(int x, int y) {
        HashSet<Tuple<Integer, Integer>> totalShadedArea = new HashSet();
        for (int xi = x - 3; xi < x + 18; xi++) {
            for (int yi = y - 3; yi < y + 18; yi++) {
                totalShadedArea.add(new Tuple(xi, yi));
            }
        }
        return totalShadedArea;
    }

    public HashSet<Tuple<Integer, Integer>> QRV1AllArea(Tuple<Integer, Integer> upperLeftPositionDetectionCenter) {
        return QRV1AllArea(upperLeftPositionDetectionCenter.x, upperLeftPositionDetectionCenter.y);
    }

    // input the upper left position detection center of a v2 qr code, return the all shaded positions in a list
    // current the alignment detection area is hide, only three position detection areas are visible
    public HashSet<Tuple<Integer, Integer>> QRV2Area(int x, int y) {
        HashSet<Tuple<Integer, Integer>> positionDetectionCenters = new HashSet(Arrays.asList(new Tuple(x, y), new Tuple(x, y + 18), new Tuple(x + 18, y)));
        //HashSet<Tuple<Integer, Integer>> alignmentDetectionCenters = new HashSet(Arrays.asList(new Tuple(x + 15, y + 15)));
        HashSet<Tuple<Integer, Integer>> totalShadedArea = new HashSet();
        for (Tuple<Integer, Integer> positionDetectionCenter : positionDetectionCenters) {
            HashMap result = positionDetectionArea(positionDetectionCenter);
            HashSet<Tuple<Integer, Integer>> shadedArea = (HashSet<Tuple<Integer, Integer>>) result.get("shadedArea");
            totalShadedArea.addAll(shadedArea);
        }
        return totalShadedArea;
    }

    public HashSet<Tuple<Integer, Integer>> QRV2Area(Tuple<Integer, Integer> upperLeftPositionDetectionCenter) {
        return QRV2Area(upperLeftPositionDetectionCenter.x, upperLeftPositionDetectionCenter.y);
    }

    public HashSet<Tuple<Integer, Integer>> QRV2AllArea(int x, int y) {
        HashSet<Tuple<Integer, Integer>> totalShadedArea = new HashSet();
        for (int xi = x - 3; xi < x + 22; xi++) {
            for (int yi = y - 3; yi < y + 22; yi++) {
                totalShadedArea.add(new Tuple(xi, yi));
            }
        }
        return totalShadedArea;
    }

    public HashSet<Tuple<Integer, Integer>> QRV2AllArea(Tuple<Integer, Integer> upperLeftPositionDetectionCenter) {
        return QRV2AllArea(upperLeftPositionDetectionCenter.x, upperLeftPositionDetectionCenter.y);
    }


    public void shade(int x, int y, int shadeItem) {
        data[x][y] = shadeItem;
    }

    public void shade(Tuple<Integer, Integer> shadeObj, int shadeItem) {
        shade(shadeObj.x, shadeObj.y, shadeItem);
    }

    public void shade(Iterable<Tuple<Integer, Integer>> shadeObjs, int shadeItem) {
        Iterator<Tuple<Integer, Integer>> it = shadeObjs.iterator();
        while (it.hasNext()) {
            Tuple<Integer, Integer> tuple = it.next();
            shade(tuple, shadeItem);
        }
    }

    public void rotate(int clockwiseAngle) {
        if (clockwiseAngle != 0) {
            if (clockwiseAngle / 90 == 1) {
                int[][] newData = new int[sizeY][sizeX];
                for (int xi = 0; xi < sizeX; xi++) {
                    for (int yi = 0; yi < sizeY; yi++) {
                        newData[yi][sizeX - xi - 1] = data[xi][yi];
                    }
                }
                data = newData;
                setSize(sizeY, sizeX);
            } else if (clockwiseAngle / 90 == 2) {
                int[][] newData = new int[sizeX][sizeY];
                for (int xi = 0; xi < sizeX; xi++) {
                    for (int yi = 0; yi < sizeY; yi++) {
                        newData[sizeX - xi - 1][sizeY - yi - 1] = data[xi][yi];
                    }
                }
                data = newData;
                setSize(sizeX, sizeY);
            } else if (clockwiseAngle / 90 == 3) {
                int[][] newData = new int[sizeY][sizeX];
                for (int xi = 0; xi < sizeX; xi++) {
                    for (int yi = 0; yi < sizeY; yi++) {
                        newData[sizeY - yi - 1][xi] = data[xi][yi];
                    }
                }
                data = newData;
                setSize(sizeY, sizeX);
            }
        } else if (clockwiseAngle == 0) {
            //System.out.println("No rotation");
        } else {
            //System.out.println("Illegal clockwiseAngle:" + clockwiseAngle);
        }
    }

    public int kToAnlge(int k) {
        if (k == 2) {
            return 180;
        } else if (k == -1) {
            return 90;
        } else if (k == 1) {
            return 270;
        } else if (k == 0) {
            return 0;
        } else {
            //System.out.println("Illegal k:" + k);
            return 0;
        }
    }

    // crop the data to select the scope in x and y
    public void crop(int startX, int endX, int startY, int endY) {
        int[][] newData = new int[endX - startX][endY - startY];
        for (int xi = startX; xi < endX; xi++) {
            for (int yi = startY; yi < endY; yi++) {
                newData[xi - startX][yi - startY] = data[xi][yi];
            }
        }
        data = newData;
        updateSize();
    }

    public int getRotateK(HashMap<String, Boolean> successDict) {
        if (successDict.getOrDefault("ul", false) == false) {
            return 2;
        }
        if (successDict.getOrDefault("ur", false) == false) {
            return -1;
        }
        if (successDict.getOrDefault("ll", false) == false) {
            return 1;
        }
        if (successDict.getOrDefault("lr", false) == false) {
            return 0;
        }
        return 0;
    }

    public HashMap findQRCode() {
        for (int trial = 0; trial < 2; trial++) {
            for (int x = 3; x < 15; x++) {
                for (int y = 3; y < 15; y++) {
                    int upperLeftValue = data[x][y];
                    if (upperLeftValue == 1) {
                        // version 1
                        if (x + 14 <= 31 && y + 14 <= 31) {
                            int successCount1 = 0;
                            int upperRightValue1 = data[x][y + 14];
                            int lowerRightValue1 = data[x + 14][y + 14];
                            int lowerLeftValue1 = data[x + 14][y];
                            HashMap<String, Boolean> successDict = new HashMap();
                            if ((upperRightValue1 + lowerRightValue1 + lowerLeftValue1) >= 2) {
                                if (isPositionDetectionCenter(x, y)) {
                                    successCount1++;
                                    successDict.put("ul", true);
                                }
                                if (isPositionDetectionCenter(x, y + 14)) {
                                    successCount1++;
                                    successDict.put("ur", true);
                                }
                                if (successCount1 >= 1) {
                                    if (isPositionDetectionCenter(x + 14, y)) {
                                        successCount1++;
                                        successDict.put("ll", true);
                                    }
                                    if (successCount1 >= 2) {
                                        if (isPositionDetectionCenter(x + 14, y + 14)) {
                                            successCount1++;
                                            successDict.put("lr", true);
                                            if (successCount1 >= 3) {
                                                //System.out.println("Found QR code!");
                                                int k = getRotateK(successDict);
                                                int rotateAngle = kToAnlge(k);
                                                //System.out.println("k:" + k + ", rotateAngle:" + rotateAngle);
                                                crop(x - 3, x + 18, y - 3, y + 18);
                                                rotate(rotateAngle);
                                                //System.out.println(this);
                                                return new HashMap() {{
                                                    put("result", data);
                                                    put("version", 1);
                                                }};
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // version 2
                        if (x + 18 <= 31 && y + 18 <= 31) {
                            int successCount2 = 0;
                            int upperRightValue2 = data[x][y + 18];
                            int lowerRightValue2 = data[x + 18][y + 18];
                            int lowerLeftValue2 = data[x + 18][y];
                            HashMap<String, Boolean> successDict = new HashMap();
                            if ((upperRightValue2 + lowerRightValue2 + lowerLeftValue2) >= 2) {
                                if (isPositionDetectionCenter(x, y)) {
                                    successCount2++;
                                    successDict.put("ul", true);
                                }
                                if (isPositionDetectionCenter(x, y + 18)) {
                                    successCount2++;
                                    successDict.put("ur", true);
                                }
                                if (successCount2 >= 1) {
                                    if (isPositionDetectionCenter(x + 18, y)) {
                                        successCount2++;
                                        successDict.put("ll", true);
                                    }
                                    if (successCount2 >= 2) {
                                        if (isPositionDetectionCenter(x + 18, y + 18)) {
                                            successCount2++;
                                            successDict.put("lr", true);
                                            if (successCount2 >= 3) {
                                                //System.out.println("Found QR code!");
                                                int k = getRotateK(successDict);
                                                int rotateAngle = kToAnlge(k);
                                                //System.out.println("k:" + k + ", rotateAngle:" + rotateAngle);
                                                crop(x - 3, x + 22, y - 3, y + 22);
                                                rotate(rotateAngle);
                                                //System.out.println(this);
                                                return new HashMap() {{
                                                    put("result", data);
                                                    put("version", 2);
                                                }};
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            rotate(90);
            //System.out.println("Rotate once after first search:");
            //System.out.println(this);
        }
        return new HashMap() {{
            put("result", new int[0][0]);
            put("version", 0);
        }};
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int xi = 0; xi < sizeX; xi++) {
            for (int yi = 0; yi < sizeY; yi++) {
                result.append(data[xi][yi]);
                result.append(" ");
            }
            result.append("\n");
        }
        return result.toString();
    }

    public static void main(String[] args) {
        BinarySquare bs = new BinarySquare();
        bs.initDataV12WithRandom();
        //System.out.println(bs);
        HashMap resultMap = bs.findQRCode();
        //System.out.println(bs);
        int version = (int) resultMap.get("version");
        int[][] data = (int[][]) resultMap.get("result");

        //System.out.println(version);
        //System.out.println(data);
    }

}
