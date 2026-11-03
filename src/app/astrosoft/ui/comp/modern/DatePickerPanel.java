package app.astrosoft.ui.comp.modern;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import app.astrosoft.ui.util.UIConsts;

/**
 * Modern, beautifully-proportioned Date Picker Panel.
 * Aligned with Options -> Chart Themes (Warm Ivory, Soft Alabaster, Calm Sage, etc.),
 * featuring high-contrast rounded cards, modern sleek buttons (no rigid boxes),
 * and custom antialiased rendering immune to Look and Feel overrides.
 */
public class DatePickerPanel extends JPanel {

    public static final int MIN_YEAR = 1800;
    public static final int MAX_YEAR = 2100;

    private final UIConsts.ChartTheme theme;
    private final Color primaryColor;
    private final Color cellBg;
    private final Color cellBorder;
    private final Color cellText;
    private final Color dowColor;

    private LocalDate selectedDate;
    private YearMonth viewYearMonth;
    private boolean confirmed = false;
    private Runnable onConfirmListener;
    private Runnable onCancelListener;

    // UI elements
    private JLabel headerYearLabel;
    private JLabel headerDateLabel;
    private JComboBox<Month> monthCombo;
    private JComboBox<Integer> yearCombo;
    private JPanel daysGridPanel;
    private DayButton[] dayButtons = new DayButton[42];

    public DatePickerPanel(LocalDate initialDate) {
        this.theme = UIConsts.getChartTheme();
        this.primaryColor = theme.getPrimaryAccentColor();
        this.cellBg = theme.getBackground();
        this.cellBorder = theme.getBorderColor();
        this.cellText = theme.getPlanetForeground();
        this.dowColor = theme.getLabelColor();

        this.selectedDate = (initialDate != null) ? initialDate : LocalDate.now();
        this.viewYearMonth = YearMonth.of(selectedDate.getYear(), selectedDate.getMonth());

        initComponents();
        updateHeaderAndGrid();
        setPreferredSize(new Dimension(315, 375));
    }

    public void setOnConfirmListener(Runnable r) {
        this.onConfirmListener = r;
    }

