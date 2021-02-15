package it.unict.dmi.mosaic;

import it.unict.dmi.srm.SRM;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;

public class BackForeSegmentationPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private JRadioButton back = new JRadioButton();
  private JRadioButton fore = new JRadioButton();
  private ButtonGroup buttonGroup1 = new ButtonGroup();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private SegmentationComponent component = new SegmentationComponent();
  private JProgressBar bar = new JProgressBar();
  private JButton fill = new JButton();
  private JLabel label = new JLabel();
  private JPanel jPanel2 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel3 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private GridLayout gridLayout2 = new GridLayout();

  private BufferedImage image, transparent;
  private int w, h;
  private int regionCount;
  private ArrayList<ActionListener> actionListener = new ArrayList<>();
  private Listener listener = new Listener();

  @SuppressWarnings("CallToPrintStackTrace")
  public BackForeSegmentationPanel() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void addActionListener(ActionListener al) {
    actionListener.add(al);
  }

  public void removeActionListener(ActionListener al) {
    actionListener.remove(al);
  }

  public void setImage(final BufferedImage im) {
    component.removeMouseListener(listener);
    component.removeMouseMotionListener(listener);
    image = null;
    transparent = null;
    component.repaint();
    setComponentEnabled(false);
    (new Thread() {
      @Override
      public void run() {
        w = im.getWidth();
        h = im.getHeight();
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        image.createGraphics().drawImage(im, 0, 0, null);
        int[] res = SRM.evaluate(image, 3, bar);
        image.setRGB(0, 0, w, h, res, 0, w);

        component.setPreferredSize(new Dimension(w, h));
        transparent = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        component.repaint();
        component.revalidate();

        regionCount = SRM.getRegionCount();
        label.setText("Remaining Regions: " + regionCount);
        setComponentEnabled(true);
        bar.setValue(0);
        bar.setString("");
        component.addMouseListener(listener);
        component.addMouseMotionListener(listener);
      }
    }).start();
  }

  public BufferedImage getBackForeImage() {
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int c = transparent.getRGB(x, y) & 0xFF;
        c = c < 64 ? 0xFF000000 : 0xFFFFFFFF;
        transparent.setRGB(x, y, c);
      }
    }

    return transparent;
  }

  private void fillRegion(Point p) {
    int colorImage = image.getRGB(p.x, p.y);
    int colorTransparent = transparent.getRGB(p.x, p.y);
    if ((colorTransparent & 0xFF000000) == 0x00000000) {
      regionCount--;
      Stack<int[]> stack = new Stack<>();
      int[] valori = new int[]{p.x, p.y, -1};
      stack.push(valori);

      while (!stack.isEmpty()) {
        valori = stack.pop();
        int x = valori[0], y = valori[1];
        if (image.getRGB(x, y) == colorImage) {
          // 0=left
          // 1=right
          // 2=top
          // 3=down

          if (back.isSelected()) {
            colorTransparent = (int) (Math.random() * 64);
          } else {
            colorTransparent = (int) (Math.random() * 64) + 192;
          }
          colorTransparent = 0xFF000000 | colorTransparent << 16 | colorTransparent << 8 | colorTransparent;
          transparent.setRGB(x, y, colorTransparent);
          if ((valori[2] != 0) && (x > 0) && (transparent.getRGB(x - 1, y) & 0xFF000000) == 0x00000000) {
            stack.push(new int[]{x - 1, y, 1});
          }
          if ((valori[2] != 1) && (valori[0] < w - 1) && (transparent.getRGB(x + 1, y) & 0xFF000000) == 0x00000000) {
            stack.push(new int[]{x + 1, y, 0});
          }
          if ((valori[2] != 2) && (valori[1] > 0) && (transparent.getRGB(x, y - 1) & 0xFF000000) == 0x00000000) {
            stack.push(new int[]{x, y - 1, 3});
          }
          if ((valori[2] != 3) && (valori[1] < h - 1) && (transparent.getRGB(x, y + 1) & 0xFF000000) == 0x00000000) {
            stack.push(new int[]{x, y + 1, 2});
          }
        }
      }
    }
    component.repaint();
  }

  private void fillAll() {
    regionCount = 0;
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int c = (y * w + x) * 100 / (w * h);
        if (c % 5 == 0) {
          bar.setValue(c);
        }

        int colorTransparent = transparent.getRGB(x, y);
        if ((colorTransparent & 0xFF000000) == 0x00000000) {
          if (back.isSelected()) {
            colorTransparent = (int) (Math.random() * 64);
          } else {
            colorTransparent = (int) (Math.random() * 64) + 192;
          }
          colorTransparent = 0xFF000000 | colorTransparent << 16 | colorTransparent << 8 | colorTransparent;
          transparent.setRGB(x, y, colorTransparent);
        }
      }
    }
    component.repaint();
  }

  private void setComponentEnabled(boolean b) {
    back.setEnabled(b);
    fore.setEnabled(b);
    fill.setEnabled(b);
  }

  private void checkFinish() {
    label.setText("Remaining Regions: " + regionCount);
    if (regionCount == 0) {
      this.fireActionPerformed();
    }
  }

  private void fireActionPerformed() {
    ActionEvent e = new ActionEvent(this, 0, "");
    Iterator<ActionListener> iter = actionListener.iterator();
    while (iter.hasNext()) {
      iter.next().actionPerformed(e);
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    back.setSelected(true);
    back.setText("Background");
    fore.setText("Foreground");
    jPanel1.setBorder(BorderFactory.createEtchedBorder());
    jPanel1.setLayout(borderLayout2);
    fill.setText("Fill Remaining Regions");
    label.setBorder(null);
    label.setText("Remaining Regions: ");
    jPanel2.setLayout(gridLayout1);
    gridLayout1.setRows(2);
    jPanel3.setLayout(gridLayout2);
    gridLayout2.setRows(2);
    jPanel2.add(fore);
    jPanel2.add(back);
    jPanel3.add(label);
    jPanel3.add(fill);
    bar.setStringPainted(true);
    this.add(bar, java.awt.BorderLayout.SOUTH);
    buttonGroup1.add(back);
    buttonGroup1.add(fore);
    jScrollPane1.getViewport().add(component, null);
    this.add(jScrollPane1, BorderLayout.CENTER);
    this.add(jPanel1, java.awt.BorderLayout.NORTH);
    jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);
    jPanel1.add(jPanel3, java.awt.BorderLayout.EAST);
    fill.addActionListener(listener);
  }

  private class SegmentationComponent extends JComponent {

    private static final long serialVersionUID = 1L;

    private SegmentationComponent() {
      this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (image != null) {
        g.drawImage(image, 0, 0, null);
      }
      if (transparent != null) {
        g.drawImage(transparent, 0, 0, null);
      }
    }
  }

  private class Listener extends MouseInputAdapter implements ActionListener {

    private Point point;
    private final ArrayList<Point> points = new ArrayList<>();

    @Override
    public void mousePressed(MouseEvent e) {
      points.clear();
      Point p = e.getPoint();
      if (0 < p.x && p.x < w && 0 < p.y && p.y < h) {
        points.add(p);
        point = p;
      }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      Point p = e.getPoint();
      if (0 < p.x && p.x < w && 0 < p.y && p.y < h) {
        points.add(p);
        Graphics g = component.getGraphics();
        g.drawLine(p.x, p.y, point.x, point.y);
        point = p;
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      component.removeMouseListener(this);
      component.removeMouseMotionListener(this);
      setComponentEnabled(false);
      bar.setIndeterminate(true);

      (new Thread() {
        @Override
        public void run() {
          for (int i = 0; i < points.size(); i++) {
            bar.setString("Point " + i + " of " + points.size());
            fillRegion(points.get(i));
          }
          bar.setString("");
          bar.setIndeterminate(false);
          setComponentEnabled(true);
          component.addMouseListener(Listener.this);
          component.addMouseMotionListener(Listener.this);
          checkFinish();
        }
      }).start();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
      component.removeMouseListener(this);
      component.removeMouseMotionListener(this);
      setComponentEnabled(false);
      (new Thread() {
        @Override
        public void run() {
          fillAll();
          setComponentEnabled(true);
          component.addMouseListener(Listener.this);
          component.addMouseMotionListener(Listener.this);
          checkFinish();
        }
      }).start();
    }
  }

  //MAIN
  /*public static void main(String a[]) throws Exception
  {
    BufferedImage buffer=javax.imageio.ImageIO.read(new java.io.File("/Users/giampo76/images/tao.jpg"));
    int w=buffer.getWidth();
    int h=buffer.getHeight();
    BufferedImage image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    image.createGraphics().drawImage(buffer,0,0,null);
    int[] res=it.unict.dmi.srm.SRM.evaluate(image,3,null);
    image.setRGB(0,0,w,h,res,0,w);
    BackForeSegmentationPanel p=new BackForeSegmentationPanel();
    p.setImage(image,it.unict.dmi.srm.SRM.getRegionCount());
    p.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {System.out.println("finito");}});
    JOptionPane.showMessageDialog(null,p);
    JOptionPane.showMessageDialog(null,new ImageIcon(p.getBackForeImage()));
    System.exit(0);
  }*/
  //FINE MAIN
}
