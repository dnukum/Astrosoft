/**
 * HousePACView.java
 *
 * Dedicated presentation view for K.N. Rao PAC (Placement, Aspect, Conjunction)
 * analysis. Features:
 * 1. 2-row Lagna-aligned 12-house button strip (Row 1: H1-H6, Row 2: H7-H12)
 *    aligning opposing 1/7, 2/8, 3/9, 4/10, 5/11, 6/12 mutual aspect axes.
 * 2. PAC Card displaying [P] Placement, [A] Aspect, and [C] Conjunction/Yogas.
 * 3. Right-click context menu and one-click "Copy" button for Chart Notes.
 */
package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import app.astrosoft.beans.HousePACData;
import app.astrosoft.beans.HousePACData.AspectInfo;
import app.astrosoft.beans.HousePACData.LordPlacement;
import app.astrosoft.beans.HousePACData.OccupantInfo;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.FunctionalRole;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.HousePACCalculator;

public class HousePACView extends JPanel {

	private final PlanetaryInfo planetaryInfo;
	private final Rasi ascendant;
	private final List<HousePACData> houseDataList;
	private int selectedHouse = 1;

	private final List<JButton> houseButtons = new ArrayList<>(12);
	private final JEditorPane pacCardPane;
	private final JButton copyBtn;
	private final JButton copyAllBtn;

	public HousePACView(Horoscope horoscope) {
		this(horoscope != null ? horoscope.getPlanetaryInfo() : null,
			 horoscope != null ? horoscope.getAscendant() : null);
	}

	public HousePACView(PlanetaryInfo planetaryInfo, Rasi ascendant) {
		this.planetaryInfo = planetaryInfo;
		this.ascendant = ascendant != null ? ascendant : Rasi.Mesha;
		this.houseDataList = (planetaryInfo != null)
				? HousePACCalculator.calculateAll(planetaryInfo, this.ascendant)
				: new ArrayList<>();

		setLayout(new BorderLayout(0, 8));
		setOpaque(false);

		// ---------------------------------------------------------------------
		// 1. Top Section: Clean Single-Row H1-H12 Strip & Action Buttons
		// ---------------------------------------------------------------------
		JPanel topContainer = new JPanel(new BorderLayout(10, 0));
		topContainer.setOpaque(false);

		// Action buttons: "Copy House" and "Copy All 12"
		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		actionsPanel.setOpaque(false);

		copyBtn = createActionButton("Copy House", "Copy current House PAC summary to clipboard", 84);
		copyBtn.addActionListener(e -> copyCurrentHouseSummary());
		actionsPanel.add(copyBtn);

		copyAllBtn = createActionButton("Copy All 12", "Copy all 12 houses PAC report to clipboard", 88);
		copyAllBtn.addActionListener(e -> copyAllHousesSummary());
		actionsPanel.add(copyAllBtn);

		topContainer.add(actionsPanel, BorderLayout.EAST);

		// Clean 1-Row Strip: 12 buttons (H1 through H12)
		JPanel stripPanel = new JPanel(new GridLayout(1, 12, 4, 0));
		stripPanel.setOpaque(false);
		stripPanel.setPreferredSize(new Dimension(520, 26));

		for (int h = 1; h <= 12; h++) {
			final int houseNum = h;
			String labelText = "H" + houseNum;

			JButton btn = new JButton(labelText) {
				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					boolean isSelected = (houseNum == selectedHouse);
					boolean isHover = getModel().isRollover();

					int w = getWidth();
					int h = getHeight();
					int arc = 10;

					if (isSelected) {
						g2.setColor(new Color(44, 82, 130)); // Deep navy
						g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
						g2.setColor(new Color(30, 60, 95));
						g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
						setForeground(Color.WHITE);
					} else if (isHover) {
						g2.setColor(new Color(228, 238, 250)); // Soft sky blue
						g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
						g2.setColor(new Color(110, 140, 175));
						g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
						setForeground(new Color(25, 45, 75));
					} else {
						g2.setColor(new Color(248, 250, 252));
						g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
						g2.setColor(new Color(203, 213, 224));
						g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
						setForeground(new Color(45, 55, 72));
					}

					FontMetrics fm = g2.getFontMetrics(getFont());
					int tw = fm.stringWidth(getText());
					int th = fm.getAscent();
					g2.setColor(getForeground());
					g2.setFont(getFont());
					g2.drawString(getText(), (w - tw) / 2, (h + th) / 2 - 2);
					g2.dispose();
				}
			};

			btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
			btn.setFocusPainted(false);
			btn.setBorderPainted(false);
			btn.setContentAreaFilled(false);
			btn.setOpaque(false);
			btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			btn.setToolTipText("View House " + houseNum + " PAC analysis");
			btn.addActionListener(e -> selectHouse(houseNum));

			houseButtons.add(btn);
			stripPanel.add(btn);
		}