    public void setOnCancelListener(Runnable r) {
        this.onCancelListener = r;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        if (date != null) {
            this.selectedDate = date;
            this.viewYearMonth = YearMonth.of(date.getYear(), date.getMonth());
            updateHeaderAndGrid();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. Top Header Banner styled with theme primary accent
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        headerYearLabel = new JLabel(String.valueOf(selectedDate.getYear()));
        headerYearLabel.setForeground(new Color(255, 255, 255, 210));
        headerYearLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        headerDateLabel = new JLabel(selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")));
        headerDateLabel.setForeground(Color.WHITE);
        headerDateLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        headerPanel.add(headerYearLabel);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(headerDateLabel);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content (Navigation bar + Day Headers + Compact Grid)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 4));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 4, 10));

        // Month / Year Navigation Bar (Sleek controls)
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        navBar.setBackground(Color.WHITE);

        JButton prevBtn = createNavArrow("<");
        prevBtn.addActionListener(e -> navigateMonth(-1));

        monthCombo = new JComboBox<>(Month.values());
        monthCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        monthCombo.setBackground(Color.WHITE);
        monthCombo.setForeground(cellText);
        monthCombo.setSelectedItem(viewYearMonth.getMonth());
        monthCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("SansSerif", Font.PLAIN, 12));
                if (isSelected) {
                    l.setBackground(primaryColor);
                    l.setForeground(Color.WHITE);
                } else {
                    l.setBackground(Color.WHITE);
                    l.setForeground(cellText);
                }
                return l;
            }
        });
        monthCombo.addActionListener(e -> {
            Month m = (Month) monthCombo.getSelectedItem();
            if (m != null && m != viewYearMonth.getMonth()) {
                viewYearMonth = YearMonth.of(viewYearMonth.getYear(), m);
                updateDaysGrid();
            }
        });

        Integer[] years = new Integer[MAX_YEAR - MIN_YEAR + 1];
        for (int y = MIN_YEAR; y <= MAX_YEAR; y++) {
            years[y - MIN_YEAR] = y;
        }
        yearCombo = new JComboBox<>(years);
        yearCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        yearCombo.setBackground(Color.WHITE);
        yearCombo.setForeground(cellText);
        yearCombo.setSelectedItem(viewYearMonth.getYear());
        yearCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("SansSerif", Font.PLAIN, 12));
                if (isSelected) {
                    l.setBackground(primaryColor);
                    l.setForeground(Color.WHITE);
                } else {
                    l.setBackground(Color.WHITE);
                    l.setForeground(cellText);
                }
                return l;
            }
        });
        yearCombo.addActionListener(e -> {
            Integer y = (Integer) yearCombo.getSelectedItem();
            if (y != null && y != viewYearMonth.getYear()) {
                viewYearMonth = YearMonth.of(y, viewYearMonth.getMonth());
                updateDaysGrid();
            }
        });

        JButton nextBtn = createNavArrow(">");
        nextBtn.addActionListener(e -> navigateMonth(1));

        navBar.add(prevBtn);
        navBar.add(monthCombo);
        navBar.add(yearCombo);
        navBar.add(nextBtn);
        centerPanel.add(navBar, BorderLayout.NORTH);

        // Calendar Container
        JPanel calendarContainer = new JPanel(new BorderLayout(0, 4));
        calendarContainer.setBackground(Color.WHITE);

        // Day of Week Header row
        JPanel dowPanel = new JPanel(new GridLayout(1, 7, 3, 0));
        dowPanel.setBackground(Color.WHITE);
        DayOfWeek[] daysOfWeek = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};
        for (DayOfWeek dow : daysOfWeek) {
            JLabel dowLabel = new JLabel(dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).substring(0, 2), SwingConstants.CENTER);
            dowLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            dowLabel.setForeground(dowColor);
            dowPanel.add(dowLabel);
        }
        calendarContainer.add(dowPanel, BorderLayout.NORTH);

        // Days Grid (6 rows x 7 cols = 42 slots, tight square cells)
        daysGridPanel = new JPanel(new GridLayout(6, 7, 3, 3));
        daysGridPanel.setBackground(Color.WHITE);
        for (int i = 0; i < 42; i++) {
            final int slot = i;
            DayButton dayBtn = new DayButton();
            dayBtn.addActionListener(e -> onDayClicked(slot));
            dayBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && dayBtn.isEnabled() && !dayBtn.getText().isEmpty()) {
                        confirmed = true;
                        if (onConfirmListener != null) onConfirmListener.run();
                    }
                }
            });
            dayButtons[i] = dayBtn;
            daysGridPanel.add(dayBtn);
        }
        calendarContainer.add(daysGridPanel, BorderLayout.CENTER);
        centerPanel.add(calendarContainer, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Action Bar (Today, Cancel, OK) styled with ModernButton
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));

        ModernButton todayBtn = ModernButton.createSecondary("Today");
        todayBtn.setPreferredSize(new Dimension(72, 28));
        todayBtn.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            selectedDate = today;
            viewYearMonth = YearMonth.of(today.getYear(), today.getMonth());
            updateHeaderAndGrid();
        });
        bottomBar.add(todayBtn, BorderLayout.WEST);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setBackground(Color.WHITE);

        ModernButton cancelBtn = ModernButton.createSecondary("Cancel");
        cancelBtn.setPreferredSize(new Dimension(72, 28));
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            if (onCancelListener != null) onCancelListener.run();
        });

        ModernButton okBtn = ModernButton.createPrimary("Ok", primaryColor);
        okBtn.setPreferredSize(new Dimension(72, 28));
        okBtn.addActionListener(e -> {
            confirmed = true;
            if (onConfirmListener != null) onConfirmListener.run();
        });

        actionsPanel.add(cancelBtn);
        actionsPanel.add(okBtn);
        bottomBar.add(actionsPanel, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private JButton createNavArrow(String arrow) {
        JButton btn = new JButton(arrow) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                boolean pressed = getModel().isPressed();
                boolean rollover = getModel().isRollover();

                Color bg = pressed ? new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), 35)
                                   : (rollover ? new Color(241, 245, 249) : Color.WHITE);
                g2.setColor(bg);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

                g2.setColor(rollover ? primaryColor : new Color(203, 213, 225));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

                g2.setColor(rollover ? primaryColor : cellText);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(30, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void navigateMonth(int delta) {
        YearMonth next = viewYearMonth.plusMonths(delta);
        if (next.getYear() >= MIN_YEAR && next.getYear() <= MAX_YEAR) {
            viewYearMonth = next;
            updateDaysGrid();
        }
    }

    private void onDayClicked(int slot) {
        String txt = dayButtons[slot].getText();
        if (txt != null && !txt.isEmpty()) {
            int day = Integer.parseInt(txt);
            selectedDate = LocalDate.of(viewYearMonth.getYear(), viewYearMonth.getMonth(), day);
            updateHeaderAndGrid();
        }
    }

    private void updateHeaderAndGrid() {
        if (headerYearLabel != null) {
            headerYearLabel.setText(String.valueOf(selectedDate.getYear()));
            headerDateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")));
        }
        updateDaysGrid();
    }

    private void updateDaysGrid() {
        if (monthCombo == null || yearCombo == null) return;

        monthCombo.setSelectedItem(viewYearMonth.getMonth());
        yearCombo.setSelectedItem(viewYearMonth.getYear());

        LocalDate firstOfMonth = viewYearMonth.atDay(1);
        int daysInMonth = viewYearMonth.lengthOfMonth();
        int firstDayOffset = firstOfMonth.getDayOfWeek().getValue() - 1;

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 42; i++) {
            DayButton btn = dayButtons[i];
            int dayNum = i - firstDayOffset + 1;

            if (dayNum >= 1 && dayNum <= daysInMonth) {
                btn.setText(String.valueOf(dayNum));
                btn.setEnabled(true);
                btn.setVisible(true);

                boolean isSelected = (viewYearMonth.getYear() == selectedDate.getYear()
                        && viewYearMonth.getMonth() == selectedDate.getMonth()
                        && dayNum == selectedDate.getDayOfMonth());

                boolean isToday = (viewYearMonth.getYear() == today.getYear()
                        && viewYearMonth.getMonth() == today.getMonth()
                        && dayNum == today.getDayOfMonth());

                btn.setSelectedState(isSelected, isToday);
            } else {
                btn.setText("");
                btn.setEnabled(false);
                btn.setVisible(false);
                btn.setSelectedState(false, false);
            }
        }
        daysGridPanel.revalidate();
        daysGridPanel.repaint();
    }

    /**
     * Custom Day Button that renders a pleasing themed rounded card cell with high contrast,
     * beautiful hover, and unmistakable focus on the selected date.
     * Aligned with Options -> Chart Themes.
     */
    private class DayButton extends JButton {
        private boolean isSelectedDay = false;
        private boolean isToday = false;
        private boolean isHovered = false;

        public DayButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            setFont(new Font("SansSerif", Font.PLAIN, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled() && !getText().isEmpty()) {
                        isHovered = true;
                        repaint();
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (isHovered) {
                        isHovered = false;
                        repaint();
                    }
                }
            });
        }

        public void setSelectedState(boolean sel, boolean today) {
            this.isSelectedDay = sel;
            this.isToday = today;
            setFont(new Font("SansSerif", (sel || today) ? Font.BOLD : Font.PLAIN, 12));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            String text = getText();
            if (text == null || text.isEmpty()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color textColor;

            if (isSelectedDay) {
                // Focus: Solid theme primary accent card with outer glow ring
                g2.setColor(primaryColor);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);

                // Focus accent ring
                Color glow = new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), 110);
                g2.setColor(glow);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 9, 9);

                textColor = Color.WHITE;
                g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
            } else if (isHovered) {
                // Delightful theme-tinted hover
                Color hoverBg = new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), 35);
                g2.setColor(hoverBg);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);

                g2.setColor(primaryColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, 8, 8);

                textColor = primaryColor;
                g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
            } else if (isToday) {
                // Today: soft theme-tinted cell with accent border
                Color todayBg = new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), 20);
                g2.setColor(todayBg);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);

                g2.setColor(primaryColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, 8, 8);

                textColor = primaryColor;
                g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
            } else {
                // Normal unselected day: themed card background & border
                g2.setColor(cellBg);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);

                Color borderSoft = new Color(cellBorder.getRed(), cellBorder.getGreen(), cellBorder.getBlue(), 120);
                g2.setColor(borderSoft);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, 8, 8);

                textColor = cellText;
                g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            }

            // Draw crisp centered text
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(text)) / 2;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, tx, ty);

            g2.dispose();
        }
    }
}
