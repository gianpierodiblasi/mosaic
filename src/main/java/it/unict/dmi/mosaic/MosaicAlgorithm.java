package it.unict.dmi.mosaic;

import it.unict.dmi.guideline.GuidelineDetector;
import it.unict.dmi.imageprocessing.ImageProcessing;
import it.unict.dmi.morphology.MorphologicalOperation;
import it.unict.dmi.srm.SRM;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

public class MosaicAlgorithm {

  private BufferedImage image;
  private BufferedImage edge;
  private BufferedImage mask;
  private BufferedImage erodedMask;
  private Graphics2D g2;
  private int wIm;
  private int hIm;

  private byte[] edgeMatrix;
  private int[] chain;
  private double[] distanceMatrix;
  private double[] gradientMatrix;
  private byte[] levelLineMatrix;
  private byte[] filledMatrix;
  private final ArrayList<Point> chains = new ArrayList<>();

  private int wTile;
  private int hTile;
  private Rectangle tile;

  private BufferedImage mosaic;

  private JProgressBar bar;
  private JTextArea text;
  private int experimentCount;

  private final static byte EDGE = 1;
  private final static byte CENTER = 2;

  private final static int[] ONES = {128, 64, 32, 16, 8, 4, 2, 1};

  public final static int DIRECTIONAL_GUIDELINE = 0;
  public final static int STATISTICAL_REGION_MERGING = 1;

  public final static int OPUS_MUSIVUM = 0;
  public final static int OPUS_VERMICULATUM = 1;

  public void setImage(BufferedImage image) {
    this.image = image;
  }

  public void setEdge(BufferedImage edge) {
    this.edge = edge;
  }

  public BufferedImage getImage() {
    return image;
  }

  public BufferedImage getEdge() {
    return edge;
  }

  public BufferedImage getMosaic() {
    return mosaic;
  }

  public BufferedImage getMask() {
    return mask;
  }

  public void setProgressBar(JProgressBar bar) {
    this.bar = bar;
  }

  public void setTextArea(JTextArea text) {
    this.text = text;
  }

  public void setMask(BufferedImage mask) {
    this.mask = mask;
  }

  public void evaluate(int type, Dimension tileSize, boolean scanLevelLineOrder, boolean scanLineOrder, boolean removeArc, boolean removeConcavity, int minimumCoveredPixels, int edgeDetectionTechnique, int borderTileNumber) {
    wTile = tileSize.width;
    hTile = tileSize.height;
    tile = new Rectangle(-wTile, -hTile, 2 * wTile, 2 * hTile);

    wIm = image.getWidth();
    hIm = image.getHeight();

    long startEdge = System.currentTimeMillis();
    this.edgeDetection(type, edgeDetectionTechnique, borderTileNumber);
    long stopEdge = System.currentTimeMillis();

    //START ELABORATION
    long start = System.currentTimeMillis();
    //Global Inizialization
    if (bar != null) {
      bar.setValue(0);
      bar.setString("Inizialization...");
    }
    this.init();
    //Distance
    if (bar != null) {
      bar.setString("Distance...");
    }
    this.distance();
    //Gradient
    if (bar != null) {
      bar.setString("Gradient...");
    }
    this.gradient();

    //LevelLine
    if (bar != null) {
      bar.setString("Level Line...");
    }
    this.levelLine();
    //Chains
    if (bar != null) {
      bar.setString("Chains...");
    }
    this.evaluateChains();
    //Mosaic
    if (bar != null) {
      bar.setString("Mosaic...");
    }
    this.mosaic(scanLevelLineOrder, scanLineOrder, removeArc, removeConcavity, minimumCoveredPixels);

    if (bar != null) {
      bar.setString("Background Creation...");
    }
    if (type == MosaicAlgorithm.OPUS_VERMICULATUM) {
      this.opusVermiculatum();
    }

    if (bar != null) {
      bar.setValue(0);
      bar.setString("Finished!!!");
    }
    long stop = System.currentTimeMillis();
    //STOP ELABORATION

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMinimumFractionDigits(3);
    format.setMaximumFractionDigits(3);
    experimentCount++;
    if (text != null) {
      text.insert("--------------------------------\n", 0);
      text.insert("\tTotal Elapsed Time                  = " + format.format((stopEdge - startEdge + stop - start) / 1000.0) + " seconds\n", 0);
      text.insert("\tElapsed Time for Edge Detection     = " + format.format((stopEdge - startEdge) / 1000.0) + " seconds\n", 0);
      text.insert("\tElapsed Time for Mosaic Creation    = " + format.format((stop - start) / 1000.0) + " seconds\n", 0);
      text.insert("Results\n", 0);
      text.insert("\tMinumum Covered Pixels              = " + minimumCoveredPixels + "\n", 0);
      text.insert("\tRemove Concavity                    = " + removeConcavity + "\n", 0);
      text.insert("\tRemove Arc                          = " + removeArc + "\n", 0);
      text.insert("\tScan Line Order                     = " + scanLineOrder + "\n", 0);
      text.insert("\tScan Level Line Order               = " + scanLevelLineOrder + "\n", 0);
      text.insert("\tHeight                              = " + tile.height + "\n", 0);
      text.insert("\tWidth                               = " + tile.width + "\n", 0);
      text.insert("Data\n", 0);
      text.insert("*** Experiment N. " + experimentCount + " ***\n", 0);
    }
  }