		topContainer.add(stripPanel, BorderLayout.CENTER);
		add(topContainer, BorderLayout.NORTH);

		// ---------------------------------------------------------------------
		// 2. Center Section: PAC Details Card in ScrollPane
		// ---------------------------------------------------------------------
		pacCardPane = new JEditorPane();
		pacCardPane.setContentType("text/html");
		pacCardPane.setEditable(false);
		pacCardPane.setOpaque(true);
		pacCardPane.setBackground(Color.WHITE);
		pacCardPane.setBorder(new EmptyBorder(4, 6, 4, 6));

		// Attach Right-Click Context Menu for Copy
		JPopupMenu contextMenu = new JPopupMenu();
		JMenuItem copyItem = new JMenuItem("Copy House Summary");
		copyItem.addActionListener(e -> copyCurrentHouseSummary());
		contextMenu.add(copyItem);

		pacCardPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					contextMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(pacCardPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 224), 1));
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(true);
		scrollPane.getViewport().setBackground(Color.WHITE);

		add(scrollPane, BorderLayout.CENTER);

		// Initial display
		updatePACCard();
	}

	public void selectHouse(int houseNum) {
		if (houseNum < 1 || houseNum > 12) return;
		this.selectedHouse = houseNum;
		for (JButton btn : houseButtons) {
			btn.repaint();
		}
		updatePACCard();
	}

	public int getSelectedHouse() {
		return selectedHouse;
	}

	private HousePACData getHouseData(int houseNum) {
		if (houseNum >= 1 && houseNum <= houseDataList.size()) {
			return houseDataList.get(houseNum - 1);
		}
		return null;
	}

	private void updatePACCard() {
		HousePACData data = getHouseData(selectedHouse);
		if (data == null) {
			pacCardPane.setText("<html><body style='font-family: sans-serif; font-size: 10px; color: #718096; padding: 10px;'>No chart data available.</body></html>");
			return;
		}

		StringBuilder html = new StringBuilder();
		html.append("<html><body style='font-family: sans-serif; font-size: 11px; color: #2d3748; margin: 4px 6px;'>");

		// Header Banner
		html.append("<div style='background-color: #ebf8ff; border: 1px solid #bee3f8; border-radius: 6px; padding: 6px 12px; margin-bottom: 8px;'>");
		html.append("<b style='font-size: 13px; color: #1a365d;'>HOUSE ").append(data.getHouseNum()).append(": ");
		html.append(data.getRasi().englishName().toUpperCase()).append(" (").append(data.getRasi().name()).append(")</b>");
		if (!data.getClassifications().isEmpty()) {
			html.append("<span style='color: #4a5568;'> — ").append(String.join(", ", data.getClassifications())).append("</span>");
		}
		html.append("<span style='color: #718096;'> | </span>");
		html.append("<b>Lord:</b> <span style='color: #2b6cb0; font-weight: bold;'>").append(data.getLord().name()).append("</span>");
		html.append("</div>");

		// 3-Column Cards Table (Option 1 Layout)
		html.append("<table width='100%' border='0' cellspacing='8' cellpadding='0'><tr>");

		// -------------------------------------------------------------
		// Card 1: [P] PLACEMENT
		// -------------------------------------------------------------
		html.append("<td width='33%' valign='top'>");
		html.append("<div style='background-color: #ffffff; border: 1px solid #cbd5e0; border-radius: 6px; padding: 10px; min-height: 280px;'>");
		html.append("<div style='font-size: 11px; font-weight: bold; color: #2c5282; border-bottom: 2px solid #3182ce; padding-bottom: 4px; margin-bottom: 8px;'>[P] PLACEMENT</div>");

		LordPlacement lp = data.getLordPlacement();
		if (lp != null) {
			html.append("<div style='margin-bottom: 6px;'>");
			html.append("• <b>Lord Placement:</b> ").append(lp.getLord().name());
			if (lp.isRetrograde()) html.append(" [R]");
			if (lp.isCombust()) html.append(" [C]");
			html.append(", ").append(app.astrosoft.core.HousePACCalculator.ordinal(lp.getRuledHouse()))
			    .append(" lord, is placed in ").append(app.astrosoft.core.HousePACCalculator.ordinal(lp.getHouse()))
			    .append(" house");
			if (lp.getRasi() != null) {
				html.append(" (").append(lp.getRasi().englishName());
				if (lp.getDignity() != null) {
					html.append(", ").append(lp.getDignity().label()).append(" sign");
				}
				html.append(")");
			}
			html.append("<br/>&nbsp;&nbsp;&nbsp;— ").append(formatRoleBadge(lp.isYogakaraka(), lp.getFunctionalRole()));
			html.append("</div>");
		}
		if (data.getOccupants().isEmpty()) {
			html.append("• <b>Occupants:</b> <span style='color: #718096;'>None (Empty house)</span>");
		} else {
			html.append("• <b>Occupants:</b>");
			for (OccupantInfo occ : data.getOccupants()) {
				html.append("<div style='margin-left: 14px; margin-top: 3px;'>");
				html.append("• <b>").append(occ.getPlanet().name());
				if (occ.isRetrograde()) html.append(" [R]");
				if (occ.isCombust()) html.append(" [C]");
				html.append(":</b> ");
				html.append(formatRoleBadge(occ.isYogakaraka(), occ.getFunctionalRole()));
				if (occ.getDignity() != null) {
					html.append(" in ").append(occ.getDignity().label().toLowerCase()).append(" sign");
				}
				html.append("</div>");
			}
		}
		html.append("</div></td>");

		// -------------------------------------------------------------
		// Card 2: [A] ASPECTS
		// -------------------------------------------------------------
		html.append("<td width='33%' valign='top'>");
		html.append("<div style='background-color: #ffffff; border: 1px solid #cbd5e0; border-radius: 6px; padding: 10px; min-height: 280px;'>");
		html.append("<div style='font-size: 11px; font-weight: bold; color: #2c5282; border-bottom: 2px solid #3182ce; padding-bottom: 4px; margin-bottom: 8px;'>[A] ASPECTS</div>");

		if (data.getAspects().isEmpty()) {
			html.append("• <span style='color: #718096;'>None (No planetary aspects on this house)</span>");
		} else {
			boolean firstAsp = true;
			for (AspectInfo asp : data.getAspects()) {
				if (!firstAsp) html.append("<div style='margin-top: 6px;'></div>");
				html.append("• Aspected by <b>").append(asp.getPlanet().name()).append("</b> from House ")
				    .append(asp.getFromHouse()).append(" via ").append(asp.getAspectName());
				String roleText = formatRoleBadge(asp.isYogakaraka(), asp.getFunctionalRole());
				if (asp.isOwnLord()) {
					html.append("<br/>&nbsp;&nbsp;&nbsp;(").append(roleText).append(" <span style='color: #718096;'>- Own House Lord</span>)");
				} else {
					html.append("<br/>&nbsp;&nbsp;&nbsp;(").append(roleText).append(")");
				}
				firstAsp = false;
			}
		}
		html.append("</div></td>");

		// -------------------------------------------------------------
		// Card 3: [C] CONJUNCTIONS & YOGAS
		// -------------------------------------------------------------
		html.append("<td width='34%' valign='top'>");
		html.append("<div style='background-color: #ffffff; border: 1px solid #cbd5e0; border-radius: 6px; padding: 10px; min-height: 280px;'>");
		html.append("<div style='font-size: 11px; font-weight: bold; color: #2c5282; border-bottom: 2px solid #3182ce; padding-bottom: 4px; margin-bottom: 8px;'>[C] CONJUNCTIONS &amp; YOGAS</div>");

		if (data.getConjunctions().isEmpty() && data.getYogas().isEmpty()) {
			html.append("• <span style='color: #718096;'>None</span>");
		} else {
			for (String c : data.getConjunctions()) {
				html.append("<div style='margin-bottom: 4px;'>• ").append(c).append("</div>");
			}
			if (!data.getYogas().isEmpty()) {
				html.append("<div style='background-color: #fefcbf; border: 1px solid #ecc94b; border-radius: 4px; padding: 6px 8px; margin-top: 8px;'>");
				boolean firstYoga = true;
				for (String y : data.getYogas()) {
					if (!firstYoga) html.append("<div style='margin-top: 4px;'></div>");
					html.append("<span style='color: #b7791f;'>★</span> <b style='color: #2b6cb0;'>").append(y).append("</b>");
					firstYoga = false;
				}
				html.append("</div>");
			}
		}
		html.append("</div></td>");

		html.append("</tr></table>");
		html.append("</body></html>");
		pacCardPane.setText(html.toString());
		pacCardPane.setCaretPosition(0);
	}

	private JButton createActionButton(String text, String tooltip, int width) {
		JButton btn = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				boolean isHover = getModel().isRollover();
				int w = getWidth();
				int h = getHeight();
				g2.setColor(isHover ? new Color(228, 238, 250) : Color.WHITE);
				g2.fillRoundRect(1, 1, w - 2, h - 2, 8, 8);
				g2.setColor(new Color(180, 195, 215));
				g2.drawRoundRect(1, 1, w - 3, h - 3, 8, 8);

				FontMetrics fm = g2.getFontMetrics(getFont());
				int tw = fm.stringWidth(getText());
				int th = fm.getAscent();
				g2.setColor(new Color(44, 82, 130));
				g2.setFont(getFont());
				g2.drawString(getText(), (w - tw) / 2, (h + th) / 2 - 2);
				g2.dispose();
			}
		};
		btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setContentAreaFilled(false);
		btn.setOpaque(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.setPreferredSize(new Dimension(width, 24));
		btn.setToolTipText(tooltip);
		return btn;
	}

	private static String formatRoleBadge(boolean isYk, FunctionalRole role) {
		if (isYk) {
			return "<font color='#276749'><b>Yogakaraka</b></font>";
		}
		if (role == null) return "";
		switch (role) {
			case BENEFIC:
				return "<font color='#276749'><b>Functional Benefic</b></font>";
			case MALEFIC:
				return "<font color='#9b2c2c'><b>Functional Malefic</b></font>";
			case NEUTRAL:
				return "<font color='#718096'>Functional Neutral</font>";
			default:
				return role.name();
		}
	}

	public void copyCurrentHouseSummary() {
		HousePACData data = getHouseData(selectedHouse);
		if (data != null) {
			StringSelection selection = new StringSelection(data.toPlainText());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);

			copyBtn.setText("Copied!");
			Timer timer = new Timer(1500, e -> copyBtn.setText("Copy House"));
			timer.setRepeats(false);
			timer.start();
		}
	}

	public void copyAllHousesSummary() {
		StringBuilder all = new StringBuilder();
		all.append("==================================================\n");
		all.append("ASTROSOFT - COMPLETE 12 HOUSES PAC ANALYSIS\n");
		all.append("==================================================\n\n");
		for (int h = 1; h <= 12; h++) {
			HousePACData d = getHouseData(h);
			if (d != null) {
				all.append(d.toPlainText()).append("\n\n");
			}
		}
		StringSelection selection = new StringSelection(all.toString());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);

		copyAllBtn.setText("Copied 12!");
		Timer timer = new Timer(1500, e -> copyAllBtn.setText("Copy All 12"));
		timer.setRepeats(false);
		timer.start();
	}
}
