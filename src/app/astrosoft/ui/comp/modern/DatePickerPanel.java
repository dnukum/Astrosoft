package app.astrosoft.ui.comp.modern;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Modern, beautifully-proportioned Material Date Picker Panel.
 * Features a circular date selection indicator, compact 320px column spacing,
 * month/year navigation, and clean action buttons.
 */
public class DatePickerPanel extends JPanel {

    public static final int MIN_YEAR = 1800;
    public static final int MAX_YEAR = 2100;

    private static final Color PRIMARY_BLUE = new Color(25, 118, 210); // #1976D2
    private static final Color HEADER_TEXT_LIGHT = new Color(210, 228, 255);
    private static final Color HOVER_BG = new Color(238, 242, 246);
    private static final Color MUTED_GRAY = new Color(110, 115, 125);
    private static final Color BORDER_GRAY = new Color(218, 220, 224);

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
        this.selectedDate = (initialDate != null) ? initialDate : LocalDate.now();
        this.viewYearMonth = YearMonth.of(selectedDate.getYear(), selectedDate.getMonth());

        initComponents();
        updateHeaderAndGrid();
        setPreferredSize(new Dimension(300, 360));
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

        // 1. Top Header Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        headerYearLabel = new JLabel(String.valueOf(selectedDate.getYear()));
        headerYearLabel.setForeground(HEADER_TEXT_LIGHT);
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
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        navBar.setBackground(Color.WHITE);

        JButton prevBtn = createNavArrow("<");
        prevBtn.addActionListener(e -> navigateMonth(-1));

        monthCombo = new JComboBox<>(Month.values());
        monthCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        monthCombo.setSelectedItem(viewYearMonth.getMonth());
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
        yearCombo.setSelectedItem(viewYearMonth.getYear());
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
        JPanel calendarContainer = new JPanel(new BorderLayout(0, 2));
        calendarContainer.setBackground(Color.WHITE);

        // Day of Week Header row
        JPanel dowPanel = new JPanel(new GridLayout(1, 7, 0, 0));
        dowPanel.setBackground(Color.WHITE);
        DayOfWeek[] daysOfWeek = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};
        for (DayOfWeek dow : daysOfWeek) {
            JLabel dowLabel = new JLabel(dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).substring(0, 2), SwingConstants.CENTER);
            dowLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            dowLabel.setForeground(new Color(110, 120, 135));
            dowPanel.add(dowLabel);
        }
        calendarContainer.add(dowPanel, BorderLayout.NORTH);

        // Days Grid (6 rows x 7 cols = 42 slots, tight square cells)
        daysGridPanel = new JPanel(new GridLayout(6, 7, 2, 2));
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

        // 3. Bottom Action Bar (Today, Cancel, OK)
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));

        JButton todayBtn = new JButton("Today");
        todayBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        todayBtn.setFocusPainted(false);
        todayBtn.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            selectedDate = today;
            viewYearMonth = YearMonth.of(today.getYear(), today.getMonth());
            updateHeaderAndGrid();
        });
        bottomBar.add(todayBtn, BorderLayout.WEST);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setBackground(Color.WHITE);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            if (onCancelListener != null) onCancelListener.run();
        });

        JButton okBtn = new JButton("Ok");
        okBtn.setBackground(PRIMARY_BLUE);
        okBtn.setForeground(Color.WHITE);
        okBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        okBtn.setFocusPainted(false);
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
        JButton btn = new JButton(arrow);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(30, 24));
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
     * Custom JButton that renders a clean circular selection indicator
     * instead of a clunky full-rectangle block.
     */
    private static class DayButton extends JButton {
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
                    isHovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
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
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            if (isSelectedDay) {
                g2.setColor(PRIMARY_BLUE);
                g2.fillOval(x, y, size, size);
                setForeground(Color.WHITE);
            } else if (isHovered && isEnabled() && !getText().isEmpty()) {
                g2.setColor(HOVER_BG);
                g2.fillOval(x, y, size, size);
                setForeground(Color.DARK_GRAY);
            } else if (isToday) {
                g2.setColor(PRIMARY_BLUE);
                g2.drawOval(x, y, size - 1, size - 1);
                setForeground(PRIMARY_BLUE);
            } else {
                setForeground(Color.DARK_GRAY);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
