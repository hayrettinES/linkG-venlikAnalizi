import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebDataAnalyzer extends JFrame {
    private JTextField urlField;
    private JTextArea infoArea;
    private JTextArea scriptArea;
    private JTextArea securityArea;
    private JButton analyzeButton;

    // Oval kenarlık sınıfı
    static class RoundBorder implements Border {
        private int radius;
        private Color color;

        RoundBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(color);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    public WebDataAnalyzer() {
        // Ana pencere ayarları
        setTitle("Web Veri Analizörü");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Koyu tema için genel ayarlar
        Color backgroundColor = new Color(44, 47, 51); // #2C2F33
        Color panelColor = new Color(58, 61, 65); // #3A3D41
        Color textColor = new Color(228, 230, 235); // #E4E6EB
        Color buttonColor = new Color(88, 101, 242); // #5865F2
        Color buttonHoverColor = new Color(114, 137, 218); // #7289DA

        getContentPane().setBackground(backgroundColor);
        UIManager.put("Button.foreground", textColor);
        UIManager.put("TextField.foreground", textColor);
        UIManager.put("TextArea.foreground", textColor);
        UIManager.put("TextField.background", panelColor);
        UIManager.put("TextArea.background", panelColor);

        // Üst panel (URL girişi)
        JPanel topPanel = new JPanel();
        topPanel.setBackground(backgroundColor);
        topPanel.setLayout(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        urlField = new JTextField();
        urlField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        urlField.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(10, textColor),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        urlField.setCaretColor(textColor);

        analyzeButton = new JButton("Analiz Et") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    g.setColor(buttonHoverColor);
                } else {
                    g.setColor(buttonColor);
                }
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        analyzeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        analyzeButton.setContentAreaFilled(false);
        analyzeButton.setFocusPainted(false);
        analyzeButton.setBorder(new RoundBorder(20, buttonColor));
        analyzeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                analyzeButton.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                analyzeButton.repaint();
            }
        });

        JLabel urlLabel = new JLabel("URL: ");
        urlLabel.setForeground(textColor);
        urlLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        topPanel.add(urlLabel, BorderLayout.WEST);
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(analyzeButton, BorderLayout.EAST);

        // Merkez panel (Bilgi ve Script panelleri)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setBackground(backgroundColor);
        splitPane.setBorder(null);

        // Bilgi paneli
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(panelColor);
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(textColor), "Toplanan Bilgiler",
                0, 0, null, textColor));
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        infoArea.setForeground(textColor);
        infoArea.setBackground(panelColor);
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoScroll.setBorder(null);
        infoPanel.add(infoScroll, BorderLayout.CENTER);

        // Script paneli
        JPanel scriptPanel = new JPanel(new BorderLayout());
        scriptPanel.setBackground(panelColor);
        scriptPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(textColor), "Veri Toplama Scriptleri",
                0, 0, null, textColor));
        scriptArea = new JTextArea();
        scriptArea.setEditable(false);
        scriptArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scriptArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scriptArea.setForeground(textColor);
        scriptArea.setBackground(panelColor);
        JScrollPane scriptScroll = new JScrollPane(scriptArea);
        scriptScroll.setBorder(null);
        scriptPanel.add(scriptScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(infoPanel);
        splitPane.setRightComponent(scriptPanel);

        // Güvenlik paneli (sağ alt)
        JPanel securityPanel = new JPanel(new BorderLayout());
        securityPanel.setBackground(panelColor);
        securityPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(textColor), "Güvenlik Durumu",
                0, 0, null, textColor));
        securityArea = new JTextArea();
        securityArea.setEditable(false);
        securityArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        securityArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        securityArea.setForeground(textColor);
        securityArea.setBackground(panelColor);
        securityArea.setPreferredSize(new Dimension(0, 100)); // Küçük bir panel
        JScrollPane securityScroll = new JScrollPane(securityArea);
        securityScroll.setBorder(null);
        securityPanel.add(securityScroll, BorderLayout.CENTER);

        // Ana layout'a ekleme
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(securityPanel, BorderLayout.SOUTH);

        // Analiz butonu aksiyonu
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyzeUrl();
            }
        });
    }

    private void analyzeUrl() {
        String url = urlField.getText().trim();
        if (url.isEmpty() || !url.startsWith("http")) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir URL girin (http veya https ile başlamalı).",
                    "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // HTTP istemcisi oluştur
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .GET()
                    .build();

            // Yanıtı al
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Bilgileri analiz et
            StringBuilder info = new StringBuilder();
            info.append("Durum Kodu: ").append(response.statusCode()).append("\n");
            info.append("Yanıt Başlıkları:\n");
            response.headers().map().forEach((key, values) -> {
                info.append(key).append(": ").append(values).append("\n");
                if (key.equalsIgnoreCase("set-cookie")) {
                    info.append(" -> Çerez Tespit Edildi!\n");
                }
            });

            // Scriptleri analiz et
            StringBuilder scripts = new StringBuilder();
            String body = response.body();
            Pattern scriptPattern = Pattern.compile("<script[^>]*>(.*?)</script>", Pattern.DOTALL);
            Matcher matcher = scriptPattern.matcher(body);
            while (matcher.find()) {
                String scriptContent = matcher.group(1).trim();
                if (!scriptContent.isEmpty()) {
                    scripts.append("Script Bulundu:\n").append(scriptContent).append("\n\n");
                }
            }

            // İzleme scriptlerini kontrol et
            boolean hasTracking = false;
            if (body.contains("google-analytics") || body.contains("gtag")) {
                scripts.append("UYARI: Google Analytics izleme scripti tespit edildi!\n");
                hasTracking = true;
            }
            if (body.contains("facebook.com/tr")) {
                scripts.append("UYARI: Facebook izleme scripti tespit edildi!\n");
                hasTracking = true;
            }

            // Güvenlik analizi
            StringBuilder securityInfo = new StringBuilder();
            int securityScore = 100;

            // HTTPS kontrolü
            if (!url.startsWith("https")) {
                securityInfo.append("Uyarı: HTTPS kullanılmıyor.\n");
                securityScore -= 20;
            }

            // Durum kodu kontrolü
            if (response.statusCode() != 200) {
                securityInfo.append("Uyarı: Yanıt kodu ").append(response.statusCode()).append(" (beklenmeyen durum).\n");
                securityScore -= 15;
            }

            // Çerez kontrolü
            boolean hasCookies = response.headers().firstValue("set-cookie").isPresent();
            if (hasCookies) {
                securityInfo.append("Uyarı: Çerezler tespit edildi.\n");
                securityScore -= 10;
                // Secure veya HttpOnly olmayan çerezler için ek kontrol
                String cookieHeader = response.headers().firstValue("set-cookie").orElse("");
                if (!cookieHeader.contains("Secure") || !cookieHeader.contains("HttpOnly")) {
                    securityInfo.append("Uyarı: Güvenli olmayan çerezler tespit edildi.\n");
                    securityScore -= 10;
                }
            }

            // İzleme scriptleri
            if (hasTracking) {
                securityInfo.append("Uyarı: İzleme scriptleri tespit edildi.\n");
                securityScore -= 10;
            }

            // Şüpheli içerik kontrolü
            if (body.toLowerCase().contains("phishing") || body.toLowerCase().contains("malware")) {
                securityInfo.append("Tehlike: Şüpheli içerik tespit edildi (phishing/malware).\n");
                securityScore -= 30;
            }

            // Yönlendirme kontrolü
            if (response.statusCode() == 301 || response.statusCode() ==8) {
                securityInfo.append("Uyarı: Yönlendirme tespit edildi.\n");
                securityScore -= 10;
            }

            // Güvenlik sonucu
            if (securityScore >= 80) {
                securityInfo.append("Sonuç: Güvenli");
            } else if (securityScore >= 50) {
                securityInfo.append("Sonuç: Uyarı (Dikkatli olun)");
            } else {
                securityInfo.append("Sonuç: Güvensiz (Riskli)");
            }

            // Sonuçları göster
            infoArea.setText(info.toString());
            scriptArea.setText(scripts.toString());
            securityArea.setText(securityInfo.toString());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata oluştu: " + ex.getMessage(),
                    "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Swing'in daha iyi görünmesi için
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Uygulamayı başlat
        SwingUtilities.invokeLater(() -> {
            new WebDataAnalyzer().setVisible(true);
        });
    }
}