  private void edgeDetection(int type, int edgeDetectionTechnique, int borderTileNumber) {
    edgeMatrix = new byte[wIm * hIm / 8 + 1];

    if (edge == null || edge.getWidth() != wIm || edge.getHeight() != hIm) {
      switch (edgeDetectionTechnique) {
        case MosaicAlgorithm.DIRECTIONAL_GUIDELINE:
          edge = GuidelineDetector.evaluate(image, bar);
          break;
        case MosaicAlgorithm.STATISTICAL_REGION_MERGING:
          int[] res = SRM.evaluate(image, 3, bar);
          edge = ImageProcessing.evaluateEdgesFromSegmentation(res, wIm, hIm);
      }
    }

    if (mask != null) {
      if (borderTileNumber > 0) {
        erodedMask = MorphologicalOperation.erode(mask, false, 4 * borderTileNumber * wTile);
      } else {
        erodedMask = mask;
      }
    } else {
      erodedMask = null;
    }

    for (int x = 0; x < wIm; x++) {
      for (int y = 0; y < hIm; y++) {
        if (type == MosaicAlgorithm.OPUS_VERMICULATUM && mask != null && erodedMask.getRGB(x, y) == 0xFF000000) {
          set(edgeMatrix, y * wIm + x);
        } else {
          int color = edge.getRGB(x, y);
          if ((color & 0xFF) == 0) {
            set(edgeMatrix, y * wIm + x);
          }
        }
      }
    }
  }

  private void init() {
    chain = new int[wIm];
    Arrays.fill(chain, -1);
    distanceMatrix = new double[wIm * hIm];
    Arrays.fill(distanceMatrix, Double.MAX_VALUE);
    gradientMatrix = new double[wIm * hIm];
    filledMatrix = new byte[wIm * hIm / 8 + 1];
    System.arraycopy(edgeMatrix, 0, filledMatrix, 0, edgeMatrix.length);
    levelLineMatrix = new byte[wIm * hIm];
    chains.clear();
    mosaic = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
    g2 = mosaic.createGraphics();
    g2.drawImage(image, 0, 0, null);
    g2.setComposite(AlphaComposite.SrcIn);
    g2.setColor(Color.gray);
    g2.fillRect(0, 0, wIm, hIm);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
  }

  private void distance() {
    for (int y = 0; y < hIm; y++) {
      int v = 50 * y / hIm;
      if (v % 5 == 0 && bar != null) {
        bar.setValue(v);
      }
      this.distance(y);
    }
    Arrays.fill(chain, -1);
    for (int y = hIm - 1; y >= 0; y--) {
      int v = 50 + 50 * (hIm - 1 - y) / hIm;
      if (v % 5 == 0 && bar != null) {
        bar.setValue(v);
      }
      this.distance(y);
    }
  }

