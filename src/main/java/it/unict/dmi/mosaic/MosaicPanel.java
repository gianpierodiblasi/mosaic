package it.unict.dmi.mosaic;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

public class MosaicPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private JButton open = new JButton();
  private JMenuItem openImage = new JMenuItem();
  private JMenuItem openEdge = new JMenuItem();
  private JMenuItem openMask = new JMenuItem();
  private JButton remove = new JButton();
  private JMenuItem removeEdge = new JMenuItem();
  private JMenuItem removeMask = new JMenuItem();
  private JButton create = new JButton();
  private JButton save = new JButton();
  private JMenuItem saveImage = new JMenuItem();
  private JMenuItem saveEdge = new JMenuItem();
  private JMenuItem saveMask = new JMenuItem();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JLabel image = new JLabel();
  private JLabel mosaic = new JLabel();
  private JFileChooser openChooser = new JFileChooser();
  private JFileChooser saveChooser = new JFileChooser();
  private JProgressBar bar = new JProgressBar();
  private JLabel edge = new JLabel();
  private JPanel jPanel4 = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JDialog dialog = new JDialog();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JLabel preview = new JLabel();
  private JPanel jPanel2 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private JSplitPane jSplitPane1 = new JSplitPane();
  private JScrollPane jScrollPane3 = new JScrollPane();
  private JTextArea messages = new JTextArea();
  private JPanel jPanel11 = new JPanel();
  private JPanel jPanel12 = new JPanel();
  private BorderLayout borderLayout5 = new BorderLayout();
  private GridLayout gridLayout8 = new GridLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private SettingPanel settingPanel = new SettingPanel();
  private JPopupMenu menuOpen = new JPopupMenu();
  private JPopupMenu menuRemove = new JPopupMenu();
  private JPopupMenu menuSave = new JPopupMenu();
  private JDialog dialogMask = new JDialog();
  private BackForeSegmentationPanel backForePanel = new BackForeSegmentationPanel();
  private JScrollPane jScrollPane4 = new JScrollPane();
  private JLabel mask = new JLabel();

  private MosaicAlgorithm ma = new MosaicAlgorithm();
  private int dim = 120;
  private final static boolean MAC_OS_X = System.getProperty("os.name").toLowerCase().startsWith("mac os x");

  private final static int IMAGE = 0;
  private final static int EDGE = 1;
  private final static int MASK = 2;

  @SuppressWarnings("CallToPrintStackTrace")
  public MosaicPanel() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
    ma.setProgressBar(bar);
    ma.setTextArea(messages);
  }

  private ImageIcon createIcon(BufferedImage im) {
    BufferedImage icon = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
    int w = im.getWidth();
    int h = im.getHeight();
    double scale = Math.min((double) dim / w, (double) dim / h);
    Graphics2D g2 = icon.createGraphics();
    g2.scale(scale, scale);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2.drawImage(im, 0, 0, null);
    g2.dispose();
    return new ImageIcon(icon);
  }

  @SuppressWarnings("UnusedAssignment")
  private void open(int type) {
    openChooser.setSelectedFile(null);
    if (openChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      try {
        FileImageInputStream fImm = new FileImageInputStream(openChooser.getSelectedFile());
        Iterator<ImageReader> iter = ImageIO.getImageReaders(fImm);
        if (iter.hasNext()) {
          ImageReader reader = iter.next();
          reader.setInput(fImm);
          int w = reader.getWidth(0);
          int h = reader.getHeight(0);
//          if (w*h<=1960000)
          {
            BufferedImage im = reader.read(0);
            if (MosaicPanel.MAC_OS_X) {
              int[] data = new int[w * h];
              im.getRGB(0, 0, w, h, data, 0, w);
              im = null;
              System.gc();
              im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
              im.setRGB(0, 0, w, h, data, 0, w);
            }

            switch (type) {
              case IMAGE:
                ma.setImage(im);
                image.setIcon(this.createIcon(im));
                image.setCursor(new Cursor(Cursor.HAND_CURSOR));
                image.setToolTipText("Click to enlarge");
                create.setEnabled(true);
                break;
              case EDGE:
                ma.setEdge(im);
                edge.setIcon(this.createIcon(im));
                edge.setCursor(new Cursor(Cursor.HAND_CURSOR));
                edge.setToolTipText("Click to enlarge");
                break;
              case MASK:
                ma.setMask(im);
                mask.setIcon(this.createIcon(im));
                mask.setCursor(new Cursor(Cursor.HAND_CURSOR));
                mask.setToolTipText("Click to enlarge");
                break;
            }
          }
//          else JOptionPane.showMessageDialog(this,"It is not possible to open the file\nThe image size is greater than 1960000 pixel","Error",JOptionPane.ERROR_MESSAGE);
          reader.setInput(null);
        }
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "It is not possible to open the file", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void save(BufferedImage image) {
    File f = this.checkFile(saveChooser);
    if (f != null)
      try {
      ImageIO.write(image, saveChooser.getFileFilter().toString(), f);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "It's not possible to save the file", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private File checkFile(JFileChooser chooser) {
    chooser.setSelectedFile(null);
    int res2 = JOptionPane.NO_OPTION;
    File f = null;
    while (res2 == JOptionPane.NO_OPTION) {
      int res = chooser.showSaveDialog(this);
      if (res == JFileChooser.APPROVE_OPTION) {
        f = chooser.getSelectedFile();
        if (f.exists()) {
          res2 = JOptionPane.showConfirmDialog(this, "The file already exists, overwrite?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        } else {
          res2 = JOptionPane.YES_OPTION;
        }
      } else {
        res2 = JOptionPane.CANCEL_OPTION;
      }
    }
    return res2 == JOptionPane.YES_OPTION ? f : null;

  }

  private void setComponentsEnabled(boolean b) {
    open.setEnabled(b);
    remove.setEnabled(b);
    create.setEnabled(b);
    save.setEnabled(b);
    settingPanel.setComponentsEnabled(b);
  }

  private void jbInit() throws Exception {
    openChooser.setAcceptAllFileFilterUsed(false);
    openChooser.setFileFilter(new OpenImageFilter());
//    openChooser.setCurrentDirectory(new File("/Users/giampo76/Images/"));
    saveChooser.setAcceptAllFileFilterUsed(false);
    saveChooser.setFileFilter(new SaveImageFilter());
//    saveChooser.setCurrentDirectory(new File("/Users/giampo76/Images/"));
    open.setFocusPainted(false);
    open.setMargin(new Insets(0, 0, 0, 0));
    open.setToolTipText("Open");
    open.setIcon(new ImageIcon(ImageIO.read(MosaicPanel.class.getClassLoader().getResourceAsStream("open.gif"))));
    remove.setFocusPainted(false);
    remove.setMargin(new Insets(0, 0, 0, 0));
    remove.setToolTipText("Remove");
    remove.setIcon(new ImageIcon(ImageIO.read(MosaicPanel.class.getClassLoader().getResourceAsStream("remove.gif"))));
    create.setEnabled(false);
    create.setFocusPainted(false);
    create.setMargin(new Insets(0, 0, 0, 0));
    create.setToolTipText("Create");
    create.setIcon(new ImageIcon(ImageIO.read(MosaicPanel.class.getClassLoader().getResourceAsStream("start.gif"))));
    save.setEnabled(false);
    save.setFocusPainted(false);
    save.setMargin(new Insets(0, 0, 0, 0));
    save.setToolTipText("Save");
    save.setIcon(new ImageIcon(ImageIO.read(MosaicPanel.class.getClassLoader().getResourceAsStream("save.gif"))));
    openImage.setText("Image");
    openEdge.setText("Edge");
    openMask.setText("Mask");
    removeEdge.setText("Edge");
    removeMask.setText("Mask");
    saveImage.setText("Mosaic");
    saveEdge.setText("Directional Guidelines");
    saveMask.setText("Mask");
    saveMask.setEnabled(false);
    menuOpen.add(openImage);
    menuOpen.add(openEdge);
    menuOpen.add(openMask);
    menuRemove.add(removeEdge);
    menuRemove.add(removeMask);
    menuSave.add(saveImage);
    menuSave.add(saveEdge);
    menuSave.add(saveMask);
    image.setBorder(BorderFactory.createEtchedBorder());
    image.setPreferredSize(new Dimension(dim, dim));
    edge.setBorder(BorderFactory.createEtchedBorder());
    edge.setPreferredSize(new Dimension(dim, dim));
    mask.setBorder(BorderFactory.createEtchedBorder());
    mask.setPreferredSize(new Dimension(dim, dim));
    mosaic.setVerticalAlignment(SwingUtilities.NORTH);
    jPanel4.setLayout(borderLayout3);
    dialog.setSize(500, 500);
    dialog.setTitle("Artificial Mosaic Creator");
    dialogMask.setSize(500, 500);
    dialogMask.setModal(true);
    dialogMask.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    dialogMask.setTitle("Artificial Mosaic Creator");
    jPanel2.setLayout(gridLayout1);
    gridLayout1.setRows(3);
    jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
    jSplitPane1.setLastDividerLocation(360);
    jSplitPane1.setOneTouchExpandable(true);
    messages.setFont(new java.awt.Font("Monospaced", 0, 12));
    messages.setEditable(false);
    jPanel11.setLayout(borderLayout5);
    jPanel12.setLayout(gridLayout8);
    this.setLayout(borderLayout1);
    bar.setString("");
    bar.setStringPainted(true);
    jPanel12.add(open, null);
    jPanel12.add(remove, null);
    jPanel12.add(create, null);
    jPanel12.add(save, null);
    jPanel2.add(image, null);
    jPanel2.add(edge, null);
    jPanel2.add(mask);
    this.add(jPanel4, BorderLayout.WEST);
    this.add(jSplitPane1, BorderLayout.CENTER);
    jSplitPane1.add(jScrollPane2, JSplitPane.LEFT);
    jSplitPane1.add(jScrollPane3, JSplitPane.RIGHT);
    jScrollPane3.getViewport().add(messages, null);
    jScrollPane2.getViewport().add(mosaic, null);
    this.add(jPanel11, BorderLayout.NORTH);
    jPanel11.add(jPanel12, BorderLayout.WEST);
    jPanel11.add(bar, BorderLayout.CENTER);
    dialog.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(preview, null);
    dialogMask.getContentPane().add(backForePanel, java.awt.BorderLayout.CENTER);
    jScrollPane4.getViewport().add(jPanel2);
    jPanel4.add(settingPanel, java.awt.BorderLayout.NORTH);
    jPanel4.add(jScrollPane4, java.awt.BorderLayout.CENTER);
    Listener listener = new Listener();
    open.addActionListener(listener);
    openImage.addActionListener(listener);
    openEdge.addActionListener(listener);
    openMask.addActionListener(listener);
    remove.addActionListener(listener);
    removeEdge.addActionListener(listener);
    removeMask.addActionListener(listener);
    create.addActionListener(listener);
    save.addActionListener(listener);
    saveImage.addActionListener(listener);
    saveEdge.addActionListener(listener);
    saveMask.addActionListener(listener);
    image.addMouseListener(listener);
    edge.addMouseListener(listener);
    mask.addMouseListener(listener);
    backForePanel.addActionListener(listener);
    jSplitPane1.setDividerLocation(340);
  }

  private class Listener extends MouseAdapter implements ActionListener {

    @Override
    public void mousePressed(MouseEvent e) {
      Object source = e.getSource();
      Image im = null;
      if (source == image) {
        im = ma.getImage();
      } else if (source == edge) {
        im = ma.getEdge();
      } else if (source == mask) {
        im = ma.getMask();
      }
      if (im != null) {
        preview.setIcon(new ImageIcon(im));
        dialog.setVisible(true);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source == open) {
        menuOpen.show(open, 0, open.getHeight());
      } else if (source == openImage) {
        open(IMAGE);
      } else if (source == openEdge) {
        open(EDGE);
      } else if (source == openMask) {
        open(MASK);
      } else if (source == remove) {
        menuRemove.show(remove, 0, remove.getHeight());
      } else if (source == removeEdge) {
        ma.setEdge(null);
        edge.setIcon(null);
        edge.setCursor(null);
        edge.setToolTipText(null);
      } else if (source == removeMask) {
        ma.setMask(null);
        mask.setIcon(null);
        mask.setCursor(null);
        mask.setToolTipText(null);
        saveMask.setEnabled(false);
      } else if (source == create) {
        final int type = settingPanel.getMosaicType();
        final boolean b = type != MosaicAlgorithm.OPUS_MUSIVUM;
        if (b) {
          BufferedImage image = ma.getImage();
          BufferedImage mask = ma.getMask();
          if (mask == null || mask.getWidth() != image.getWidth() || mask.getHeight() != image.getHeight()) {
            backForePanel.setImage(image);
            dialogMask.setVisible(true);
            ma.setMask(backForePanel.getBackForeImage());
          }
        } else {
          ma.setMask(null);
        }
        (new Thread() {
          @Override
          public void run() {
            setComponentsEnabled(false);
            ma.evaluate(type, settingPanel.getTileSize(), settingPanel.isScanLevelLineOrder(), settingPanel.isScanLineOrder(), settingPanel.isRemoveArc(), settingPanel.isRemoveConcavity(), settingPanel.getMinCoveredPixels(), settingPanel.getEdgeDetectionTechnique(), settingPanel.getBorderTileNumber());
            mosaic.setIcon(new ImageIcon(ma.getMosaic()));
            setComponentsEnabled(true);
            edge.setIcon(createIcon(ma.getEdge()));
            edge.setCursor(new Cursor(Cursor.HAND_CURSOR));
            edge.setToolTipText("Click to enlarge");
            if (b) {
              mask.setIcon(createIcon(ma.getMask()));
              mask.setCursor(new Cursor(Cursor.HAND_CURSOR));
              mask.setToolTipText("Click to enlarge");
            }
            saveMask.setEnabled(b);
          }
        }).start();
      } else if (source == backForePanel) {
        dialogMask.setVisible(false);
      } else if (source == save) {
        menuSave.show(save, 0, save.getHeight());
      } else if (source == saveImage) {
        save(ma.getMosaic());
      } else if (source == saveEdge) {
        save(ma.getEdge());
      } else if (source == saveMask) {
        save(ma.getMask());
      }
    }
  }

  private class OpenImageFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
      String str = f.getName().toLowerCase();
      return str.endsWith(".gif") || str.endsWith(".jpg") || str.endsWith(".jpeg")
              || str.endsWith(".png") || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "Image File";
    }
  }

  private class SaveImageFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
      String str = f.getName().toLowerCase();
      return str.endsWith(".png") || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "PNG";
    }

    @Override
    public String toString() {
      return "png";
    }
  }
}
