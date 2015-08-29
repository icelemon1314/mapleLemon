package client;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import tools.HexTool;
import tools.MaplePacketCreator;

public class DebugWindow extends JFrame {

    private MapleClient c;
    private JButton jButton1;
    private JLabel jLabel1;
    private JScrollPane jScrollPane1;
    private JTextArea jTextArea1;

    public DebugWindow() {
        initComponents();
        setLocationRelativeTo(null);
    }

    public MapleClient getC() {
        return this.c;
    }

    public void setC(MapleClient c) {
        this.c = c;
        if (c.getPlayer() != null) {
            setTitle("玩家: " + c.getPlayer().getName() + " - 封包测试");
        }
    }

    private void initComponents() {
        this.jScrollPane1 = new JScrollPane();
        this.jTextArea1 = new JTextArea();
        this.jButton1 = new JButton();
        this.jLabel1 = new JLabel();

        setDefaultCloseOperation(2);
        setTitle("调试窗口");
        setResizable(false);

        this.jTextArea1.setColumns(20);
        this.jTextArea1.setLineWrap(true);
        this.jTextArea1.setRows(5);
        this.jScrollPane1.setViewportView(this.jTextArea1);

        this.jButton1.setText("测试封包");
        this.jButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                DebugWindow.this.jButton1ActionPerformed(evt);
            }
        });
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jScrollPane1, -1, 446, 32767).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(this.jLabel1, -1, -1, 32767).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jButton1))).addContainerGap()));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.jScrollPane1, -1, 253, 32767).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(this.jButton1).addGroup(layout.createSequentialGroup().addGap(0, 0, 0).addComponent(this.jLabel1, -1, -1, 32767))).addContainerGap()));

        pack();
    }

    private void jButton1ActionPerformed(ActionEvent evt) {
        if (this.c == null) {
            this.jLabel1.setText("发送失败，客户为空.");
            return;
        }
        byte[] data = HexTool.getByteArrayFromHexString(this.jTextArea1.getText());
        this.jTextArea1.setText(null);
        this.jLabel1.setText(null);
        if ((this.c != null) && (data.length >= 2)) {
            this.c.getSession().write(MaplePacketCreator.testPacket(data));
            this.jLabel1.setText("发送成功，发送的封包长度: " + data.length);
        } else {
            this.jLabel1.setText("发送失败，发送的封包长度: " + data.length);
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(DebugWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DebugWindow().setVisible(true);
            }
        });
    }
}