  private void distance(int y) {
    for (int x = 0; x < wIm; x++) {
      int c = y * wIm + x;
      if (is(edgeMatrix, c)) {
        chain[x] = y;
        distanceMatrix[c] = 0;
      }
    }
    for (int x = 0; x < wIm; x++) {
      int c = x + wIm * y;
      if (!is(edgeMatrix, c)) {
        int xx = 1;
        boolean stop = false;
        while (x - xx >= 0 && !stop) {
          if (chain[x - xx] != -1) {
            double dd = Point.distance(x, y, x - xx, chain[x - xx]);
            if (dd < distanceMatrix[c]) {
              distanceMatrix[c] = dd;
            }
            stop = xx + 1 >= distanceMatrix[c];
          }
          xx++;
        }
        xx = 1;
        stop = false;
        while (x + xx < wIm && !stop) {
          if (chain[x + xx] != -1) {
            double dd = Point.distance(x, y, x + xx, chain[x + xx]);
            if (dd < distanceMatrix[c]) {
              distanceMatrix[c] = dd;
            }
            stop = xx + 1 >= distanceMatrix[c];
          }
          xx++;
        }
      }
    }
  }

  private void gradient() {
    for (int y = 0; y < hIm; y++) {
      for (int x = 0; x < wIm; x++) {
        int c = x + y * wIm;
        int v = 100 * c / (wIm * hIm);
        if (v % 5 == 0 && bar != null) {
          bar.setValue(v);
        }
        double dx;
        double dy;
        if (x > 0 && x < wIm - 1) {
          dx = distanceMatrix[c + 1] - distanceMatrix[c - 1];
        } else if (x > 0) {
          dx = distanceMatrix[c] - distanceMatrix[c - 1];
        } else {
          dx = distanceMatrix[c + 1] - distanceMatrix[c];
        }
        if (y > 0 && y < hIm - 1) {
          dy = distanceMatrix[c + wIm] - distanceMatrix[c - wIm];
        } else if (y > 0) {
          dy = distanceMatrix[c] - distanceMatrix[c - wIm];
        } else {
          dy = distanceMatrix[c + wIm] - distanceMatrix[c];
        }
        gradientMatrix[c] = Math.atan2(dy, dx);
        if (gradientMatrix[c] < 0) {
          gradientMatrix[c] += 2 * Math.PI;
        }
      }
    }
  }

  private void levelLine() {
    for (int y = 0; y < hIm; y++) {
      for (int x = 0; x < wIm; x++) {
        int c = x + y * wIm;
        int v = 100 * c / (wIm * hIm);
        if (v % 5 == 0 && bar != null) {
          bar.setValue(v);
        }
        int r = (int) distanceMatrix[c] % (2 * wTile);
        if (r == 0) {
          levelLineMatrix[c] = MosaicAlgorithm.EDGE;
        } else if (r == wTile) {
          levelLineMatrix[c] = MosaicAlgorithm.CENTER;
        }
      }
    }
  }

  private void evaluateChains() {
    boolean[] marked = new boolean[levelLineMatrix.length];
    for (int x = 0; x < wIm; x++) {
      for (int y = 0; y < hIm; y++) {
        int c = y * wIm + x;
        int v = 100 * c / (wIm * hIm);
        if (v % 5 == 0 && bar != null) {
          bar.setValue(v);
        }
        if (!marked[c] && levelLineMatrix[c] == MosaicAlgorithm.CENTER) {
          ArrayList<Point> points = new ArrayList<>();
          this.followChain(marked, points, x, y);
          chains.addAll(points);
        }
      }
    }
  }

