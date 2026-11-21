import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class VideographerApp extends JFrame {

    private static final Color SIDEBAR = new Color(30, 41, 59);
    private static final Color SIDEBAR_ACTIVE = new Color(79, 70, 229);
    private static final Color BACKGROUND = new Color(241, 245, 249);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color WARNING = new Color(217, 119, 6);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Font BASE = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font BOLD = new Font("SansSerif", Font.BOLD, 13);
    private static final Font TITLE = new Font("SansSerif", Font.BOLD, 26);
    private static final String PRICE_PATTERN = "[0-9]+(\\.[0-9]{1,2})?";

    private final CustomerManager customers = new CustomerManager();
    private final BookingManager bookings = new BookingManager();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final String[] pageNames = {"Dashboard", "Customers", "Bookings", "Availability", "Reports"};
    private final JButton[] navButtons = new JButton[pageNames.length];

    private final JLabel[] statValues = new JLabel[8];
    private DefaultTableModel dashboardUpcomingModel;

    private JTextField customerName, customerPhone, customerEmail, customerSearch;
    private DefaultTableModel customerModel;
    private JTable customerTable;

    private JComboBox<Customer> bookingCustomer;
    private JComboBox<String> bookingEvent, bookingStatus, bookingFilter;
    private JTextField bookingDate, bookingLocation, bookingPrice;
    private JSpinner bookingDays;
    private JLabel bookingTotal;
    private DefaultTableModel bookingModel;
    private JTable bookingTable;

    private YearMonth currentMonth = YearMonth.now();
    private JPanel calendarGrid;
    private JLabel monthLabel, availabilityMessage;
    private JTextField availabilityDate;

    private DefaultTableModel summaryModel, metricsModel, reportUpcomingModel;

    public VideographerApp() {
        super("Videographer Business Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 800);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        loadSampleData();

        add(buildSidebar(), BorderLayout.WEST);
        content.setBackground(BACKGROUND);
        content.add(buildDashboardPage(), "Dashboard");
        content.add(buildCustomerPage(), "Customers");
        content.add(buildBookingPage(), "Bookings");
        content.add(buildAvailabilityPage(), "Availability");
        content.add(buildReportsPage(), "Reports");
        add(content, BorderLayout.CENTER);

        showPage("Dashboard");
    }

    private void loadSampleData() {
        Customer a = customers.add("Rahul Sharma", "9876543210", "rahul.sharma@example.com");
        Customer b = customers.add("Priya Nair", "9123456780", "priya.nair@example.com");
        Customer c = customers.add("Arjun Mehta", "9988776655", "arjun.mehta@example.com");
        LocalDate today = LocalDate.now();
        bookings.add(new Booking(0, a.getId(), "Wedding", today.plusDays(12), 2, "Palace Grounds, Bengaluru", 25000, Booking.CONFIRMED));
        bookings.add(new Booking(0, b.getId(), "Birthday", today.plusDays(5), 1, "Indiranagar", 8000, Booking.PENDING));
        bookings.add(new Booking(0, c.getId(), "Corporate Event", today.plusDays(20), 1, "Whitefield", 15000, Booking.CONFIRMED));
        bookings.add(new Booking(0, a.getId(), "Engagement", today.minusDays(15), 1, "Koramangala", 12000, Booking.COMPLETED));
        bookings.add(new Booking(0, b.getId(), "Concert", today.plusDays(30), 1, "Hebbal", 20000, Booking.CANCELLED));
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(SIDEBAR);
        side.setPreferredSize(new Dimension(220, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(26, 14, 24, 14));

        JLabel brand = new JLabel("Videographer");
        brand.setFont(new Font("SansSerif", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Business Manager");
        sub.setFont(BASE);
        sub.setForeground(new Color(148, 163, 184));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(brand);
        side.add(sub);
        side.add(Box.createVerticalStrut(30));

        for (int i = 0; i < pageNames.length; i++) {
            final String name = pageNames[i];
            JButton button = styledButton(name, SIDEBAR);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setBorder(new EmptyBorder(11, 16, 11, 16));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            button.addActionListener(e -> showPage(name));
            navButtons[i] = button;
            side.add(button);
            side.add(Box.createVerticalStrut(6));
        }

        side.add(Box.createVerticalGlue());
        JButton exit = styledButton("Exit", DANGER);
        exit.setAlignmentX(Component.LEFT_ALIGNMENT);
        exit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        exit.addActionListener(e -> {
            if (confirm("Do you want to exit the application?\nAll data will be lost.")) {
                System.exit(0);
            }
        });
        side.add(exit);
        return side;
    }

    private void showPage(String name) {
        refreshAll();
        cardLayout.show(content, name);
        for (int i = 0; i < pageNames.length; i++) {
            navButtons[i].setBackground(pageNames[i].equals(name) ? SIDEBAR_ACTIVE : SIDEBAR);
        }
    }

    private void refreshAll() {
        refreshDashboard();
        refreshCustomerTable();
        refreshCustomerCombo();
        refreshBookingTable();
        refreshCalendar();
        refreshReports();
    }

    private JPanel createPage(String title, String subtitle, JComponent body) {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(24, 28, 24, 28));
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE);
        titleLabel.setForeground(TEXT);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(BASE);
        subtitleLabel.setForeground(MUTED);
        header.add(titleLabel);
        header.add(subtitleLabel);
        page.add(header, BorderLayout.NORTH);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(14, 14, 14, 14)));
        return panel;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(TEXT);
        return label;
    }

    private JButton styledButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 18, 9, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JTextField createField() {
        JTextField field = new JTextField();
        field.setFont(BASE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)), new EmptyBorder(6, 8, 6, 8)));
        field.setPreferredSize(new Dimension(100, 34));
        return field;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(BASE);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(100, 34));
    }

    private void addField(JPanel panel, String label, JComponent field, int x, int y, int width) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = width;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 6, 4, 6);
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setOpaque(false);
        JLabel text = new JLabel(label);
        text.setFont(BOLD);
        text.setForeground(MUTED);
        box.add(text, BorderLayout.NORTH);
        box.add(field, BorderLayout.CENTER);
        panel.add(box, g);
    }

    private void addWide(JPanel panel, JComponent component, int y, int width) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = y;
        g.gridwidth = width;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 0, 0);
        panel.add(component, g);
    }

    private DefaultTableModel createModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(BASE);
        table.setRowHeight(28);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = table.getTableHeader();
        header.setFont(BOLD);
        header.setBackground(BACKGROUND);
        header.setForeground(TEXT);
        header.setReorderingAllowed(false);
        return table;
    }

    private JScrollPane scroll(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(Color.WHITE);
        return pane;
    }

    private Color statusColor(String status) {
        if (Booking.PENDING.equals(status)) return WARNING;
        if (Booking.CONFIRMED.equals(status)) return BLUE;
        if (Booking.COMPLETED.equals(status)) return SUCCESS;
        if (Booking.CANCELLED.equals(status)) return DANGER;
        return TEXT;
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(BOLD);
            c.setForeground(statusColor(String.valueOf(value)));
            return c;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Please Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private String money(double value) {
        return String.format(Locale.US, "Rs. %,.2f", value);
    }

    private String customerNameOf(int customerId) {
        Customer c = customers.findById(customerId);
        return c == null ? "Unknown" : c.getName();
    }

    private String dateRange(Booking b) {
        if (b.getDays() == 1) {
            return b.getStartDate().toString();
        }
        return b.getStartDate() + " to " + b.getEndDate();
    }

    private LocalDate parseDate(String text) {
        try {
            LocalDate date = LocalDate.parse(text.trim());
            if (date.getYear() < 2000 || date.getYear() > 2100) {
                return null;
            }
            return date;
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private int selectedId(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return -1;
        }
        return (Integer) table.getModel().getValueAt(row, 0);
    }

    private JPanel buildDashboardPage() {
        String[] titles = {"Total Bookings", "Pending", "Confirmed", "Completed",
                "Cancelled", "Customers", "Total Revenue", "Pending Value"};
        Color[] colors = {PRIMARY, WARNING, BLUE, SUCCESS, DANGER, TEAL, SUCCESS, WARNING};

        JPanel stats = new JPanel(new GridLayout(2, 4, 14, 14));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(0, 200));
        for (int i = 0; i < statValues.length; i++) {
            statValues[i] = new JLabel("0");
            statValues[i].setFont(new Font("SansSerif", Font.BOLD, 24));
            statValues[i].setForeground(TEXT);
            stats.add(statCard(titles[i], statValues[i], colors[i]));
        }

        dashboardUpcomingModel = createModel(new String[]{"Date", "Customer", "Event", "Location", "Status"});
        JTable table = createTable(dashboardUpcomingModel);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        JPanel tableCard = card();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(sectionTitle("Upcoming Events"), BorderLayout.NORTH);
        tableCard.add(scroll(table), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        body.add(stats, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        return createPage("Dashboard", "Overview of your business at a glance", body);
    }

    private JPanel statCard(String title, JLabel value, Color accent) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 6, 0, 0, accent),
                        new EmptyBorder(14, 16, 14, 16))));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(BOLD);
        titleLabel.setForeground(MUTED);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private void refreshDashboard() {
        statValues[0].setText(String.valueOf(bookings.getCount()));
        statValues[1].setText(String.valueOf(bookings.countByStatus(Booking.PENDING)));
        statValues[2].setText(String.valueOf(bookings.countByStatus(Booking.CONFIRMED)));
        statValues[3].setText(String.valueOf(bookings.countByStatus(Booking.COMPLETED)));
        statValues[4].setText(String.valueOf(bookings.countByStatus(Booking.CANCELLED)));
        statValues[5].setText(String.valueOf(customers.getCount()));
        statValues[6].setText(money(bookings.getRevenue()));
        statValues[7].setText(money(bookings.totalByStatus(Booking.PENDING)));
        fillUpcoming(dashboardUpcomingModel, 8);
    }

    private void fillUpcoming(DefaultTableModel model, int limit) {
        model.setRowCount(0);
        Booking[] upcoming = bookings.getUpcoming();
        int shown = Math.min(limit, upcoming.length);
        for (int i = 0; i < shown; i++) {
            Booking b = upcoming[i];
            model.addRow(new Object[]{dateRange(b), customerNameOf(b.getCustomerId()),
                    b.getEventType(), b.getLocation(), b.getStatus()});
        }
    }

    private JPanel buildCustomerPage() {
        customerName = createField();
        customerPhone = createField();
        customerEmail = createField();
        customerSearch = createField();

        JPanel form = card();
        form.setLayout(new GridBagLayout());
        addField(form, "Full Name", customerName, 0, 0, 1);
        addField(form, "Phone Number", customerPhone, 1, 0, 1);
        addField(form, "Email Address", customerEmail, 2, 0, 1);

        JButton addButton = styledButton("Add Customer", SUCCESS);
        JButton updateButton = styledButton("Update Selected", PRIMARY);
        JButton deleteButton = styledButton("Delete Selected", DANGER);
        JButton clearButton = styledButton("Clear Form", MUTED);
        addButton.addActionListener(e -> addCustomer());
        updateButton.addActionListener(e -> updateCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        clearButton.addActionListener(e -> clearCustomerForm());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);
        addWide(form, buttons, 1, 3);

        customerModel = createModel(new String[]{"ID", "Name", "Phone", "Email"});
        customerTable = createTable(customerModel);
        customerTable.getColumnModel().getColumn(0).setMaxWidth(70);
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int id = selectedId(customerTable);
            if (id < 0) return;
            Customer c = customers.findById(id);
            if (c != null) {
                customerName.setText(c.getName());
                customerPhone.setText(c.getPhone());
                customerEmail.setText(c.getEmail());
            }
        });

        customerSearch.setPreferredSize(new Dimension(240, 34));
        customerSearch.addActionListener(e -> refreshCustomerTable());
        JButton searchButton = styledButton("Search", PRIMARY);
        JButton showAllButton = styledButton("Show All", MUTED);
        searchButton.addActionListener(e -> refreshCustomerTable());
        showAllButton.addActionListener(e -> {
            customerSearch.setText("");
            refreshCustomerTable();
        });
        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchControls.setOpaque(false);
        searchControls.add(new JLabel("Search by name, phone or email:"));
        searchControls.add(customerSearch);
        searchControls.add(searchButton);
        searchControls.add(showAllButton);

        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.setOpaque(false);
        searchBar.add(sectionTitle("Customer List"), BorderLayout.WEST);
        searchBar.add(searchControls, BorderLayout.EAST);

        JPanel tableCard = card();
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.add(searchBar, BorderLayout.NORTH);
        tableCard.add(scroll(customerTable), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        return createPage("Customers", "Add, search, update and delete customer records", body);
    }

    private String cleanPhone(String text) {
        return text.trim().replace(" ", "").replace("-", "");
    }

    private String validateCustomer(String name, String phone, String email, int excludeId) {
        if (name.isEmpty()) {
            return "Customer name is required.";
        }
        if (!name.matches("[A-Za-z][A-Za-z .'-]{1,59}")) {
            return "Name must be 2-60 characters and contain only letters, spaces, dots, hyphens or apostrophes.";
        }
        if (!phone.matches("\\+?[0-9]{10,13}")) {
            return "Phone number must contain 10 to 13 digits (an optional leading + is allowed).";
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Please enter a valid email address (for example name@example.com).";
        }
        if (customers.phoneExists(phone, excludeId)) {
            return "Another customer already uses this phone number.";
        }
        return null;
    }

    private void addCustomer() {
        String name = customerName.getText().trim();
        String phone = cleanPhone(customerPhone.getText());
        String email = customerEmail.getText().trim();
        String error = validateCustomer(name, phone, email, -1);
        if (error != null) {
            showError(error);
            return;
        }
        if (customers.isFull()) {
            showError("The customer list is full (maximum " + CustomerManager.CAPACITY + " customers).");
            return;
        }
        customers.add(name, phone, email);
        clearCustomerForm();
        refreshAll();
        showInfo("Customer \"" + name + "\" was added successfully.");
    }

    private void updateCustomer() {
        int id = selectedId(customerTable);
        if (id < 0) {
            showError("Please select a customer from the table first.");
            return;
        }
        String name = customerName.getText().trim();
        String phone = cleanPhone(customerPhone.getText());
        String email = customerEmail.getText().trim();
        String error = validateCustomer(name, phone, email, id);
        if (error != null) {
            showError(error);
            return;
        }
        Customer c = customers.findById(id);
        c.setName(name);
        c.setPhone(phone);
        c.setEmail(email);
        clearCustomerForm();
        refreshAll();
        showInfo("Customer details were updated.");
    }

    private void deleteCustomer() {
        int id = selectedId(customerTable);
        if (id < 0) {
            showError("Please select a customer from the table first.");
            return;
        }
        Customer c = customers.findById(id);
        if (bookings.hasActiveBookings(id)) {
            showError("\"" + c.getName() + "\" has bookings that are not cancelled.\n"
                    + "Cancel those bookings first, then delete the customer.");
            return;
        }
        if (!confirm("Delete customer \"" + c.getName() + "\"?\nTheir cancelled bookings will also be removed.")) {
            return;
        }
        bookings.removeForCustomer(id);
        customers.delete(id);
        clearCustomerForm();
        refreshAll();
        showInfo("Customer was deleted.");
    }

    private void clearCustomerForm() {
        customerName.setText("");
        customerPhone.setText("");
        customerEmail.setText("");
        customerTable.clearSelection();
    }

    private void refreshCustomerTable() {
        String query = customerSearch.getText().trim().toLowerCase();
        customerModel.setRowCount(0);
        for (int i = 0; i < customers.getCount(); i++) {
            Customer c = customers.getAt(i);
            boolean match = query.isEmpty()
                    || c.getName().toLowerCase().contains(query)
                    || c.getPhone().contains(query)
                    || c.getEmail().toLowerCase().contains(query);
            if (match) {
                customerModel.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(), c.getEmail()});
            }
        }
    }

    private JPanel buildBookingPage() {
        bookingCustomer = new JComboBox<>();
        bookingEvent = new JComboBox<>(new String[]{"Wedding", "Engagement", "Birthday", "Corporate Event",
                "Concert", "Pre-Wedding Shoot", "Product Shoot", "Other"});
        bookingStatus = new JComboBox<>(Booking.STATUSES);
        bookingFilter = new JComboBox<>(new String[]{"All", "Pending", "Confirmed", "Completed", "Cancelled"});
        styleCombo(bookingCustomer);
        styleCombo(bookingEvent);
        styleCombo(bookingStatus);
        styleCombo(bookingFilter);
        bookingFilter.setPreferredSize(new Dimension(150, 34));

        bookingDate = createField();
        bookingLocation = createField();
        bookingPrice = createField();
        bookingDays = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
        bookingDays.setFont(BASE);
        bookingDays.setPreferredSize(new Dimension(100, 34));
        ((JSpinner.DefaultEditor) bookingDays.getEditor()).getTextField().setFont(BASE);

        bookingTotal = new JLabel("Rs. 0.00");
        bookingTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        bookingTotal.setForeground(SUCCESS);
        bookingTotal.setOpaque(true);
        bookingTotal.setBackground(new Color(240, 253, 244));
        bookingTotal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 247, 208)), new EmptyBorder(4, 10, 4, 10)));
        bookingTotal.setPreferredSize(new Dimension(100, 34));

        JPanel form = card();
        form.setLayout(new GridBagLayout());
        addField(form, "Customer", bookingCustomer, 0, 0, 2);
        addField(form, "Event Type", bookingEvent, 2, 0, 1);
        addField(form, "Event Date (yyyy-MM-dd)", bookingDate, 3, 0, 1);
        addField(form, "Duration (days)", bookingDays, 0, 1, 1);
        addField(form, "Price per Day (Rs.)", bookingPrice, 1, 1, 1);
        addField(form, "Status", bookingStatus, 2, 1, 1);
        addField(form, "Total Amount", bookingTotal, 3, 1, 1);
        addField(form, "Event Location", bookingLocation, 0, 2, 4);

        JButton addButton = styledButton("Add Booking", SUCCESS);
        JButton updateButton = styledButton("Update Selected", PRIMARY);
        JButton cancelButton = styledButton("Cancel Booking", DANGER);
        JButton checkButton = styledButton("Check Availability", TEAL);
        JButton clearButton = styledButton("Clear Form", MUTED);
        addButton.addActionListener(e -> addBooking());
        updateButton.addActionListener(e -> updateBooking());
        cancelButton.addActionListener(e -> cancelBooking());
        checkButton.addActionListener(e -> checkAvailabilityFromForm());
        clearButton.addActionListener(e -> clearBookingForm());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(cancelButton);
        buttons.add(checkButton);
        buttons.add(clearButton);
        addWide(form, buttons, 3, 4);

        bookingModel = createModel(new String[]{"ID", "Customer", "Event", "Start Date", "End Date",
                "Days", "Location", "Price / Day", "Total", "Status"});
        bookingTable = createTable(bookingModel);
        bookingTable.getColumnModel().getColumn(0).setMaxWidth(60);
        bookingTable.getColumnModel().getColumn(5).setMaxWidth(60);
        bookingTable.getColumnModel().getColumn(9).setCellRenderer(new StatusRenderer());
        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int id = selectedId(bookingTable);
            if (id < 0) return;
            Booking b = bookings.findById(id);
            if (b != null) fillBookingForm(b);
        });

        bookingFilter.addActionListener(e -> refreshBookingTable());
        bookingPrice.getDocument().addDocumentListener(new DocChange(this::updateTotalLabel));
        bookingDays.addChangeListener(e -> updateTotalLabel());

        JPanel filterBar = new JPanel(new BorderLayout());
        filterBar.setOpaque(false);
        filterBar.add(sectionTitle("All Bookings"), BorderLayout.WEST);
        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterControls.setOpaque(false);
        filterControls.add(new JLabel("Filter by status:"));
        filterControls.add(bookingFilter);
        filterBar.add(filterControls, BorderLayout.EAST);

        JPanel tableCard = card();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(filterBar, BorderLayout.NORTH);
        tableCard.add(scroll(bookingTable), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);

        clearBookingForm();
        return createPage("Bookings", "Create, edit and cancel event bookings", body);
    }

    private void updateTotalLabel() {
        String text = bookingPrice.getText().trim();
        double total = 0;
        if (text.matches(PRICE_PATTERN)) {
            total = Double.parseDouble(text) * (Integer) bookingDays.getValue();
        }
        bookingTotal.setText(money(total));
    }

    private void fillBookingForm(Booking b) {
        for (int i = 0; i < bookingCustomer.getItemCount(); i++) {
            if (bookingCustomer.getItemAt(i).getId() == b.getCustomerId()) {
                bookingCustomer.setSelectedIndex(i);
            }
        }
        bookingEvent.setSelectedItem(b.getEventType());
        bookingDate.setText(b.getStartDate().toString());
        bookingDays.setValue(b.getDays());
        bookingLocation.setText(b.getLocation());
        bookingPrice.setText(String.format(Locale.US, "%.2f", b.getPricePerDay()));
        bookingStatus.setSelectedItem(b.getStatus());
        updateTotalLabel();
    }

    private void clearBookingForm() {
        bookingTable.clearSelection();
        bookingEvent.setSelectedIndex(0);
        bookingDate.setText(LocalDate.now().plusDays(1).toString());
        bookingDays.setValue(1);
        bookingLocation.setText("");
        bookingPrice.setText("");
        bookingStatus.setSelectedItem(Booking.PENDING);
        if (bookingCustomer.getItemCount() > 0) {
            bookingCustomer.setSelectedIndex(0);
        }
        updateTotalLabel();
    }

    private Booking readBookingForm(int id) {
        Customer customer = (Customer) bookingCustomer.getSelectedItem();
        if (customer == null) {
            showError("Please add a customer first, then select the customer for this booking.");
            return null;
        }
        LocalDate date = parseDate(bookingDate.getText());
        if (date == null) {
            showError("Enter a valid event date in yyyy-MM-dd format (for example 2026-12-25).");
            return null;
        }
        String location = bookingLocation.getText().trim();
        if (location.isEmpty()) {
            showError("Event location is required.");
            return null;
        }
        String priceText = bookingPrice.getText().trim();
        if (!priceText.matches(PRICE_PATTERN)) {
            showError("Price per day must be a positive number with at most 2 decimal places.");
            return null;
        }
        double price = Double.parseDouble(priceText);
        if (price <= 0 || price > 10000000) {
            showError("Price per day must be greater than 0 and not more than 10,000,000.");
            return null;
        }
        int days = (Integer) bookingDays.getValue();
        String status = (String) bookingStatus.getSelectedItem();
        LocalDate today = LocalDate.now();
        if ((Booking.PENDING.equals(status) || Booking.CONFIRMED.equals(status)) && date.isBefore(today)) {
            showError("Pending or Confirmed bookings cannot start in the past.\n"
                    + "Choose a future date, or set the status to Completed or Cancelled.");
            return null;
        }
        if (Booking.COMPLETED.equals(status) && date.isAfter(today)) {
            showError("A booking can only be marked Completed once the event date has arrived.");
            return null;
        }
        return new Booking(id, customer.getId(), (String) bookingEvent.getSelectedItem(),
                date, days, location, price, status);
    }

    private boolean passesAvailabilityCheck(Booking b) {
        if (!b.blocksDates()) {
            return true;
        }
        Booking other = bookings.findConflict(b.getStartDate(), b.getDays(), b.getId());
        if (other == null) {
            return true;
        }
        showError("Cannot save this booking as " + b.getStatus() + ".\n\n"
                + "The date " + dateRange(b) + " overlaps with booking #" + other.getId()
                + " (" + customerNameOf(other.getCustomerId()) + ", " + other.getEventType()
                + ", " + other.getStatus() + ") on " + dateRange(other) + ".\n\n"
                + "Choose different dates, or save this booking as Pending.");
        return false;
    }

    private void addBooking() {
        Booking b = readBookingForm(0);
        if (b == null) return;
        if (bookings.isFull()) {
            showError("The booking list is full (maximum " + BookingManager.CAPACITY + " bookings).");
            return;
        }
        if (!passesAvailabilityCheck(b)) return;
        bookings.add(b);
        clearBookingForm();
        refreshAll();
        showInfo("Booking #" + b.getId() + " was added.\nTotal amount: " + money(b.getTotal()));
    }

    private void updateBooking() {
        int id = selectedId(bookingTable);
        if (id < 0) {
            showError("Please select a booking from the table first.");
            return;
        }
        Booking existing = bookings.findById(id);
        Booking edited = readBookingForm(id);
        if (edited == null) return;
        if (!passesAvailabilityCheck(edited)) return;
        existing.copyFrom(edited);
        clearBookingForm();
        refreshAll();
        showInfo("Booking #" + id + " was updated.");
    }

    private void cancelBooking() {
        int id = selectedId(bookingTable);
        if (id < 0) {
            showError("Please select a booking from the table to cancel.");
            return;
        }
        Booking b = bookings.findById(id);
        if (Booking.CANCELLED.equals(b.getStatus())) {
            showInfo("This booking is already cancelled.");
            return;
        }
        if (!confirm("Cancel booking #" + id + " for " + customerNameOf(b.getCustomerId()) + "?")) {
            return;
        }
        b.setStatus(Booking.CANCELLED);
        clearBookingForm();
        refreshAll();
        showInfo("Booking #" + id + " was cancelled. Its dates are available again.");
    }

    private void checkAvailabilityFromForm() {
        LocalDate date = parseDate(bookingDate.getText());
        if (date == null) {
            showError("Enter a valid event date in yyyy-MM-dd format (for example 2026-12-25).");
            return;
        }
        int days = (Integer) bookingDays.getValue();
        Booking probe = new Booking(0, 0, "", date, days, "", 0, Booking.CONFIRMED);
        Booking conflict = bookings.findConflict(date, days, selectedId(bookingTable));
        if (conflict == null) {
            JOptionPane.showMessageDialog(this, "Available!\nNo confirmed booking exists for " + dateRange(probe) + ".",
                    "Availability", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Not available.\n" + dateRange(probe) + " overlaps with booking #"
                    + conflict.getId() + " (" + customerNameOf(conflict.getCustomerId()) + ", "
                    + conflict.getEventType() + ") on " + dateRange(conflict) + ".",
                    "Availability", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshCustomerCombo() {
        Customer selected = (Customer) bookingCustomer.getSelectedItem();
        int selectedCustomerId = selected == null ? -1 : selected.getId();
        bookingCustomer.removeAllItems();
        for (int i = 0; i < customers.getCount(); i++) {
            bookingCustomer.addItem(customers.getAt(i));
        }
        for (int i = 0; i < bookingCustomer.getItemCount(); i++) {
            if (bookingCustomer.getItemAt(i).getId() == selectedCustomerId) {
                bookingCustomer.setSelectedIndex(i);
            }
        }
    }

    private void refreshBookingTable() {
        String filter = (String) bookingFilter.getSelectedItem();
        bookingModel.setRowCount(0);
        for (int i = 0; i < bookings.getCount(); i++) {
            Booking b = bookings.getAt(i);
            if (filter == null || filter.equals("All") || filter.equals(b.getStatus())) {
                bookingModel.addRow(new Object[]{b.getId(), customerNameOf(b.getCustomerId()), b.getEventType(),
                        b.getStartDate().toString(), b.getEndDate().toString(), b.getDays(), b.getLocation(),
                        money(b.getPricePerDay()), money(b.getTotal()), b.getStatus()});
            }
        }
    }

    private JPanel buildAvailabilityPage() {
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        monthLabel.setForeground(TEXT);
        monthLabel.setPreferredSize(new Dimension(200, 34));

        JButton prevButton = styledButton("< Previous", PRIMARY);
        JButton nextButton = styledButton("Next >", PRIMARY);
        JButton todayButton = styledButton("Today", MUTED);
        prevButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });
        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });
        todayButton.addActionListener(e -> {
            currentMonth = YearMonth.now();
            refreshCalendar();
        });

        JPanel navigation = card();
        navigation.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
        navigation.add(prevButton);
        navigation.add(monthLabel);
        navigation.add(nextButton);
        navigation.add(todayButton);

        calendarGrid = new JPanel(new GridLayout(0, 7, 6, 6));
        calendarGrid.setOpaque(false);
        JPanel calendarCard = card();
        calendarCard.setLayout(new BorderLayout());
        calendarCard.add(calendarGrid, BorderLayout.CENTER);

        availabilityDate = createField();
        availabilityDate.setPreferredSize(new Dimension(150, 34));
        availabilityDate.setText(LocalDate.now().toString());
        JButton checkButton = styledButton("Check Date", TEAL);
        checkButton.addActionListener(e -> checkDateFromField());
        availabilityDate.addActionListener(e -> checkDateFromField());
        JPanel checkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        checkRow.setOpaque(false);
        JLabel checkLabel = new JLabel("Check a date (yyyy-MM-dd):");
        checkLabel.setFont(BOLD);
        checkRow.add(checkLabel);
        checkRow.add(availabilityDate);
        checkRow.add(checkButton);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        legend.setOpaque(false);
        legend.add(legendLabel("Available", new Color(220, 252, 231), new Color(22, 101, 52)));
        legend.add(legendLabel("Booked (Confirmed / Completed)", new Color(254, 226, 226), new Color(153, 27, 27)));
        legend.add(legendLabel("Pending (does not block the date)", new Color(254, 243, 199), new Color(146, 64, 14)));

        availabilityMessage = new JLabel("Click any date in the calendar to see its details.");
        availabilityMessage.setFont(BOLD);
        availabilityMessage.setForeground(MUTED);

        JPanel footer = card();
        footer.setLayout(new GridLayout(3, 1, 0, 8));
        footer.add(checkRow);
        footer.add(legend);
        footer.add(availabilityMessage);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(navigation, BorderLayout.NORTH);
        body.add(calendarCard, BorderLayout.CENTER);
        body.add(footer, BorderLayout.SOUTH);
        return createPage("Availability", "See which dates are free and which are booked", body);
    }

    private JLabel legendLabel(String text, Color background, Color foreground) {
        JLabel label = new JLabel(text);
        label.setFont(BOLD);
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setBorder(new EmptyBorder(5, 12, 5, 12));
        return label;
    }

    private void refreshCalendar() {
        monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)));
        calendarGrid.removeAll();
        String[] heads = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String head : heads) {
            JLabel label = new JLabel(head, SwingConstants.CENTER);
            label.setFont(BOLD);
            label.setForeground(MUTED);
            calendarGrid.add(label);
        }
        int blanks = currentMonth.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < blanks; i++) {
            calendarGrid.add(new JLabel(""));
        }
        LocalDate today = LocalDate.now();
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            final LocalDate date = currentMonth.atDay(day);
            String info;
            Color background;
            Color foreground;
            if (bookings.findBlockingOn(date) != null) {
                info = "Booked";
                background = new Color(254, 226, 226);
                foreground = new Color(153, 27, 27);
            } else if (bookings.findPendingOn(date) != null) {
                info = "Pending";
                background = new Color(254, 243, 199);
                foreground = new Color(146, 64, 14);
            } else {
                info = "Available";
                background = new Color(220, 252, 231);
                foreground = new Color(22, 101, 52);
            }
            JLabel cell = new JLabel("<html><center><b>" + day + "</b><br>" + info + "</center></html>",
                    SwingConstants.CENTER);
            cell.setFont(BASE);
            cell.setOpaque(true);
            cell.setBackground(background);
            cell.setForeground(foreground);
            cell.setBorder(date.equals(today)
                    ? BorderFactory.createLineBorder(PRIMARY, 3)
                    : BorderFactory.createLineBorder(BORDER));
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showDateDetails(date);
                }
            });
            calendarGrid.add(cell);
        }
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private void showDateDetails(LocalDate date) {
        Booking blocking = bookings.findBlockingOn(date);
        Booking pending = bookings.findPendingOn(date);
        if (blocking != null) {
            availabilityMessage.setForeground(DANGER);
            availabilityMessage.setText(date + " is BOOKED: " + customerNameOf(blocking.getCustomerId())
                    + " - " + blocking.getEventType() + " at " + blocking.getLocation()
                    + " (" + blocking.getStatus() + ")");
        } else if (pending != null) {
            availabilityMessage.setForeground(WARNING);
            availabilityMessage.setText(date + " is AVAILABLE, but has a pending booking: "
                    + customerNameOf(pending.getCustomerId()) + " - " + pending.getEventType());
        } else {
            availabilityMessage.setForeground(SUCCESS);
            availabilityMessage.setText(date + " is AVAILABLE.");
        }
    }

    private void checkDateFromField() {
        LocalDate date = parseDate(availabilityDate.getText());
        if (date == null) {
            showError("Enter a valid date in yyyy-MM-dd format (for example 2026-12-25).");
            return;
        }
        currentMonth = YearMonth.from(date);
        refreshCalendar();
        showDateDetails(date);
    }

    private JPanel buildReportsPage() {
        summaryModel = createModel(new String[]{"Status", "Bookings", "Value"});
        JTable summaryTable = createTable(summaryModel);
        summaryTable.getColumnModel().getColumn(0).setCellRenderer(new StatusRenderer());
        JPanel summaryCard = card();
        summaryCard.setLayout(new BorderLayout(0, 10));
        summaryCard.add(sectionTitle("Bookings by Status"), BorderLayout.NORTH);
        summaryCard.add(scroll(summaryTable), BorderLayout.CENTER);

        metricsModel = createModel(new String[]{"Business Summary", "Value"});
        JTable metricsTable = createTable(metricsModel);
        JPanel metricsCard = card();
        metricsCard.setLayout(new BorderLayout(0, 10));
        metricsCard.add(sectionTitle("Business Summary"), BorderLayout.NORTH);
        metricsCard.add(scroll(metricsTable), BorderLayout.CENTER);

        JPanel top = new JPanel(new GridLayout(1, 2, 16, 0));
        top.setOpaque(false);
        top.setPreferredSize(new Dimension(0, 310));
        top.add(summaryCard);
        top.add(metricsCard);

        reportUpcomingModel = createModel(new String[]{"Date", "Customer", "Event", "Location", "Status"});
        JTable upcomingTable = createTable(reportUpcomingModel);
        upcomingTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        JPanel upcomingCard = card();
        upcomingCard.setLayout(new BorderLayout(0, 10));
        upcomingCard.add(sectionTitle("Upcoming Events (Pending and Confirmed)"), BorderLayout.NORTH);
        upcomingCard.add(scroll(upcomingTable), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(upcomingCard, BorderLayout.CENTER);
        return createPage("Reports", "Booking statistics, revenue and upcoming events", body);
    }

    private void refreshReports() {
        summaryModel.setRowCount(0);
        double allValue = 0;
        for (String status : Booking.STATUSES) {
            double value = bookings.totalByStatus(status);
            allValue += value;
            summaryModel.addRow(new Object[]{status, bookings.countByStatus(status), money(value)});
        }
        summaryModel.addRow(new Object[]{"All Bookings", bookings.getCount(), money(allValue)});

        int earning = bookings.countByStatus(Booking.CONFIRMED) + bookings.countByStatus(Booking.COMPLETED);
        double revenue = bookings.getRevenue();
        double average = earning == 0 ? 0 : revenue / earning;
        metricsModel.setRowCount(0);
        metricsModel.addRow(new Object[]{"Total Bookings", bookings.getCount()});
        metricsModel.addRow(new Object[]{"Total Customers", customers.getCount()});
        metricsModel.addRow(new Object[]{"Total Revenue (Confirmed + Completed)", money(revenue)});
        metricsModel.addRow(new Object[]{"Pending Value", money(bookings.totalByStatus(Booking.PENDING))});
        metricsModel.addRow(new Object[]{"Value of Cancelled Bookings", money(bookings.totalByStatus(Booking.CANCELLED))});
        metricsModel.addRow(new Object[]{"Average Booking Value", money(average)});
        metricsModel.addRow(new Object[]{"Upcoming Events", bookings.getUpcoming().length});

        fillUpcoming(reportUpcomingModel, bookings.getCount());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Using default look and feel.");
        }
        SwingUtilities.invokeLater(() -> new VideographerApp().setVisible(true));
    }
}

