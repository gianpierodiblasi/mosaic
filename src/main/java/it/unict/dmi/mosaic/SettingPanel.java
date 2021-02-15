package it.unict.dmi.mosaic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class SettingPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private JPanel jPanel3 = new JPanel();
  private JSpinner wTile = new JSpinner();
  private JSpinner hTile = new JSpinner();
  private JSpinner minCoveredPixels = new JSpinner();
  private SpinnerNumberModel modelW = new SpinnerNumberModel(5, 1, 20, 1);
  private SpinnerNumberModel modelH = new SpinnerNumberModel(10, 1, 20, 1);
  private SpinnerNumberModel modelMinCoveredPixels = new SpinnerNumberModel(0, 0, 50, 1);
  private SpinnerNumberModel modelBorderTileNumber = new SpinnerNumberModel(2, 0, 10, 1);
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JCheckBox removeArc = new JCheckBox();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel jPanel6 = new JPanel();
  private JCheckBox removeConcavity = new JCheckBox();
  private GridLayout gridLayout3 = new GridLayout();
  private Border border1;
  private TitledBorder titledBorder1;
  private JPanel jPanel7 = new JPanel();
  private JCheckBox scanLineOrder = new JCheckBox();
  private JCheckBox scanLevelLineOrder = new JCheckBox();
  private Border border2;
  private TitledBorder titledBorder2;
  private GridLayout gridLayout4 = new GridLayout();
  private JLabel jLabel3 = new JLabel();
  private JPanel jPanel9 = new JPanel();
  private GridLayout gridLayout7 = new GridLayout();
  private JPanel jPanel10 = new JPanel();
  private BorderLayout borderLayout4 = new BorderLayout();
  private GridLayout gridLayout2 = new GridLayout();
  private BoxLayout boxLayout1 = new BoxLayout(this, BoxLayout.Y_AXIS);
  private JPanel jPanel1 = new JPanel();
  private Border border3;
  private TitledBorder titledBorder3;
  private JRadioButton guideline = new JRadioButton();
  private JRadioButton srm = new JRadioButton();
  private ButtonGroup buttonGroup1 = new ButtonGroup();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel2 = new JPanel();
  private Border border4 = BorderFactory.createEtchedBorder(Color.white, new Color(142, 142, 142));
  private Border border5 = new TitledBorder(border4, "Mosaic Type");
  private JRadioButton opusMusivum = new JRadioButton();
  private JRadioButton opusVermiculatum = new JRadioButton();
  private GridLayout gridLayout5 = new GridLayout();
  private ButtonGroup buttonGroup2 = new ButtonGroup();
  private JLabel jLabel4 = new JLabel();
  private JSpinner borderTileNumber = new JSpinner();

  @SuppressWarnings("CallToPrintStackTrace")
  public SettingPanel() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Dimension getTileSize() {
    return new Dimension(((Number) wTile.getValue()).intValue(), ((Number) hTile.getValue()).intValue());
  }

  public boolean isScanLevelLineOrder() {
    return scanLevelLineOrder.isSelected();
  }

  public boolean isScanLineOrder() {
    return scanLineOrder.isSelected();
  }

  public boolean isRemoveArc() {
    return removeArc.isSelected();
  }

  public boolean isRemoveConcavity() {
    return removeConcavity.isSelected();
  }

  public int getEdgeDetectionTechnique() {
    return guideline.isSelected() ? MosaicAlgorithm.DIRECTIONAL_GUIDELINE : MosaicAlgorithm.STATISTICAL_REGION_MERGING;
  }

  public int getMinCoveredPixels() {
    return ((Number) minCoveredPixels.getValue()).intValue();
  }

  public int getBorderTileNumber() {
    return ((Number) borderTileNumber.getValue()).intValue();
  }

  public int getMosaicType() {
    if (opusMusivum.isSelected()) {
      return MosaicAlgorithm.OPUS_MUSIVUM;
    } else if (opusVermiculatum.isSelected()) {
      return MosaicAlgorithm.OPUS_VERMICULATUM;
    } else {
      return -1;
    }
  }

  public void setComponentsEnabled(boolean b) {
    scanLevelLineOrder.setEnabled(b);
    scanLineOrder.setEnabled(b);
    wTile.setEnabled(b);
    hTile.setEnabled(b);
    minCoveredPixels.setEnabled(b);
    removeArc.setEnabled(b);
    removeConcavity.setEnabled(b && removeArc.isSelected());
    guideline.setEnabled(b);
    srm.setEnabled(b);
    opusMusivum.setEnabled(b);
    opusVermiculatum.setEnabled(b);
    borderTileNumber.setEnabled(b && opusVermiculatum.isSelected());
  }

  private void jbInit() throws Exception {
    border1 = BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151));
    titledBorder1 = new TitledBorder(border1, "Settings");
    border2 = BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151));
    titledBorder2 = new TitledBorder(border2, "Tile Placing Policy");
    border3 = BorderFactory.createEtchedBorder(Color.white, new Color(142, 142, 142));
    titledBorder3 = new TitledBorder(border3, "Edge Detection Technique");
    jPanel3.setLayout(borderLayout4);
    wTile.setModel(modelW);
    hTile.setModel(modelH);
    jLabel1.setBorder(BorderFactory.createEtchedBorder());
    jLabel1.setText("Width");
    jLabel2.setBorder(BorderFactory.createEtchedBorder());
    jLabel2.setText("Height");
    removeArc.setText("remove Arc");
    this.setLayout(borderLayout2);
    removeConcavity.setEnabled(false);
    removeConcavity.setText("remove Concavity");
    jPanel6.setLayout(gridLayout3);
    gridLayout3.setRows(2);
    this.setBorder(titledBorder1);
    scanLineOrder.setSelected(true);
    scanLineOrder.setText("Scan Line Order");
    scanLevelLineOrder.setSelected(true);
    scanLevelLineOrder.setText("Scan Level Line Order");
    jPanel7.setBorder(titledBorder2);
    jPanel7.setLayout(gridLayout4);
    gridLayout4.setRows(2);
    jLabel3.setBorder(BorderFactory.createEtchedBorder());
    jLabel3.setText("Min. Covered Pixels");
    jPanel9.setLayout(gridLayout7);
    gridLayout7.setRows(4);
    jPanel10.setLayout(gridLayout2);
    gridLayout2.setRows(4);
    minCoveredPixels.setModel(modelMinCoveredPixels);
    this.setLayout(boxLayout1);
    jPanel1.setBorder(titledBorder3);
    jPanel1.setLayout(gridLayout1);
    guideline.setSelected(true);
    guideline.setText("Directional Guideline");
    srm.setText("Statistical Region Merging");
    gridLayout1.setRows(2);
    jPanel2.setBorder(border5);
    jPanel2.setLayout(gridLayout5);
    opusMusivum.setSelected(true);
    opusMusivum.setText("Opus Musivum");
    opusVermiculatum.setText("Opus Vermiculatum");
    gridLayout5.setRows(2);
    jLabel4.setBorder(BorderFactory.createEtchedBorder());
    jLabel4.setText("Border Tile Number");
    borderTileNumber.setModel(modelBorderTileNumber);
    borderTileNumber.setEnabled(false);
    jPanel10.add(wTile, null);
    jPanel10.add(hTile, null);
    jPanel10.add(minCoveredPixels, null);
    jPanel10.add(borderTileNumber);
    this.add(jPanel2);
    this.add(jPanel1, null);
    jPanel1.add(guideline, null);
    jPanel1.add(srm, null);
    this.add(jPanel7, null);
    jPanel3.add(jPanel9, BorderLayout.WEST);
    jPanel3.add(jPanel10, BorderLayout.CENTER);
    this.add(jPanel3, null);
    jPanel9.add(jLabel1, null);
    jPanel9.add(jLabel2, null);
    jPanel9.add(jLabel3, null);
    jPanel9.add(jLabel4);
    jPanel7.add(scanLevelLineOrder, null);
    jPanel7.add(scanLineOrder, null);
    this.add(jPanel6, null);
    jPanel6.add(removeArc, null);
    jPanel6.add(removeConcavity, null);
    Listener listener = new Listener();
    removeArc.addActionListener(listener);
    opusMusivum.addActionListener(listener);
    opusVermiculatum.addActionListener(listener);
    buttonGroup1.add(guideline);
    buttonGroup1.add(srm);
    jPanel2.add(opusMusivum);
    jPanel2.add(opusVermiculatum);
    buttonGroup2.add(opusMusivum);
    buttonGroup2.add(opusVermiculatum);

  }

  private class Listener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source == removeArc) {
        boolean b = removeArc.isSelected();
        removeConcavity.setEnabled(b);
        if (!b) {
          removeConcavity.setSelected(false);
        }
      } else if (source == opusMusivum) {
        borderTileNumber.setEnabled(false);
      } else if (source == opusVermiculatum) {
        borderTileNumber.setEnabled(true);
      }
    }
  }
}