  private void followChain(boolean[] marked, ArrayList<Point> points, int x, int y) {
    Point p = new Point(x, y);
    while (p != null) {
      points.add(p);
      int c = p.y * wIm + p.x;
      marked[c] = true;
      if (p.x > 0 && levelLineMatrix[c - 1] == MosaicAlgorithm.CENTER && !marked[c - 1]) {
        p = new Point(p.x - 1, p.y);
      } else if (p.x < wIm - 1 && levelLineMatrix[c + 1] == MosaicAlgorithm.CENTER && !marked[c + 1]) {
        p = new Point(p.x + 1, p.y);
      } else if (p.x > 0 && p.y < hIm - 1 && levelLineMatrix[c - 1 + wIm] == MosaicAlgorithm.CENTER && !marked[c - 1 + wIm]) {
        p = new Point(p.x - 1, p.y + 1);
      } else if (p.x < wIm - 1 && p.y > 0 && levelLineMatrix[c + 1 - wIm] == MosaicAlgorithm.CENTER && !marked[c + 1 - wIm]) {
        p = new Point(p.x + 1, p.y - 1);
      } else if (p.y > 0 && levelLineMatrix[c - wIm] == MosaicAlgorithm.CENTER && !marked[c - wIm]) {
        p = new Point(p.x, p.y - 1);
      } else if (p.y < hIm - 1 && levelLineMatrix[c + wIm] == MosaicAlgorithm.CENTER && !marked[c + wIm]) {
        p = new Point(p.x, p.y + 1);
      } else if (p.x > 0 && p.y > 0 && levelLineMatrix[c - 1 - wIm] == MosaicAlgorithm.CENTER && !marked[c - 1 - wIm]) {
        p = new Point(p.x - 1, p.y - 1);
      } else if (p.x < wIm - 1 && p.y < hIm - 1 && levelLineMatrix[c + 1 + wIm] == MosaicAlgorithm.CENTER && !marked[c + 1 + wIm]) {
        p = new Point(p.x + 1, p.y + 1);
      } else {
        p = null;
      }
    }
  }

  private Shape[] findWithCut(int x, int y, byte[] matrix, boolean removeArc, boolean removeConcavity, int minimumCoveredPixels) {
    Rectangle r = this.findShape(x, y, matrix);
    return this.convertMatrixToShapes(matrix, r, x, y, removeArc, removeConcavity, minimumCoveredPixels);
  }

  private Rectangle findShape(int x, int y, byte[] matrix) {
    Stack<int[]> stack = new Stack<>();
    int[] values = new int[]{x, y, -1};
    stack.push(values);

    // 0=left
    // 1=right
    // 2=top
    // 3=down
    int minX = wIm;
    int minY = hIm;
    int maxX = -1;
    int maxY = -1;

    AffineTransform tx = AffineTransform.getRotateInstance(gradientMatrix[y * wIm + x]);
    tx.preConcatenate(AffineTransform.getTranslateInstance(x, y));
    Shape shape = tx.createTransformedShape(tile);

    while (!stack.isEmpty()) {
      values = stack.pop();
      x = values[0];
      y = values[1];
      int uselessDirection = values[2];
      int coord = y * wIm + x;
      if (!is(filledMatrix, coord) && shape.contains(x, y)) {
        if (x < minX) {
          minX = x;
        }
        if (x > maxX) {
          maxX = x;
        }
        if (y < minY) {
          minY = y;
        }
        if (y > maxY) {
          maxY = y;
        }
        set(matrix, coord);
        if (levelLineMatrix[coord] != MosaicAlgorithm.EDGE) {
          if ((uselessDirection != 0) && x > 0 && (!is(matrix, coord - 1))) {
            stack.push(new int[]{x - 1, y, 1});
          }
          if ((uselessDirection != 1) && x < wIm - 1 && (!is(matrix, coord + 1))) {
            stack.push(new int[]{x + 1, y, 0});
          }
          if ((uselessDirection != 2) && y > 0 && (!is(matrix, coord - wIm))) {
            stack.push(new int[]{x, y - 1, 3});
          }
          if ((uselessDirection != 3) && y < hIm - 1 && (!is(matrix, coord + wIm))) {
            stack.push(new int[]{x, y + 1, 2});
          }
        }
      }
    }
    return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
  }

