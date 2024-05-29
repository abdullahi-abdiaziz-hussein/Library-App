import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LibraryApp extends JFrame {

    private JTextField bookIdField, titleField, authorField, yearField;
    private JTable booksTable;
    private DefaultTableModel tableModel;

    private static final String DB_URL = "jdbc:ucanaccess://C://Users//ahmed//Documents//Library.accdb";

    public LibraryApp() {
        setTitle("Library Application");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(8, 2));
        inputPanel.add(new JLabel("Book ID:"));
        bookIdField = new JTextField();
        inputPanel.add(bookIdField);

        inputPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Author:"));
        authorField = new JTextField();
        inputPanel.add(authorField);

        inputPanel.add(new JLabel("Year:"));
        yearField = new JTextField();
        inputPanel.add(yearField);

        JButton addButton = new JButton("Add Book");
        addButton.addActionListener(new AddButtonListener());
        inputPanel.add(addButton);

        JButton deleteButton = new JButton("Delete Book");
        deleteButton.addActionListener(new DeleteButtonListener());
        inputPanel.add(deleteButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new RefreshButtonListener());
        inputPanel.add(refreshButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ClearButtonListener());
        inputPanel.add(clearButton);

        add(inputPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Book ID", "Title", "Author", "Year"}, 0);
        booksTable = new JTable(tableModel);
        add(new JScrollPane(booksTable), BorderLayout.CENTER);

        refreshBookList();
    }

    private void refreshBookList() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM Books")) {

            tableModel.setRowCount(0);
            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getString("BookID"),
                        resultSet.getString("Title"),
                        resultSet.getString("Author"),
                        resultSet.getString("Year")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String bookId = bookIdField.getText();
            String title = titleField.getText();
            String author = authorField.getText();
            String year = yearField.getText();

            try (Connection connection = DriverManager.getConnection(DB_URL);
                 PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Books (BookID, Title, Author, Year) VALUES (?, ?, ?, ?)")) {

                preparedStatement.setString(1, bookId);
                preparedStatement.setString(2, title);
                preparedStatement.setString(3, author);
                preparedStatement.setString(4, year);
                preparedStatement.executeUpdate();

                refreshBookList();
                JOptionPane.showMessageDialog(LibraryApp.this, "Book added successfully!");

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class DeleteButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow != -1) {
                String bookId = (String) tableModel.getValueAt(selectedRow, 0);

                try (Connection connection = DriverManager.getConnection(DB_URL);
                     PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Books WHERE BookID = ?")) {

                    preparedStatement.setString(1, bookId);
                    preparedStatement.executeUpdate();

                    refreshBookList();
                    JOptionPane.showMessageDialog(LibraryApp.this, "Book deleted successfully!");

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(LibraryApp.this, "Please select a book to delete.");
            }
        }
    }

    private class RefreshButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            refreshBookList();
        }
    }

    public class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            bookIdField.setText("");
            titleField.setText("");
            yearField.setText("");
            authorField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryApp app = new LibraryApp();
            app.setVisible(true);
        });
    }

}