class DocChange implements DocumentListener {
    private final Runnable action;

    DocChange(Runnable action) {
        this.action = action;
    }

    public void insertUpdate(DocumentEvent e) {
        action.run();
    }

    public void removeUpdate(DocumentEvent e) {
        action.run();
    }

    public void changedUpdate(DocumentEvent e) {
        action.run();
    }
}

class Customer {
    private final int id;
    private String name;
    private String phone;
    private String email;

    public Customer(int id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

class Booking {
    public static final String PENDING = "Pending";
    public static final String CONFIRMED = "Confirmed";
    public static final String COMPLETED = "Completed";
    public static final String CANCELLED = "Cancelled";
    public static final String[] STATUSES = {PENDING, CONFIRMED, COMPLETED, CANCELLED};

    private int id;
    private int customerId;
    private String eventType;
    private LocalDate startDate;
    private int days;
    private String location;
    private double pricePerDay;
    private String status;

    public Booking(int id, int customerId, String eventType, LocalDate startDate,
            int days, String location, double pricePerDay, String status) {
        this.id = id;
        this.customerId = customerId;
        this.eventType = eventType;
        this.startDate = startDate;
        this.days = days;
        this.location = location;
        this.pricePerDay = pricePerDay;
        this.status = status;
    }

    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public String getEventType() { return eventType; }
    public LocalDate getStartDate() { return startDate; }
    public int getDays() { return days; }
    public String getLocation() { return location; }
    public double getPricePerDay() { return pricePerDay; }
    public String getStatus() { return status; }
    public void setId(int id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }

    public double getTotal() {
        return pricePerDay * days;
    }

    public LocalDate getEndDate() {
        return startDate.plusDays(days - 1);
    }

    public boolean blocksDates() {
        return CONFIRMED.equals(status) || COMPLETED.equals(status);
    }

    public boolean coversDate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(getEndDate());
    }

    public boolean overlaps(LocalDate start, int numberOfDays) {
        LocalDate end = start.plusDays(numberOfDays - 1);
        return !start.isAfter(getEndDate()) && !end.isBefore(startDate);
    }

    public void copyFrom(Booking other) {
        this.customerId = other.customerId;
        this.eventType = other.eventType;
        this.startDate = other.startDate;
        this.days = other.days;
        this.location = other.location;
        this.pricePerDay = other.pricePerDay;
        this.status = other.status;
    }
}

class CustomerManager {
    public static final int CAPACITY = 200;
    private final Customer[] customers = new Customer[CAPACITY];
    private int count = 0;
    private int nextId = 1;