  private Shape[] convertMatrixToShapes(byte[] matrix, Rectangle r, int x, int y, boolean removeArc, boolean removeConcavity, int minimumCoveredPixels) {
    Area area = new Area();
    int xx = r.x + r.width;
    int yy = r.y + r.height;
    for (int xxx = r.x; xxx < xx; xxx++) {
      for (int yyy = r.y; yyy < yy; yyy++) {
        int c = yyy * wIm + xxx;
        if (is(matrix, c)) {
          int ww = 0;
          while (xxx + ww < xx && is(matrix, c + ww)) {
            reset(matrix, c + ww);
            ww++;
          }
          area.add(new Area(new Rectangle(xxx, yyy, ww, 1)));
        }
      }
    }

    Polygon polygon = this.convertAreaInPolygon(area, removeArc);

    Shape[] shapes = removeConcavity ? this.removeConcavity(polygon, x, y) : new Shape[]{polygon};
    for (int i = 0; i < shapes.length; i++) {
      Rectangle rect = shapes[i].getBounds();
      AffineTransform tx = AffineTransform.getTranslateInstance(-rect.x - rect.width / 2, -rect.y - rect.height / 2);
      tx.preConcatenate(AffineTransform.getScaleInstance(0.9, 0.9));
      tx.preConcatenate(AffineTransform.getTranslateInstance(rect.x + rect.width / 2, rect.y + rect.height / 2));
      shapes[i] = tx.createTransformedShape(shapes[i]);
    }

    int coveredPixels = 0;
    for (int xxx = r.x; xxx < xx; xxx++) {
      for (int yyy = r.y; yyy < yy; yyy++) {
        for (Shape shape : shapes) {
          if (shape.contains(xxx, yyy)) {
            coveredPixels++;
          }
        }
      }
    }

    if (coveredPixels >= minimumCoveredPixels) {
      for (int xxx = r.x; xxx < xx; xxx++) {
        for (int yyy = r.y; yyy < yy; yyy++) {
          if (polygon.contains(xxx, yyy)) {
            set(filledMatrix, yyy * wIm + xxx);
          }
        }
      }
      return shapes;
    } else {
      return null;
    }
  }

  private Polygon convertAreaInPolygon(Area area, boolean removeArc) {
    PathIterator iter = area.getPathIterator(null);
    float[] data = new float[6];
    ArrayList<Point> p = new ArrayList<>();
    while (!iter.isDone()) {
      int type = iter.currentSegment(data);
      if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
        p.add(new Point((int) data[0], (int) data[1]));
      }
      iter.next();
    }
    Point[] points = new Point[p.size()];
    p.toArray(points);

