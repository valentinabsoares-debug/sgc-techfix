package com.sgctechfix.swing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SgcSwingApp extends JFrame {

    private String token = "";
    private static final String BASE_URL = "http://localhost:8080/api";

    public SgcSwingApp() {
        setTitle("SGC TechFix");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        mostrarLogin();
    }

    private void mostrarLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel titulo = new JLabel("SGC TechFix - Login");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        JTextField emailField = new JTextField(20);
        JPasswordField senhaField = new JPasswordField(20);
        JButton loginBtn = new JButton("Entrar");
        JButton registerBtn = new JButton("Registrar");
        JLabel msgLabel = new JLabel(" ");
        msgLabel.setForeground(Color.RED);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titulo, gbc);
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("E-mail:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        panel.add(senhaField, gbc);
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(loginBtn, gbc);
        gbc.gridx = 1;
        panel.add(registerBtn, gbc);
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(msgLabel, gbc);

        loginBtn.addActionListener(e -> {
            String email = emailField.getText();
            String senha = new String(senhaField.getPassword());
            String body = "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
            try {
                String resp = post("/auth/login", body, false);
                token = extrairToken(resp);
                if (!token.isEmpty()) mostrarPrincipal();
                else msgLabel.setText("Login invalido.");
            } catch (Exception ex) { msgLabel.setText("Erro: " + ex.getMessage()); }
        });

        registerBtn.addActionListener(e -> {
            String email = emailField.getText();
            String senha = new String(senhaField.getPassword());
            String nome = JOptionPane.showInputDialog(this, "Digite seu nome:");
            if (nome == null || nome.isEmpty()) return;
            String body = "{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
            try {
                String resp = post("/auth/register", body, false);
                token = extrairToken(resp);
                if (!token.isEmpty()) mostrarPrincipal();
                else msgLabel.setText("Erro ao registrar.");
            } catch (Exception ex) { msgLabel.setText("Erro: " + ex.getMessage()); }
        });

        setContentPane(panel);
        revalidate();
        repaint();
    }

    private void mostrarPrincipal() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clientes", painelClientes());
        tabs.addTab("Produtos", painelProdutos());
        tabs.addTab("Vendas", painelVendas());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> { token = ""; mostrarLogin(); });

        JPanel top = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("  SGC TechFix", JLabel.LEFT);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        top.add(titulo, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);

        JPanel main = new JPanel(new BorderLayout());
        main.add(top, BorderLayout.NORTH);
        main.add(tabs, BorderLayout.CENTER);

        setContentPane(main);
        revalidate();
        repaint();
    }

    private JPanel painelClientes() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] colunas = {"ID", "Nome", "Email", "Telefone", "CPF", "Ativo"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        JTable table = new JTable(model);

        JButton listarBtn = new JButton("Listar");
        JButton novoBtn = new JButton("Novo");
        JButton deletarBtn = new JButton("Deletar");

        listarBtn.addActionListener(e -> carregarClientes(model));

        novoBtn.addActionListener(e -> {
            JTextField nome = new JTextField();
            JTextField email = new JTextField();
            JTextField tel = new JTextField();
            JTextField cpf = new JTextField();
            Object[] fields = {"Nome:", nome, "Email:", email, "Telefone:", tel, "CPF:", cpf};
            int ok = JOptionPane.showConfirmDialog(this, fields, "Novo Cliente", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                String body = "{\"nome\":\"" + nome.getText() + "\",\"email\":\"" + email.getText() +
                        "\",\"telefone\":\"" + tel.getText() + "\",\"cpf\":\"" + cpf.getText() + "\"}";
                try { post("/clientes", body, true); carregarClientes(model); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
            }
        });

        deletarBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um cliente."); return; }
            Long id = (Long) model.getValueAt(row, 0);
            try { delete("/clientes/" + id); carregarClientes(model); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
        });

        JPanel btns = new JPanel();
        btns.add(listarBtn); btns.add(novoBtn); btns.add(deletarBtn);
        panel.add(btns, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void carregarClientes(DefaultTableModel model) {
        try {
            String resp = get("/clientes");
            model.setRowCount(0);
            String[] itens = resp.replace("[", "").replace("]", "").split("\\},\\{");
            for (String item : itens) {
                if (item.trim().isEmpty()) continue;
                model.addRow(new Object[]{
                        extrairLong(item, "id"), extrairString(item, "nome"),
                        extrairString(item, "email"), extrairString(item, "telefone"),
                        extrairString(item, "cpf"), extrairString(item, "ativo")
                });
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private JPanel painelProdutos() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] colunas = {"ID", "Nome", "Categoria", "Preco", "Estoque", "Ativo"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        JTable table = new JTable(model);

        JButton listarBtn = new JButton("Listar");
        JButton novoBtn = new JButton("Novo");
        JButton deletarBtn = new JButton("Deletar");

        listarBtn.addActionListener(e -> carregarProdutos(model));

        novoBtn.addActionListener(e -> {
            JTextField nome = new JTextField();
            JTextField desc = new JTextField();
            JTextField preco = new JTextField();
            JTextField estoque = new JTextField();
            JTextField cat = new JTextField();
            Object[] fields = {"Nome:", nome, "Descricao:", desc, "Preco:", preco, "Estoque:", estoque, "Categoria:", cat};
            int ok = JOptionPane.showConfirmDialog(this, fields, "Novo Produto", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                String body = "{\"nome\":\"" + nome.getText() + "\",\"descricao\":\"" + desc.getText() +
                        "\",\"preco\":" + preco.getText() + ",\"estoque\":" + estoque.getText() +
                        ",\"categoria\":\"" + cat.getText() + "\"}";
                try { post("/produtos", body, true); carregarProdutos(model); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
            }
        });

        deletarBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um produto."); return; }
            Long id = (Long) model.getValueAt(row, 0);
            try { delete("/produtos/" + id); carregarProdutos(model); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
        });

        JPanel btns = new JPanel();
        btns.add(listarBtn); btns.add(novoBtn); btns.add(deletarBtn);
        panel.add(btns, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void carregarProdutos(DefaultTableModel model) {
        try {
            String resp = get("/produtos");
            model.setRowCount(0);
            String[] itens = resp.replace("[", "").replace("]", "").split("\\},\\{");
            for (String item : itens) {
                if (item.trim().isEmpty()) continue;
                model.addRow(new Object[]{
                        extrairLong(item, "id"), extrairString(item, "nome"),
                        extrairString(item, "categoria"), extrairString(item, "preco"),
                        extrairString(item, "estoque"), extrairString(item, "ativo")
                });
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private JPanel painelVendas() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] colunas = {"ID", "Data", "Cliente", "Total", "Observacao"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        JTable table = new JTable(model);

        JButton listarBtn = new JButton("Listar");
        JButton novaBtn = new JButton("Nova Venda");

        listarBtn.addActionListener(e -> carregarVendas(model));

        novaBtn.addActionListener(e -> {
            JTextField clienteId = new JTextField();
            JTextField usuarioId = new JTextField();
            JTextField produtoId = new JTextField();
            JTextField quantidade = new JTextField();
            JTextField obs = new JTextField();
            Object[] fields = {"ID do Cliente:", clienteId, "ID do Usuario:", usuarioId,
                    "ID do Produto:", produtoId, "Quantidade:", quantidade, "Observacao:", obs};
            int ok = JOptionPane.showConfirmDialog(this, fields, "Nova Venda", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                String body = "{\"clienteId\":" + clienteId.getText() +
                        ",\"usuarioId\":" + usuarioId.getText() +
                        ",\"itens\":[{\"produtoId\":" + produtoId.getText() +
                        ",\"quantidade\":" + quantidade.getText() + "}]" +
                        ",\"observacao\":\"" + obs.getText() + "\"}";
                try { post("/vendas", body, true); carregarVendas(model); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
            }
        });

        JPanel btns = new JPanel();
        btns.add(listarBtn); btns.add(novaBtn);
        panel.add(btns, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void carregarVendas(DefaultTableModel model) {
        try {
            String resp = get("/vendas");
            model.setRowCount(0);
            String[] itens = resp.replace("[", "").replace("]", "").split("\\},\\{");
            for (String item : itens) {
                if (item.trim().isEmpty()) continue;
                model.addRow(new Object[]{
                        extrairLong(item, "id"), extrairString(item, "data"),
                        extrairString(item, "clienteNome"), extrairString(item, "valorTotal"),
                        extrairString(item, "observacao")
                });
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private String get(String path) throws Exception {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        return lerResposta(conn);
    }

    private String post(String path, String body, boolean comToken) throws Exception {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        if (comToken) conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return lerResposta(conn);
    }

    private void delete(String path) throws Exception {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.getResponseCode();
    }

    private String lerResposta(HttpURLConnection conn) throws Exception {
        InputStream is = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (is == null) return "";
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String extrairToken(String json) {
        String key = "\"token\":\"";
        int i = json.indexOf(key);
        if (i < 0) return "";
        int start = i + key.length();
        int end = json.indexOf("\"", start);
        return end < 0 ? "" : json.substring(start, end);
    }

    private String extrairString(String json, String campo) {
        String key = "\"" + campo + "\":\"";
        int i = json.indexOf(key);
        if (i < 0) {
            String key2 = "\"" + campo + "\":";
            int j = json.indexOf(key2);
            if (j < 0) return "";
            int start = j + key2.length();
            int end = json.indexOf(",", start);
            if (end < 0) end = json.indexOf("}", start);
            return end < 0 ? "" : json.substring(start, end).replace("}", "").trim();
        }
        int start = i + key.length();
        int end = json.indexOf("\"", start);
        return end < 0 ? "" : json.substring(start, end);
    }

    private Long extrairLong(String json, String campo) {
        String val = extrairString(json, campo);
        try { return Long.parseLong(val.trim()); } catch (Exception e) { return 0L; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SgcSwingApp().setVisible(true));
    }
}