package app.astrosoft.ui.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;

import app.astrosoft.beans.NakshathraPada;
import app.astrosoft.beans.PlanetCharacter;
import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.Dignity;
import app.astrosoft.consts.FunctionalRole;
import app.astrosoft.consts.Karaka;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Roman;
import app.astrosoft.consts.SignRelationship;
import app.astrosoft.consts.TableStyle;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.PlanetStatusCalculator;
import app.astrosoft.core.ShadBala;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.DefaultColumnMetaData;
import app.astrosoft.ui.table.ListTableData;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.ui.table.TableDataFactory;
import app.astrosoft.ui.util.PlanetLabels;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.util.AstroUtil;

/**
 * Multi-tab "Planet Details" panel for the Horoscope Dashboard.
 * Provides 3 perspectives via subtle rounded pill tabs:
 * 1. Positions: Classical coordinates, signs, retrogrades, dignities, with Nakshatra, Pada, and Star Lord on hover tooltip.
 * 2. Roles & Karakas: Bhava placement, Dr. B.V. Raman functional role ([Yog], [Ben], [Mal], [Neu]), and Jaimini Chara Karaka (AK, AmK, BK, MK, PK, GK, DK).
 * 3. Shadbala: Colored horizontal strength bars with 100% threshold guideline and rich tooltips.
 */
public class RelativePlanetView extends JPanel {

	private final JLabel title;
	private final JButton resizeBtn;
	private final PillTabBar pillBar;
	private final CardLayout cardLayout;
	private final JPanel contentCards;

	private static final int CARD_WIDTH = 530;
	private static final int CARD_HEIGHT = 188;
	private static final int DEFAULT_HOUSES_HEIGHT = 380;
	private int cardHeight = CARD_HEIGHT;
	private boolean userCustomizedHeight = false;

	private static final String TAB_POSITIONS = "POSITIONS";
	private static final String TAB_ROLES = "ROLES";
	private static final String TAB_SHADBALA = "SHADBALA";

	private static final Planet[] NAK_LORDS = {
		Planet.Ketu, Planet.Venus, Planet.Sun, Planet.Moon, Planet.Mars,
		Planet.Rahu, Planet.Jupiter, Planet.Saturn, Planet.Mercury
	};

	public RelativePlanetView(PlanetaryInfo planetaryInfo) {
		this(planetaryInfo, null, null);
	}

	public RelativePlanetView(PlanetaryInfo planetaryInfo, Horoscope horoscope) {
		this(planetaryInfo, horoscope != null ? horoscope.getAscendant() : null,
				horoscope != null ? horoscope.getShadBala() : null);
	}

	public RelativePlanetView(PlanetaryInfo planetaryInfo, Rasi ascendant, ShadBala shadBala) {
		setLayout(null);
		setOpaque(false);

		final Dimension panelSize = new Dimension(CARD_WIDTH, CARD_HEIGHT);
		setPreferredSize(panelSize);
		setMinimumSize(panelSize);
		setMaximumSize(panelSize);

		EnumMap<Planet, Rasi> planetRasi = planetaryInfo.getPlanetRasi();
		final EnumMap<Planet, Double> planetPos = planetaryInfo.getPlanetPosition();

		Rasi ascRasi = ascendant != null ? ascendant : (planetRasi != null ? planetRasi.get(Planet.Ascendant) : Rasi.Mesha);
		int ascIndex = ascRasi.ordinal();

		// Sort planets in zodiacal order starting from Ascendant
		List<Planet> sortedPlanets = new ArrayList<Planet>();
		for (int i = 0; i < 12; i++) {
			int rasiIndex = (ascIndex + i) % 12;
			Rasi currentRasi = Rasi.values()[rasiIndex];

			List<Planet> planetsInSign = new ArrayList<Planet>();
			for (Planet p : Planet.values()) {
				if (planetRasi != null && planetRasi.get(p) == currentRasi) {
					planetsInSign.add(p);
				}
			}

			Collections.sort(planetsInSign, new Comparator<Planet>() {
				public int compare(Planet p1, Planet p2) {
					Double pos1 = (planetPos != null && planetPos.containsKey(p1)) ? (planetPos.get(p1) % 30.0) : 0.0;
					Double pos2 = (planetPos != null && planetPos.containsKey(p2)) ? (planetPos.get(p2) % 30.0) : 0.0;
					return pos1.compareTo(pos2);
				}
			});

			sortedPlanets.addAll(planetsInSign);
		}

		// Header title
		title = new JLabel("Planet Details", SwingConstants.LEFT);
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		title.setForeground(UIConsts.TEXT_DARK_NAVY);
		title.setBounds(20, 3, 90, 22);
		add(title);

		// Header vertical expand/compact button [↕]
		resizeBtn = new JButton("↕") {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				boolean isHover = getModel().isRollover();
				int w = getWidth();
				int h = getHeight();
				g2.setColor(isHover ? new Color(228, 238, 250) : Color.WHITE);
				g2.fillRoundRect(1, 1, w - 2, h - 2, 6, 6);
				g2.setColor(new Color(180, 195, 215));
				g2.drawRoundRect(1, 1, w - 3, h - 3, 6, 6);

				FontMetrics fm = g2.getFontMetrics(getFont());
				int tw = fm.stringWidth(getText());
				int th = fm.getAscent();
				g2.setColor(new Color(44, 82, 130));
				g2.setFont(getFont());
				g2.drawString(getText(), (w - tw) / 2, (h + th) / 2 - 2);
				g2.dispose();
			}
		};
		resizeBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
		resizeBtn.setFocusPainted(false);
		resizeBtn.setBorderPainted(false);
		resizeBtn.setContentAreaFilled(false);
		resizeBtn.setOpaque(false);
		resizeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		resizeBtn.setToolTipText("Expand / Compact card vertically (or drag the bottom border)");
		resizeBtn.setBounds(112, 4, 18, 20);
		resizeBtn.addActionListener(e -> toggleHeight());
		add(resizeBtn);