    Polygon polygon = new Polygon();
    if (!removeArc || points.length <= 7) {
      for (Point point : points) {
        polygon.addPoint(point.x, point.y);
      }
    } else {
      for (int i = 0; i < points.length; i++) {
        boolean add = true;
        for (int step = 1; step <= 3 && add; step++) {
          double alfa = this.findAlfa(points, i, step);
          if (alfa > 7 * Math.PI / 9) {
            add = false; //140 degree (136 minimum)
          }
        }
        if (add) {
          polygon.addPoint(points[i].x, points[i].y);
        }
      }
    }
    return polygon;
  }

  private double findAlfa(Point[] points, int index, int step) {
    int prev = (index < step ? points.length + index - step : index - step);
    int next = (index > points.length - step - 1 ? index - points.length + step : index + step);
    double b = points[prev].distance(points[index]);
    double a = points[next].distance(points[index]);
    double c = points[prev].distance(points[next]);
    return Math.acos((a * a + b * b - c * c) / (2 * a * b));
  }

  private Shape[] removeConcavity(Polygon polygon, int x, int y) {
    ArrayList<Point> concaveVertices = this.findConcaveVertices(polygon);
    if (concaveVertices.isEmpty()) {
      return new Shape[]{polygon};
    } else {
      ArrayList<Shape> shapes = new ArrayList<>();
      double alfa = gradientMatrix[y * wIm + x];
      if (alfa >= Math.PI) {
        alfa -= Math.PI;
      }
      AffineTransform tx = AffineTransform.getRotateInstance(alfa);
      Area area = new Area(polygon);
      Rectangle rect = area.getBounds();
      int dim = Math.max(rect.width, rect.height);
      Rectangle rectShadow = new Rectangle(-dim - 1, -2 * dim - 1, 2 * dim, 2 * dim);
      Shape shapeShadow1 = tx.createTransformedShape(rectShadow);
      rectShadow = new Rectangle(-dim, 0, 2 * dim, 2 * dim);
      Shape shapeShadow2 = tx.createTransformedShape(rectShadow);
      this.recursiveRemoveConcavity(concaveVertices, shapes, area, shapeShadow1, shapeShadow2);
      Shape[] s = new Shape[shapes.size()];
      shapes.toArray(s);
      return s;
    }
  }

  private void recursiveRemoveConcavity(ArrayList<Point> concaveVertices, ArrayList<Shape> shapes, Area area, Shape shapeShadow1, Shape shapeShadow2) {
    if (concaveVertices.isEmpty()) {
      shapes.add(area);
    } else {
      Point vertex = concaveVertices.get(0);
      Area area1 = (Area) area.clone();
      AffineTransform tx2 = AffineTransform.getTranslateInstance(vertex.x, vertex.y);
      Shape shapeShadow = tx2.createTransformedShape(shapeShadow1);
      area1.intersect(new Area(shapeShadow));
      shapeShadow = tx2.createTransformedShape(shapeShadow2);
      area.intersect(new Area(shapeShadow));

      ArrayList<Point> cv1 = new ArrayList<>(concaveVertices.size());
      ArrayList<Point> cv2 = new ArrayList<>(concaveVertices.size());
      for (int i = 1; i < concaveVertices.size(); i++) {
        vertex = concaveVertices.get(i);
        if (area1.contains(vertex)) {
          cv1.add(vertex);
        } else {
          cv2.add(vertex);
        }
      }
      this.recursiveRemoveConcavity(cv1, shapes, area1, shapeShadow1, shapeShadow2);
      this.recursiveRemoveConcavity(cv2, shapes, area, shapeShadow1, shapeShadow2);
    }
  }

  private ArrayList<Point> findConcaveVertices(Polygon polygon) {
    ArrayList<Point> concaveVertices = new ArrayList<>();
    for (int index = 0; index < polygon.npoints; index++) {
      int prev = (index == 0 ? polygon.npoints - 1 : index - 1);
      int next = (index == polygon.npoints - 1 ? 0 : index + 1);
      if (Line2D.relativeCCW(polygon.xpoints[prev], polygon.ypoints[prev], polygon.xpoints[next], polygon.ypoints[next], polygon.xpoints[index], polygon.ypoints[index]) > 0) {
        concaveVertices.add(new Point(polygon.xpoints[index], polygon.ypoints[index]));
      }
    }
    return concaveVertices;
  }

  private void mosaic(boolean scanLevelLineOrder, boolean scanLineOrder, boolean removeArc, boolean removeConcavity, int minimumCoveredPixels) {
    byte[] matrix = new byte[wIm * hIm / 8 + 1];
    if (scanLevelLineOrder) {
      this.mosaicScanLevelLine(matrix, removeArc, removeConcavity, minimumCoveredPixels, g2);
    }
    if (scanLineOrder) {
      this.mosaicScanLine(matrix, removeArc, removeConcavity, minimumCoveredPixels, g2);
    }
    g2.dispose();
  }

  private void mosaicScanLevelLine(byte[] matrix, boolean removeArc, boolean removeConcavity, int minimumCoveredPixels, Graphics2D g2) {
    int step = 2 * wTile;
    int size = chains.size();
    for (int i = 0; i < size; i += step) {
      Point point = chains.get(i);
      this.addNextTile(matrix, point.x, point.y, 100 * i / size, removeArc, removeConcavity, minimumCoveredPixels, g2);
    }
  }

  private void mosaicScanLine(byte[] matrix, boolean removeArc, boolean removeConcavity, int minimumCoveredPixels, Graphics2D g2) {
    for (int y = 0; y < hIm; y++) {
      for (int x = 0; x < wIm; x++) {
        int c = y * wIm + x;
        this.addNextTile(matrix, x, y, 100 * c / (wIm * hIm), removeArc, removeConcavity, minimumCoveredPixels, g2);
      }
    }
  }

  private void addNextTile(byte[] matrix, int x, int y, int v, boolean removeArc, boolean removeConcavity, int minimumCoveredPixels, Graphics2D g2) {
    if (v % 5 == 0 && bar != null) {
      bar.setValue(v);
    }
    int color = image.getRGB(x, y);
    if (!is(filledMatrix, y * wIm + x) && (color & 0xFF000000) != 0) {
      Shape[] shapes = this.findWithCut(x, y, matrix, removeArc, removeConcavity, minimumCoveredPixels);
      if (shapes != null) {
        for (Shape shape : shapes) {
          if (shape != null) {
            g2.setPaint(new Color(color));
            g2.fill(shape);
          }
        }
      }
    }
  }

  private void set(byte[] matrix, int pos) {
    matrix[pos / 8] |= MosaicAlgorithm.ONES[pos % 8];
  }

  private boolean is(byte[] matrix, int pos) {
    return (matrix[pos / 8] & MosaicAlgorithm.ONES[pos % 8]) != 0;
  }

  private void reset(byte[] matrix, int pos) {
    matrix[pos / 8] &= ~MosaicAlgorithm.ONES[pos % 8];
  }

  private void opusVermiculatum() {
    BufferedImage back = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
    Graphics2D gBack = back.createGraphics();
    gBack.setColor(Color.gray);
    gBack.fillRect(0, 0, wIm, hIm);
    gBack.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    gBack.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    int stepX = 2 * hTile;
    int stepY = 2 * wTile;

    for (int y = 0; y < hIm; y += stepY) {
      for (int x = 0; x < wIm;) {
        int c = y * wIm + x;
        if (c % 5 == 0 && bar != null) {
          bar.setValue(c);
        }
        int stepXX = stepX / 2 + (int) (stepX / 2 * Math.random());
        int stepYY = stepY - (int) (Math.random() * 2);
        int color = image.getRGB(x, y);
        int xNext = x + stepXX - 1;
        if (xNext >= wIm) {
          xNext = wIm - 1;
        }
        int yNext = y + stepYY - 1;
        if (yNext >= hIm) {
          yNext = hIm - 1;
        }
        int colorNextX = image.getRGB(xNext, y);
        int colorNextY = image.getRGB(x, yNext);
        int colorNextXY = image.getRGB(xNext, yNext);
        boolean ok = false;

        if (erodedMask.getRGB(x, y) == 0xFF000000 && (color & 0xFF000000) != 0) {
          ok = true;
        } else if (erodedMask.getRGB(xNext, y) == 0xFF000000 && (colorNextX & 0xFF000000) != 0) {
          ok = true;
          color = colorNextX;
        } else if (erodedMask.getRGB(x, yNext) == 0xFF000000 && (colorNextY & 0xFF000000) != 0) {
          ok = true;
          color = colorNextY;
        } else if (erodedMask.getRGB(xNext, yNext) == 0xFF000000 && (colorNextXY & 0xFF000000) != 0) {
          ok = true;
          color = colorNextXY;
        }

        if (ok) {
          double sX = stepXX * .9;
          double sY = stepYY * .9;

          double xTranslation = x + sX / 2;
          double yTranslation = y + Math.random() * (stepY - stepYY) + sY / 2;
          double angle = Math.random() * Math.PI / 32 - Math.PI / 64;

          gBack.translate(xTranslation, yTranslation);
          gBack.rotate(angle);

          Rectangle2D rect = new Rectangle2D.Double(-sX / 2, -sY / 2, sX, sY);
          gBack.setColor(new Color(color));
          gBack.fill(rect);

          gBack.rotate(-angle);
          gBack.translate(-xTranslation, -yTranslation);

          x += stepXX;
        } else {
          x++;
        }
      }
    }

    gBack.dispose();

    for (int y = 0; y < hIm; y++) {
      for (int x = 0; x < wIm; x++) {
        int c = y * wIm + x;
        if (c % 5 == 0 && bar != null) {
          bar.setValue(c);
        }
        if (erodedMask.getRGB(x, y) == 0xFF000000) {
          mosaic.setRGB(x, y, back.getRGB(x, y));
        }
      }
    }
  }
}
