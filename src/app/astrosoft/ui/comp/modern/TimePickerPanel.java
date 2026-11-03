package app.astrosoft.ui.comp.modern;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import app.astrosoft.ui.util.UIConsts;

/**
 * Modern Time Picker Panel aligned with Options -> Chart Themes.
 * Features 4-column scrollable lists (Hour, Min, Sec, AM/PM), live preview banner,
 * and non-boxy modern rounded action buttons. Headless-safe.
 */
public class TimePickerPanel extends JPanel {

    private final UIConsts.ChartTheme theme;
    private final Color primaryAccent;
    private final Color cellBg;
    private final Color cellBorder;
    private final Color cellText;
    private final Color labelColor;

    private LocalTime selectedTime;
    private boolean confirmed = false;
    private Runnable onConfirmListener;
    private Runnable onCancelListener;

    // UI elements
    private JLabel headerTitleLabel;
    private JLabel headerTimePreviewLabel;

    private JList<String> hourList;
    private JList<String> minuteList;
    private JList<String> secondList;
    private JList<String> amPmList;

    public TimePickerPanel(LocalTime initialTime) {
        this.theme = UIConsts.getChartTheme();
        this.primaryAccent = theme.getPrimaryAccentColor();
        this.cellBg = theme.getBackground();
        this.cellBorder = theme.getBorderColor();
        this.cellText = theme.getPlanetForeground();
        this.labelColor = theme.getLabelColor();

        this.selectedTime = (initialTime != null) ? initialTime : LocalTime.now();

        initComponents();
        syncListsFromTime(this.selectedTime);
        updateHeader();
        setPreferredSize(new Dimension(315, 360));
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

    public LocalTime getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedTime(LocalTime time) {
        if (time != null) {
            this.selectedTime = time;
            syncListsFromTime(time);
            updateHeader();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. Top Header Banner styled with active theme primary accent
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(primaryAccent);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        headerTitleLabel = new JLabel("Time");
        headerTitleLabel.setForeground(new Color(255, 255, 255, 215));
        headerTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        headerTimePreviewLabel = new JLabel(formatTime(selectedTime));
        headerTimePreviewLabel.setForeground(Color.WHITE);
        headerTimePreviewLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        headerPanel.add(headerTitleLabel);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(headerTimePreviewLabel);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center: 4 Column Pickers
        JPanel columnsPanel = new JPanel(new GridLayout(1, 4, 8, 0));
        columnsPanel.setBackground(Color.WHITE);
        columnsPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 10, 14));

        // Hours Column (01 - 12)
        DefaultListModel<String> hourModel = new DefaultListModel<>();
        for (int h = 1; h <= 12; h++) {
            hourModel.addElement(String.format("%02d", h));
        }
        hourList = createColumnList(hourModel);
        columnsPanel.add(createColumnCard("Hour", hourList));

        // Minutes Column (00 - 59)
        DefaultListModel<String> minuteModel = new DefaultListModel<>();
        for (int m = 0; m <= 59; m++) {
            minuteModel.addElement(String.format("%02d", m));
        }
        minuteList = createColumnList(minuteModel);
        columnsPanel.add(createColumnCard("Min", minuteList));

        // Seconds Column (00 - 59)
        DefaultListModel<String> secondModel = new DefaultListModel<>();
        for (int s = 0; s <= 59; s++) {
            secondModel.addElement(String.format("%02d", s));
        }
        secondList = createColumnList(secondModel);
        columnsPanel.add(createColumnCard("Sec", secondList));

        // AM / PM Column
        DefaultListModel<String> amPmModel = new DefaultListModel<>();
        amPmModel.addElement("AM");
        amPmModel.addElement("PM");
        amPmList = createColumnList(amPmModel);
        columnsPanel.add(createColumnCard("AM/PM", amPmList));

        // Wire selection listeners to live-update preview
        hourList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) onSelectionChanged(); });
        minuteList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) onSelectionChanged(); });
        secondList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) onSelectionChanged(); });
        amPmList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) onSelectionChanged(); });

        add(columnsPanel, BorderLayout.CENTER);

        // 3. Bottom Action Bar (Now, Cancel, Ok) with ModernButton (non-boxy)
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));

        ModernButton nowBtn = ModernButton.createSecondary("Now");
        nowBtn.setPreferredSize(new Dimension(68, 28));
        nowBtn.addActionListener(e -> setSelectedTime(LocalTime.now()));
        bottomBar.add(nowBtn, BorderLayout.WEST);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setBackground(Color.WHITE);

        ModernButton cancelBtn = ModernButton.createSecondary("Cancel");
        cancelBtn.setPreferredSize(new Dimension(72, 28));
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            if (onCancelListener != null) onCancelListener.run();
        });

        ModernButton okBtn = ModernButton.createPrimary("Ok", primaryAccent);
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

    private JList<String> createColumnList(DefaultListModel<String> model) {
        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(6);
        list.setFont(new Font("SansSerif", Font.BOLD, 15));
        list.setBackground(cellBg);
        list.setForeground(cellText);
        list.setSelectionBackground(primaryAccent);
        list.setSelectionForeground(Color.WHITE);

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(l, value, index, isSelected, cellHasFocus);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
                lbl.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
                if (isSelected) {
                    lbl.setBackground(primaryAccent);
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(cellBg);
                    lbl.setForeground(cellText);
                }
                return lbl;
            }
        });

        return list;
    }

    private JPanel createColumnCard(String title, JList<String> list) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(labelColor);
        card.add(label, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(58, 150));
        Color borderSoft = new Color(cellBorder.getRed(), cellBorder.getGreen(), cellBorder.getBlue(), 140);
        scroll.setBorder(BorderFactory.createLineBorder(borderSoft, 1));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void syncListsFromTime(LocalTime time) {
        int h = time.getHour();
        int m = time.getMinute();
        int s = time.getSecond();

        boolean isPm = (h >= 12);
        int hour12 = h % 12;
        if (hour12 == 0) hour12 = 12;

        hourList.setSelectedValue(String.format("%02d", hour12), true);
        minuteList.setSelectedValue(String.format("%02d", m), true);
        secondList.setSelectedValue(String.format("%02d", s), true);
        amPmList.setSelectedValue(isPm ? "PM" : "AM", true);
    }

    private void onSelectionChanged() {
        String hStr = hourList.getSelectedValue();
        String mStr = minuteList.getSelectedValue();
        String sStr = secondList.getSelectedValue();
        String amPm = amPmList.getSelectedValue();

        if (hStr == null || mStr == null || sStr == null || amPm == null) return;

        int h12 = Integer.parseInt(hStr);
        int min = Integer.parseInt(mStr);
        int sec = Integer.parseInt(sStr);
        boolean isPm = "PM".equalsIgnoreCase(amPm);

        int h24 = h12 % 12;
        if (isPm) h24 += 12;

        selectedTime = LocalTime.of(h24, min, sec);
        updateHeader();
    }

    private void updateHeader() {
        if (headerTimePreviewLabel != null && selectedTime != null) {
            headerTimePreviewLabel.setText(formatTime(selectedTime));
        }
    }

    private String formatTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("hh : mm : ss a"));
    }
}