		// Multi-tab Card layout container
		cardLayout = new CardLayout();
		contentCards = new JPanel(cardLayout);
		contentCards.setOpaque(false);
		contentCards.setBounds(18, 30, 494, cardHeight - 38);

		// Build the 4 perspective panels
		JPanel positionsPanel = createPositionsPanel(planetaryInfo, sortedPlanets, planetRasi, planetPos);
		JPanel rolesPanel = createRolesAndKarakasPanel(planetaryInfo, sortedPlanets, planetRasi, ascRasi, planetPos);
		JPanel shadbalaPanel = new ShadbalaBarPanel(shadBala);

		contentCards.add(positionsPanel, TAB_POSITIONS);
		contentCards.add(rolesPanel, TAB_ROLES);
		contentCards.add(shadbalaPanel, TAB_SHADBALA);

		add(contentCards);

		// Header subtle rounded pill tab bar
		pillBar = new PillTabBar(tabIndex -> {
			switch (tabIndex) {
				case 0:
					cardLayout.show(contentCards, TAB_POSITIONS);
					break;
				case 1:
					cardLayout.show(contentCards, TAB_ROLES);
					break;
				case 2:
					cardLayout.show(contentCards, TAB_SHADBALA);
					break;
			}
		});
		pillBar.setBounds(180, 3, 332, 24);
		add(pillBar);