    public boolean isFull() {
        return count >= CAPACITY;
    }

    public int getCount() {
        return count;
    }

    public Customer getAt(int index) {
        if (index < 0 || index >= count) {
            return null;
        }
        return customers[index];
    }

    public Customer add(String name, String phone, String email) {
        if (isFull()) {
            return null;
        }
        Customer customer = new Customer(nextId++, name, phone, email);
        customers[count++] = customer;
        return customer;
    }

    public Customer findById(int id) {
        for (int i = 0; i < count; i++) {
            if (customers[i].getId() == id) {
                return customers[i];
            }
        }
        return null;
    }

    public boolean phoneExists(String phone, int excludeId) {
        for (int i = 0; i < count; i++) {
            if (customers[i].getId() != excludeId && customers[i].getPhone().equals(phone)) {
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) {
        for (int i = 0; i < count; i++) {
            if (customers[i].getId() == id) {
                for (int j = i; j < count - 1; j++) {
                    customers[j] = customers[j + 1];
                }
                customers[count - 1] = null;
                count--;
                return true;
            }
        }
        return false;
    }
}

class BookingManager {
    public static final int CAPACITY = 500;
    private final Booking[] bookings = new Booking[CAPACITY];
    private int count = 0;
    private int nextId = 1;

    public boolean isFull() {
        return count >= CAPACITY;
    }

    public int getCount() {
        return count;
    }

    public Booking getAt(int index) {
        if (index < 0 || index >= count) {
            return null;
        }
        return bookings[index];
    }

    public boolean add(Booking booking) {
        if (isFull()) {
            return false;
        }
        booking.setId(nextId++);
        bookings[count++] = booking;
        return true;
    }

    public Booking findById(int id) {
        for (int i = 0; i < count; i++) {
            if (bookings[i].getId() == id) {
                return bookings[i];
            }
        }
        return null;
    }

    public Booking findConflict(LocalDate start, int days, int excludeId) {
        for (int i = 0; i < count; i++) {
            Booking b = bookings[i];
            if (b.getId() != excludeId && b.blocksDates() && b.overlaps(start, days)) {
                return b;
            }
        }
        return null;
    }

    public Booking findBlockingOn(LocalDate date) {
        for (int i = 0; i < count; i++) {
            if (bookings[i].blocksDates() && bookings[i].coversDate(date)) {
                return bookings[i];
            }
        }
        return null;
    }

    public Booking findPendingOn(LocalDate date) {
        for (int i = 0; i < count; i++) {
            if (Booking.PENDING.equals(bookings[i].getStatus()) && bookings[i].coversDate(date)) {
                return bookings[i];
            }
        }
        return null;
    }

    public int countByStatus(String status) {
        int total = 0;
        for (int i = 0; i < count; i++) {
            if (bookings[i].getStatus().equals(status)) {
                total++;
            }
        }
        return total;
    }

    public double totalByStatus(String status) {
        double total = 0;
        for (int i = 0; i < count; i++) {
            if (bookings[i].getStatus().equals(status)) {
                total += bookings[i].getTotal();
            }
        }
        return total;
    }

    public double getRevenue() {
        return totalByStatus(Booking.CONFIRMED) + totalByStatus(Booking.COMPLETED);
    }

    public boolean hasActiveBookings(int customerId) {
        for (int i = 0; i < count; i++) {
            if (bookings[i].getCustomerId() == customerId && !Booking.CANCELLED.equals(bookings[i].getStatus())) {
                return true;
            }
        }
        return false;
    }

    public void removeForCustomer(int customerId) {
        for (int i = count - 1; i >= 0; i--) {
            if (bookings[i].getCustomerId() == customerId) {
                removeAt(i);
            }
        }
    }

    private void removeAt(int index) {
        for (int j = index; j < count - 1; j++) {
            bookings[j] = bookings[j + 1];
        }
        bookings[count - 1] = null;
        count--;
    }

    public Booking[] getUpcoming() {
        Booking[] temp = new Booking[count];
        int n = 0;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < count; i++) {
            Booking b = bookings[i];
            boolean open = Booking.PENDING.equals(b.getStatus()) || Booking.CONFIRMED.equals(b.getStatus());
            if (open && !b.getEndDate().isBefore(today)) {
                temp[n++] = b;
            }
        }
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (temp[j].getStartDate().isAfter(temp[j + 1].getStartDate())) {
                    Booking swap = temp[j];
                    temp[j] = temp[j + 1];
                    temp[j + 1] = swap;
                }
            }
        }
        Booking[] result = new Booking[n];
        for (int i = 0; i < n; i++) {
            result[i] = temp[i];
        }
        return result;
    }
}