package it.unict.dmi.mosaic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MosaicFrame extends JFrame {

  private static final long serialVersionUID = 1L;
  private MosaicPanel mosaicPanel = new MosaicPanel();

  @SuppressWarnings("CallToPrintStackTrace")
  public MosaicFrame() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setTitle("Artifical Mosaic Creator");
    this.setSize(new Dimension(800, 700));
    this.getContentPane().add(mosaicPanel, BorderLayout.CENTER);
  }

  //MAIN
  public static void main(String[] a) {
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
    }
    MosaicFrame f = new MosaicFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(true);
    f.setExtendedState(JFrame.MAXIMIZED_BOTH);
  }
  //END MAIN
}