		// Interactive bottom-edge drag to resize
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getY() >= getHeight() - 14) {
					toggleHeight();
				}
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (e.getY() >= getHeight() - 14) {
					setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				} else {
					setCursor(Cursor.getDefaultCursor());
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (getCursor().getType() == Cursor.N_RESIZE_CURSOR) {
					int newH = e.getY();
					setCardHeight(newH, true);
				}
			}
		});
	}

	public void toggleHeight() {
		if (cardHeight > CARD_HEIGHT) {
			setCardHeight(CARD_HEIGHT, true);
		} else {
			setCardHeight(DEFAULT_HOUSES_HEIGHT, true);
		}
	}

	@Override
	public void doLayout() {
		super.doLayout();
		int currentH = getHeight();
		if (currentH <= 0) currentH = cardHeight;
		contentCards.setBounds(18, 30, 494, currentH - 38);
	}

	public void setCardHeight(int newHeight) {
		setCardHeight(newHeight, true);
	}

	public void setCardHeight(int newHeight, boolean fromUserDrag) {
		this.cardHeight = Math.max(CARD_HEIGHT, Math.min(650, newHeight));
		if (fromUserDrag) {
			this.userCustomizedHeight = true;
		}
		Dimension d = new Dimension(CARD_WIDTH, this.cardHeight);
		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);
		contentCards.setBounds(18, 30, 494, this.cardHeight - 38);
		revalidate();
		repaint();

		Container p = getParent();
		while (p != null) {
			if (p instanceof HoroscopeDashboard) {
				((HoroscopeDashboard) p).updateLayout();
			}
			p.revalidate();
			p.repaint();
			p = p.getParent();
		}
	}

	public int getCardHeight() {
		return cardHeight;
	}

	public void switchToTab(int tabIndex) {
		if (pillBar != null) {
			pillBar.selectTab(tabIndex);
		}
	}

	// -------------------------------------------------------------------------
	// Tab 1: Positions (Coordinates, Signs, Dignities + Nakshatra Tooltips)
	// -------------------------------------------------------------------------
	private JPanel createPositionsPanel(PlanetaryInfo planetaryInfo, List<Planet> sortedPlanets,
			EnumMap<Planet, Rasi> planetRasi, EnumMap<Planet, Double> planetPos) {
		EnumMap<Planet, Boolean> planetDir = planetaryInfo.getPlanetDirection();
		Map<Planet, PlanetStatus> planetStatus = PlanetLabels.statuses(planetaryInfo);
		EnumMap<Planet, NakshathraPada> nakMap = planetaryInfo.getPlanetNakshathra();

		List<MapTableRow> rows = new ArrayList<MapTableRow>();
		Map<String, String> tooltips = new HashMap<String, String>();
		Map<String, String> copyTexts = new HashMap<String, String>();

		for (Planet p : sortedPlanets) {
			MapTableRow row = new MapTableRow();
			boolean isRetro = planetDir != null && planetDir.containsKey(p) && Boolean.TRUE.equals(planetDir.get(p));
			boolean isCombust = planetaryInfo.getPlanetCombustion() != null
					&& planetaryInfo.getPlanetCombustion().containsKey(p)
					&& Boolean.TRUE.equals(planetaryInfo.getPlanetCombustion().get(p));

			PlanetStatus status = p.isAsc() ? null : planetStatus.get(p);
			String name = PlanetLabels.label(p.name(), isRetro, isCombust, status);
			row.addColumn(AstrosoftTableColumn.Planet, name);

			double offset = (planetPos != null && planetPos.containsKey(p)) ? (planetPos.get(p) % 30.0) : 0.0;
			int[] dmsArr = AstroUtil.int_dms(offset);
			String dms = AstroUtil.twoDigit(dmsArr[0]) + ":" + AstroUtil.twoDigit(dmsArr[1]);
			Rasi r = (planetRasi != null) ? planetRasi.get(p) : null;
			if (r != null) {
				dms += " (" + r.toString() + ")";
			}
			row.addColumn(AstrosoftTableColumn.Longitude, dms);
			rows.add(row);

			// Build rich HTML tooltip for Positions
			NakshathraPada np = (nakMap != null) ? nakMap.get(p) : null;
			if (np == null && planetPos != null && planetPos.containsKey(p)) {
				np = new NakshathraPada(planetPos.get(p));
			}

			StringBuilder tt = new StringBuilder("<html><div style='padding: 2px 4px;'>");
			tt.append("<b style='color: #1a365d;'>").append(p.name()).append("</b>");
			if (r != null) {
				tt.append(" in <b>").append(r.name()).append("</b> (").append(r.toString()).append(")");
			}
			tt.append("<br><span style='color: #4a5568;'>Exact Longitude: ").append(dmsArr[0]).append("&deg; ")
			  .append(AstroUtil.twoDigit(dmsArr[1])).append("' ")
			  .append(AstroUtil.twoDigit(dmsArr[2])).append("\"</span>");
			tt.append("<hr style='border: 0; border-top: 1px solid #cbd5e0; margin: 3px 0;'>");

			StringBuilder pt = new StringBuilder();
			pt.append(p.name());
			if (r != null) {
				pt.append(" in ").append(r.name()).append(" (").append(r.toString()).append(")");
			}
			pt.append("\nExact Longitude: ").append(dmsArr[0]).append("° ")
			  .append(AstroUtil.twoDigit(dmsArr[1])).append("' ")
			  .append(AstroUtil.twoDigit(dmsArr[2])).append("\"\n");

			if (np != null) {
				Nakshathra nak = np.getNak();
				int pada = np.getPada();
				Planet starLord = NAK_LORDS[nak.ordinal() % 9];
				tt.append("<b>Nakshatra:</b> ").append(nak.name()).append(" (Pada ").append(pada).append(")<br>");
				tt.append("<b>Star Lord:</b> ").append(starLord.name()).append("<br>");
				pt.append("Nakshatra: ").append(nak.name()).append(" (Pada ").append(pada).append(")\n");
				pt.append("Star Lord: ").append(starLord.name()).append("\n");
			}

			List<String> states = new ArrayList<String>();
			if (isRetro) states.add("Retrograde (Vakra)");
			if (isCombust) states.add("Combust (Asta)");
			if (status != null) {
				if (status.getDignity() == Dignity.EXALTED) states.add("Exalted (Ucha)");
				else if (status.getDignity() == Dignity.DEBILITATED) states.add("Debilitated (Neecha)");
				else if (status.getDignity() == Dignity.MOOLATRIKONA) states.add("Moolatrikona");
				else if (status.getDignity() == Dignity.OWN) states.add("Own Sign (Swakshetra)");
				if (status.isVargottama()) states.add("Vargottama");
			}
			if (!states.isEmpty()) {
				tt.append("<b>Dignity / State:</b> ").append(String.join(", ", states));
				pt.append("Dignity / State: ").append(String.join(", ", states));
			} else if (!p.isAsc()) {
				tt.append("<b>Dignity:</b> Direct / Neutral");
				pt.append("Dignity: Direct / Neutral");
			}

			tt.append("</div></html>");
			tooltips.put(name, tt.toString());
			copyTexts.put(name, pt.toString().trim());
		}

		return buildSplitTablePanel(rows, AstrosoftTableColumn.Longitude, 120, 117, tooltips, copyTexts);
	}

	// -------------------------------------------------------------------------
	// Tab 2: Roles & Karakas (Bhava, Raman Nature & Jaimini Chara Karaka)
	// -------------------------------------------------------------------------
	private JPanel createRolesAndKarakasPanel(PlanetaryInfo planetaryInfo, List<Planet> sortedPlanets,
			EnumMap<Planet, Rasi> planetRasi, Rasi ascRasi, EnumMap<Planet, Double> planetPos) {
		EnumMap<Planet, Karaka> planetKaraka = planetaryInfo.getPlanetKaraka();
		List<MapTableRow> rows = new ArrayList<MapTableRow>();
		Map<String, String> tooltips = new HashMap<String, String>();
		Map<String, String> copyTexts = new HashMap<String, String>();

		for (Planet p : sortedPlanets) {
			MapTableRow row = new MapTableRow();
			row.addColumn(AstrosoftTableColumn.Planet, p.name());

			String roleDesc;
			if (p.isAsc()) {
				roleDesc = "<html>1st House (Lagna)</html>";
			} else {
				Rasi r = (planetRasi != null) ? planetRasi.get(p) : null;
				int bhava = (r != null && ascRasi != null) ? r.bhava(ascRasi) : 1;
				String roleTag = formatPlanetFunctionalRoleTag(ascRasi, p);
				Karaka k = (planetKaraka != null) ? planetKaraka.get(p) : null;
				String kAbbr = getKarakaAbbr(k);

				StringBuilder sb = new StringBuilder("<html>House ").append(bhava);
				if (roleTag != null) {
					sb.append("&nbsp;&nbsp;").append(roleTag);
				}
				if (!"—".equals(kAbbr)) {
					sb.append("&nbsp;&nbsp;<font color=\"#1a365d\"><b>").append(kAbbr).append("</b></font>");
				}
				sb.append("</html>");
				roleDesc = sb.toString();
			}
			row.addColumn(AstrosoftTableColumn.Bhava, roleDesc);
			rows.add(row);

			// Build rich HTML tooltip & plain text summary for Roles & Karakas (Layout 1)
			StringBuilder tt = new StringBuilder("<html><div style='padding: 2px 4px;'>");
			StringBuilder pt = new StringBuilder();

			if (p.isAsc()) {
				tt.append("<b style='color: #1a365d;'>Ascendant</b> &mdash; <b>1st House (Lagna / Tanu Bhava)</b><br>");
				pt.append("Ascendant — 1st House (Lagna / Tanu Bhava)\n");
				if (ascRasi != null) {
					tt.append("<span style='color: #4a5568;'>Sign: ").append(ascRasi.name())
					  .append(" (").append(ascRasi.toString()).append(")</span>");
					pt.append("Sign: ").append(ascRasi.name()).append(" (").append(ascRasi.toString()).append(")\n");
				}
				tt.append("<hr style='border: 0; border-top: 1px solid #cbd5e0; margin: 3px 0;'>");
				tt.append("Represents physical constitution, vitality, character, and foundational self.");
				pt.append("Represents physical constitution, vitality, character, and foundational self.");
			} else {
				String lordText = getLordshipText(ascRasi, p);
				String roleName = getFunctionalRoleName(ascRasi, p);
				String roleColor = getFunctionalRoleColor(ascRasi, p);

				// Line 1: Identity, Lordship & Functional Nature
				tt.append("<b style='color: #1a365d;'>").append(p.name()).append("</b>");
				pt.append(p.name());
				if (lordText != null) {
					tt.append(" &bull; ").append(lordText);
					pt.append(" • ").append(lordText);
				}
				if (roleName != null) {
					tt.append(" (<span style='color: ").append(roleColor).append("; font-weight: bold;'>").append(roleName).append("</span>)");
					pt.append(" (").append(roleName).append(")");
				}
				tt.append("<br>");
				pt.append("\n");

				// Line 2: Placement & Sign Relationship
				Rasi r = (planetRasi != null) ? planetRasi.get(p) : null;
				int bhava = (r != null && ascRasi != null) ? r.bhava(ascRasi) : 1;
				tt.append("<span style='color: #4a5568;'>Occupies <b>House ").append(bhava).append("</b>");
				pt.append("Occupies House ").append(bhava);
				if (r != null) {
					tt.append(", <b>").append(r.toString()).append("</b>");
					pt.append(", ").append(r.toString());
					SignRelationship rel = PlanetStatusCalculator.signRelationship(p, r);
					if (rel != null) {
						tt.append(" (").append(rel.label()).append(" sign)");
						pt.append(" (").append(rel.label()).append(" sign)");
					}
				}
				tt.append("</span>");
				tt.append("<hr style='border: 0; border-top: 1px solid #cbd5e0; margin: 3px 0;'>");
				pt.append("\n");

				// Line 3: Jaimini Chara Karaka
				Karaka k = (planetKaraka != null) ? planetKaraka.get(p) : null;
				String kPlain = getKarakaSignificationPlain(k);
				if (kPlain != null) {
					Double deg = (planetPos != null && planetPos.containsKey(p)) ? (planetPos.get(p) % 30.0) : null;
					String degHtml = (deg != null) ? (" (" + AstroUtil.twoDigit((int) (double) deg) + "&deg; in sign)") : "";
					String degPlain = (deg != null) ? (" (" + AstroUtil.twoDigit((int) (double) deg) + "° in sign)") : "";
					tt.append("<b>Jaimini Chara Karaka:</b> ").append(getKarakaSignification(k)).append(degHtml);
					pt.append("Jaimini Chara Karaka: ").append(kPlain).append(degPlain);
				} else if (p.isNode()) {
					tt.append("<b>Jaimini Chara Karaka:</b> Excluded (Classical 7-Karaka scheme)");
					pt.append("Jaimini Chara Karaka: Excluded (Classical 7-Karaka scheme)");
				}
			}
			tt.append("</div></html>");
			tooltips.put(p.name(), tt.toString());
			copyTexts.put(p.name(), pt.toString());
		}

		return buildSplitTablePanel(rows, AstrosoftTableColumn.Bhava, 80, 155, tooltips, copyTexts);
	}

	private static String ordinal(int n) {
		switch (n) {
			case 1: return "1st";
			case 2: return "2nd";
			case 3: return "3rd";
			default: return n + "th";
		}
	}

	private static String getLordshipText(Rasi ascRasi, Planet p) {
		if (p == null || p.isAsc() || p.isNode() || ascRasi == null) {
			return null;
		}
		List<Integer> bhavas = new ArrayList<>();
		for (Rasi r : Rasi.values()) {
			if (p.isOwnerOf(r)) {
				bhavas.add(r.bhava(ascRasi));
			}
		}
		Collections.sort(bhavas);
		if (bhavas.isEmpty()) {
			return null;
		}
		if (bhavas.size() == 1) {
			return "Lord of " + ordinal(bhavas.get(0));
		}
		return "Lord of " + ordinal(bhavas.get(0)) + " & " + ordinal(bhavas.get(1));
	}

	private static String getFunctionalRoleName(Rasi ascendant, Planet p) {
		if (p == null || p.isAsc() || ascendant == null) return null;
		if (PlanetCharacter.isYogakaraka(ascendant, p)) return "Yogakaraka";
		if (PlanetCharacter.isFunctionalBenefic(ascendant, p)) return "Functional Benefic";
		if (PlanetCharacter.isFunctionalMalefic(ascendant, p)) return "Functional Malefic";
		if (PlanetCharacter.isFunctionalNeutral(ascendant, p)) return "Functional Neutral";
		return null;
	}

	private static String getFunctionalRoleColor(Rasi ascendant, Planet p) {
		if (p == null || p.isAsc() || ascendant == null) return "#4a5568";
		if (PlanetCharacter.isYogakaraka(ascendant, p) || PlanetCharacter.isFunctionalBenefic(ascendant, p)) {
			return "#008000";
		}
		if (PlanetCharacter.isFunctionalMalefic(ascendant, p)) {
			return "#cc0000";
		}
		return "#666666";
	}

	private static String getKarakaAbbr(Karaka k) {
		if (k == null) return "—";
		switch (k) {
			case AtmaKaraka: return "AK";
			case AmatyaKaraka: return "AmK";
			case BhratruKaraka: return "BK";
			case MatruKaraka: return "MK";
			case PuthraKaraka: return "PK";
			case GnatiKaraka: return "GK";
			case DaraKaraka: return "DK";
			default: return "—";
		}
	}

	private static String getKarakaSignification(Karaka k) {
		String plain = getKarakaSignificationPlain(k);
		return plain != null ? plain.replace("—", "&mdash;") : null;
	}

	private static String getKarakaSignificationPlain(Karaka k) {
		if (k == null) return null;
		switch (k) {
			case AtmaKaraka:
				return "Atmakaraka (AK) — Soul significator, self, and prime life lessons (Rank 1)";
			case AmatyaKaraka:
				return "Amatyakaraka (AmK) — Career, intellect, status, and profession (Rank 2)";
			case BhratruKaraka:
				return "Bhratrukaraka (BK) — Siblings, courage, motivation, and guru (Rank 3)";
			case MatruKaraka:
				return "Matrukaraka (MK) — Mother, emotional mind, education, and property (Rank 4)";
			case PuthraKaraka:
				return "Putrakaraka (PK) — Children, wisdom, intellect, and creativity (Rank 5)";
			case GnatiKaraka:
				return "Gnatikaraka (GK) — Obstacles, rivals, disease, and disputes (Rank 6)";
			case DaraKaraka:
				return "Darakaraka (DK) — Spouse, marital partner, and trade (Rank 7)";
			default:
				return null;
		}
	}

	// -------------------------------------------------------------------------
	// Tab 4: Shadbala Colored Horizontal Bar Panel
	// -------------------------------------------------------------------------
	private class ShadbalaBarPanel extends JPanel {
		private final List<ShadBalaBarView.PlanetBalaItem> items = new ArrayList<>();
		private double scaleMax = 100.0;
		private int hoveredIndex = -1;

		private static final int TRACK_X = 66;
		private static final int PCT_WIDTH = 55;
		private static final int START_Y = 14;
		private static final int ROW_HEIGHT = 18;
		private static final int BAR_HEIGHT = 12;

		private final DecimalFormat pctFmt = new DecimalFormat("0.0'%'");
		private final DecimalFormat valFmt = new DecimalFormat("0.00");
		private final DecimalFormat rupaFmt = new DecimalFormat("0.00");

		public ShadbalaBarPanel(ShadBala shadBala) {
			setOpaque(false);
			if (shadBala != null) {
				List<ShadBalaBarView.PlanetBalaItem> extracted = ShadBalaBarView.extractItems(shadBala);
				double maxP = 0.0;
				for (ShadBalaBarView.PlanetBalaItem item : extracted) {
					items.add(item);
					if (item.balaPercent > maxP) {
						maxP = item.balaPercent;
					}
				}
				// Traditional astrological order: Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn
				Collections.sort(items, Comparator.comparingInt(o -> o.planet.ordinal()));
				scaleMax = Math.max(100.0, maxP);
			}

			ToolTipManager.sharedInstance().registerComponent(this);

			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					int prev = hoveredIndex;
					hoveredIndex = getRowAt(e.getPoint());
					if (prev != hoveredIndex) {
						repaint();
					}
				}
			});
		}

		private int getRowAt(Point pt) {
			if (pt.y >= START_Y && pt.y < START_Y + items.size() * ROW_HEIGHT) {
				return (pt.y - START_Y) / ROW_HEIGHT;
			}
			return -1;
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			int index = getRowAt(e.getPoint());
			if (index >= 0 && index < items.size()) {
				ShadBalaBarView.PlanetBalaItem item = items.get(index);
				String rankStr = item.rank > 0 ? Roman.of(item.rank).toString() : "-";
				String minRupaStr = rupaFmt.format(item.minVirupas / 60.0);
				String statusColor = item.balaPercent >= 100.0 ? "#006400" : "#B22222";

				StringBuilder sb = new StringBuilder();
				sb.append("<html><div style='padding:4px; font-family:Verdana,sans-serif; font-size:11px;'>");
				sb.append("<b style='color:#00008C; font-size:12px;'>").append(item.planet.name()).append("</b>");
				sb.append("&nbsp;<span style='color:#800000;'>(").append(rankStr).append(")</span>");
				sb.append("<hr style='border:0; border-top:1px solid #CCC; margin:4px 0;'>");
				sb.append("<table cellpadding='1' cellspacing='0'>");
				sb.append("<tr><td><b>Shadbala:</b>&nbsp;</td><td>").append(valFmt.format(item.virupas)).append(" Virupas</td></tr>");
				sb.append("<tr><td><b>Rupa:</b>&nbsp;</td><td>").append(rupaFmt.format(item.rupa)).append("</td></tr>");
				sb.append("<tr><td><b>Bala %:</b>&nbsp;</td><td><b style='color:").append(statusColor).append(";'>")
				  .append(pctFmt.format(item.balaPercent)).append("</b></td></tr>");
				sb.append("<tr><td><b>Required:</b>&nbsp;</td><td>").append(valFmt.format(item.minVirupas)).append(" Virupas (")
				  .append(minRupaStr).append(" Rupa)</td></tr>");
				sb.append("</table></div></html>");
				return sb.toString();
			}
			return null;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (items.isEmpty()) {
				g.setColor(Color.GRAY);
				g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
				g.drawString("Shadbala data calculated when chart is loaded", 130, 70);
				return;
			}

			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			int currentW = getWidth();
			if (currentW <= 0) currentW = 494;
			int trackWidth = Math.max(80, currentW - TRACK_X - PCT_WIDTH);

			// 100% reference line X
			int x100 = TRACK_X + (int) Math.round((100.0 / scaleMax) * trackWidth);
			int lineTop = START_Y - 2;
			int lineBottom = START_Y + items.size() * ROW_HEIGHT;

			// Draw 100% dashed reference guideline
			Stroke origStroke = g2.getStroke();
			Stroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f, 3.0f}, 0.0f);
			g2.setStroke(dashed);
			g2.setColor(new Color(185, 170, 150));
			g2.drawLine(x100, lineTop, x100, lineBottom);
			g2.setStroke(origStroke);

			// 100% Min marker label
			g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
			g2.setColor(new Color(140, 120, 100));
			String markStr = "100% Min";
			FontMetrics fmMark = g2.getFontMetrics();
			g2.drawString(markStr, x100 - fmMark.stringWidth(markStr) / 2, lineTop - 2);

			Font labelFont = new Font(Font.SANS_SERIF, Font.BOLD, 10);
			Font pctFont = new Font(Font.SANS_SERIF, Font.BOLD, 10);
			FontMetrics fmLabel = g2.getFontMetrics(labelFont);

			for (int i = 0; i < items.size(); i++) {
				ShadBalaBarView.PlanetBalaItem item = items.get(i);
				int rowY = START_Y + i * ROW_HEIGHT;
				int barY = rowY + (ROW_HEIGHT - BAR_HEIGHT) / 2;

				// Subtle hover background highlight
				if (i == hoveredIndex) {
					g2.setColor(new Color(230, 240, 255, 120));
					g2.fillRoundRect(2, rowY, currentW - 4, ROW_HEIGHT, 4, 4);
				}

				// Planet Name (Right-aligned at TRACK_X - 6)
				g2.setFont(labelFont);
				if (item.planet == Planet.Moon) {
					g2.setColor(UIConsts.getChartMoonColor());
				} else {
					g2.setColor(UIConsts.getChartPlanetForeground());
				}
				String pName = item.planet.name();
				int nameW = fmLabel.stringWidth(pName);
				g2.drawString(pName, TRACK_X - 6 - nameW, barY + BAR_HEIGHT - 2);

				// Background gray track
				g2.setColor(new Color(238, 238, 240));
				g2.fillRoundRect(TRACK_X, barY, trackWidth, BAR_HEIGHT, 4, 4);

				// Colored horizontal bar
				int fillW = (int) Math.round((item.balaPercent / scaleMax) * trackWidth);
				fillW = Math.max(3, Math.min(fillW, trackWidth));

				Color barColor = ShadBalaBarView.getVedicPlanetColor(item.planet);
				g2.setColor(barColor);
				g2.fillRoundRect(TRACK_X, barY, fillW, BAR_HEIGHT, 4, 4);

				// Glossy top edge highlight
				g2.setColor(new Color(255, 255, 255, 45));
				g2.fillRect(TRACK_X + 1, barY + 1, fillW - 2, BAR_HEIGHT / 2);

				// Percentage label on the right
				g2.setFont(pctFont);
				boolean isStrong = item.balaPercent >= 100.0;
				g2.setColor(isStrong ? new Color(20, 130, 40) : new Color(200, 35, 35));
				String pctStr = pctFmt.format(item.balaPercent);
				g2.drawString(pctStr, TRACK_X + trackWidth + 8, barY + BAR_HEIGHT - 2);
			}

			g2.dispose();
		}
	}

	// -------------------------------------------------------------------------
	// Split 2-Column Table Panel Helper
	// -------------------------------------------------------------------------
	private JPanel buildSplitTablePanel(List<MapTableRow> rows, AstrosoftTableColumn valueColumn,
			int col0Width, int col1Width) {
		return buildSplitTablePanel(rows, valueColumn, col0Width, col1Width, null, null);
	}

	private JPanel buildSplitTablePanel(List<MapTableRow> rows, AstrosoftTableColumn valueColumn,
			int col0Width, int col1Width, final Map<String, String> tooltips) {
		return buildSplitTablePanel(rows, valueColumn, col0Width, col1Width, tooltips, null);
	}

	private JPanel buildSplitTablePanel(List<MapTableRow> rows, AstrosoftTableColumn valueColumn,
			int col0Width, int col1Width, final Map<String, String> tooltips, final Map<String, String> copyTexts) {
		JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
		panel.setOpaque(false);

		ListTableData<MapTableRow> tableData = new ListTableData<MapTableRow>(rows);
		DefaultColumnMetaData colMetaData = new DefaultColumnMetaData(AstrosoftTableColumn.Planet, valueColumn);
		List<ListTableData<MapTableRow>> splitData = TableDataFactory.splitTableData(tableData, 5);

		for (ListTableData<MapTableRow> data : splitData) {
			AstrosoftTable table = new AstrosoftTable(new AstrosoftTableModel(data, colMetaData), TableStyle.NONE) {
				@Override
				public String getToolTipText(MouseEvent event) {
					if (tooltips != null) {
						int row = rowAtPoint(event.getPoint());
						if (row >= 0 && row < data.getRowCount()) {
							MapTableRow r = data.getRow(row);
							Object pObj = r.getColumnData(AstrosoftTableColumn.Planet);
							if (pObj != null) {
								String tt = tooltips.get(pObj.toString());
								if (tt != null) return tt;
							}
						}
					}
					return super.getToolTipText(event);
				}
			};
			table.setOpaque(false);
			table.setRowHeight(22);
			ToolTipManager.sharedInstance().registerComponent(table);

			if (copyTexts != null) {
				MouseAdapter popupListener = new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						if (e.isPopupTrigger()) {
							showPopup(e);
						}
					}

					@Override
					public void mouseReleased(MouseEvent e) {
						if (e.isPopupTrigger()) {
							showPopup(e);
						}
					}

					private void showPopup(MouseEvent e) {
						int row = table.rowAtPoint(e.getPoint());
						if (row >= 0 && row < data.getRowCount()) {
							table.setRowSelectionInterval(row, row);
							MapTableRow r = data.getRow(row);
							Object pObj = r.getColumnData(AstrosoftTableColumn.Planet);
							if (pObj != null) {
								final String textToCopy = copyTexts.get(pObj.toString());
								if (textToCopy != null) {
									JPopupMenu popup = new JPopupMenu();
									JMenuItem copyItem = new JMenuItem("Copy Planet Summary");
									copyItem.addActionListener(evt -> {
										StringSelection sel = new StringSelection(textToCopy);
										Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
									});
									popup.add(copyItem);
									popup.show(e.getComponent(), e.getX(), e.getY());
								}
							}
						}
					}
				};
				table.addMouseListener(popupListener);
			}

			javax.swing.table.TableColumn pCol = table.getColumnModel().getColumn(0);
			javax.swing.table.TableColumn vCol = table.getColumnModel().getColumn(1);
			pCol.setPreferredWidth(col0Width);
			pCol.setMaxWidth(col0Width + 15);
			vCol.setPreferredWidth(col1Width);

			pCol.setCellRenderer(new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
					Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
					comp.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
					String text = (val != null) ? val.toString() : "";
					if (text.startsWith("Ascendant")) {
						comp.setForeground(UIConsts.getChartAscendantColor());
					} else if (text.startsWith("Moon")) {
						comp.setForeground(UIConsts.getChartMoonColor());
					} else {
						comp.setForeground(UIConsts.getChartPlanetForeground());
					}
					setOpaque(false);
					return comp;
				}
			});

			vCol.setCellRenderer(new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
					Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
					comp.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
					comp.setForeground(UIConsts.TEXT_DARK_NAVY);
					setOpaque(false);
					return comp;
				}
			});

			panel.add(table);
		}

		return panel;
	}

	// -------------------------------------------------------------------------
	// Subtle Rounded Pill Tabs Header Component
	// -------------------------------------------------------------------------
	private class PillTabBar extends JPanel {
		private final String[] tabs = {"Positions", "Roles & Karakas", "Shadbala"};
		private int selectedTab = 0;
		private final Consumer<Integer> onTabSelected;
		private final List<JButton> buttons = new ArrayList<JButton>();

		public PillTabBar(Consumer<Integer> onTabSelected) {
			this.onTabSelected = onTabSelected;
			setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
			setOpaque(false);

			for (int i = 0; i < tabs.length; i++) {
				final int idx = i;
				JButton btn = new JButton(tabs[i]) {
					@Override
					protected void paintComponent(Graphics g) {
						Graphics2D g2 = (Graphics2D) g.create();
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						boolean isSelected = (idx == selectedTab);
						boolean isHover = getModel().isRollover();

						int w = getWidth();
						int h = getHeight();
						int arc = 12;

						if (isSelected) {
							// Active pill: solid deep navy with subtle dark border
							g2.setColor(new Color(44, 82, 130));
							g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
							g2.setColor(new Color(30, 60, 95));
							g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
							setForeground(Color.WHITE);
						} else if (isHover) {
							// Hover pill: soft sky-blue fill with defined navy border
							g2.setColor(new Color(228, 238, 250));
							g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
							g2.setColor(new Color(110, 140, 175));
							g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
							setForeground(new Color(25, 45, 75));
						} else {
							// Inactive pill: crisp solid white fill with defined steel-slate border & high-contrast dark text
							g2.setColor(Color.WHITE);
							g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
							g2.setColor(new Color(145, 165, 188));
							g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
							setForeground(new Color(30, 50, 80));
						}

						FontMetrics fm = g2.getFontMetrics(getFont());
						int textW = fm.stringWidth(getText());
						int textH = fm.getAscent();
						int textX = (w - textW) / 2;
						int textY = (h + textH) / 2 - 2;

						g2.setColor(getForeground());
						g2.setFont(getFont());
						g2.drawString(getText(), textX, textY);
						g2.dispose();
					}
				};

				btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
				btn.setMargin(new java.awt.Insets(0, 0, 0, 0));
				btn.setFocusPainted(false);
				btn.setBorderPainted(false);
				btn.setContentAreaFilled(false);
				btn.setOpaque(false);
				btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				FontMetrics fm = btn.getFontMetrics(btn.getFont());
				int textW = fm.stringWidth(tabs[i]);
				btn.setPreferredSize(new Dimension(textW + 16, 22));

				btn.addActionListener(e -> {
					selectedTab = idx;
					for (JButton b : buttons) {
						b.repaint();
					}
					if (onTabSelected != null) {
						onTabSelected.accept(idx);
					}
				});

				buttons.add(btn);
				add(btn);
			}
		}

		public void selectTab(int idx) {
			if (idx >= 0 && idx < buttons.size()) {
				selectedTab = idx;
				for (JButton b : buttons) {
					b.repaint();
				}
				if (onTabSelected != null) {
					onTabSelected.accept(idx);
				}
			}
		}
	}

	private static String formatPlanetFunctionalRoleTag(Rasi ascendant, Planet p) {
		if (p == null || p.isAsc() || ascendant == null) {
			return null;
		}
		if (PlanetCharacter.isYogakaraka(ascendant, p)) {
			return "<font color=\"green\"><b>[Yog]</b></font>";
		} else if (PlanetCharacter.isFunctionalBenefic(ascendant, p)) {
			return "<font color=\"green\"><b>[Ben]</b></font>";
		} else if (PlanetCharacter.isFunctionalMalefic(ascendant, p)) {
			return "<font color=\"red\"><b>[Mal]</b></font>";
		} else if (PlanetCharacter.isFunctionalNeutral(ascendant, p)) {
			return "<font color=\"#888888\">[Neu]</font>";
		}
		return null;
	}

	private static String formatPlanetShort(Planet p) {
		if (p == null) return "";
		switch (p) {
			case Sun: return "Sun";
			case Moon: return "Moon";
			case Mars: return "Mars";
			case Mercury: return "Mer";
			case Jupiter: return "Jup";
			case Venus: return "Ven";
			case Saturn: return "Sat";
			case Rahu: return "Rah";
			case Ketu: return "Ket";
			case Ascendant: return "Asc";
			default: return p.name().substring(0, Math.min(3, p.name().length()));
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int currentW = getWidth();
		if (currentW <= 0) currentW = CARD_WIDTH;
		int currentH = getHeight();
		if (currentH <= 0) currentH = CARD_HEIGHT;

		int arc = 12;
		int gap = 8;

		int boxLeft = 2;
		int boxRight = currentW - 4;
		int boxTop = 12;
		int boxBottom = currentH - 5;

		// Card background
		g2.setColor(UIConsts.getChartBackground());
		g2.fillRoundRect(boxLeft, boxTop, boxRight - boxLeft, boxBottom - boxTop, arc, arc);

		title.setForeground(UIConsts.getChartPlanetForeground());

		FontMetrics fm = title.getFontMetrics(title.getFont());
		int textW = fm.stringWidth(title.getText());
		int textX = 20;

		Path2D p = new Path2D.Double();
		// Top-left corner to title cutout
		p.moveTo(boxLeft + arc, boxTop);
		p.lineTo(Math.max(boxLeft + arc, textX - gap), boxTop);

		// Top line from title cutout to right corner
		p.moveTo(textX + textW + gap, boxTop);
		p.lineTo(boxRight - arc, boxTop);

		// Top-right rounded corner
		p.quadTo(boxRight, boxTop, boxRight, boxTop + arc);

		// Right vertical border
		p.lineTo(boxRight, boxBottom - arc);

		// Bottom-right rounded corner
		p.quadTo(boxRight, boxBottom, boxRight - arc, boxBottom);

		// Bottom horizontal border
		p.lineTo(boxLeft + arc, boxBottom);

		// Bottom-left rounded corner
		p.quadTo(boxLeft, boxBottom, boxLeft, boxBottom - arc);

		// Left vertical border
		p.lineTo(boxLeft, boxTop + arc);

		// Top-left rounded corner
		p.quadTo(boxLeft, boxTop, boxLeft + arc, boxTop);

		g2.setColor(UIConsts.getChartBorderColor());
		g2.setStroke(new BasicStroke(1.5f));
		g2.draw(p);

		// Subtle bottom-center grip handle indicating vertical resizability
		int gripW = 40;
		int gripX = (currentW - gripW) / 2;
		int gripY = boxBottom - 2;
		g2.setColor(new Color(160, 180, 205));
		g2.fillRoundRect(gripX, gripY - 1, gripW, 3, 2, 2);

		g2.dispose();
	}
}
